package io.github.gasparbarancelli;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.persistence.Entity;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;

import io.github.gasparbarancelli.engine.jtwig.JtwigTemplateEngineSQLProcessor;

public class NativeQueryInfo implements Serializable, Cloneable {

    private String file;

    private List<NativeQueryParameter> parameterList;

    private Pageable pageable;

    private Sort sort;

    private Class<?> aliasToBean;

    private Class<?> returnType;

    private boolean returnTypeIsIterable;

    private String sql;

    private String sqlInline;

    private boolean useSqlInline;

    private Boolean isEntity;

    private boolean useJdbcTemplate;

    private boolean useTenant;

    private final Map<String, String> replaceSql = new HashMap<>();

    private final List<Class<ProcessorSql>> processorSqlList = new ArrayList<>();

    private NativeQueryInfo() {
    }

    public static NativeQueryInfo of(Class<? extends NativeQuery> classe, MethodInvocation invocation) {
        NativeQueryInfo info = new NativeQueryInfo();

        Method method = invocation.getMethod();
        info.useSqlInline = method.isAnnotationPresent(NativeQuerySql.class);
        if (info.useSqlInline) {
            info.sqlInline = method.getAnnotation(NativeQuerySql.class).value();
        } else {
            setFile(classe, invocation, info);
        }

        info.useJdbcTemplate = method.isAnnotationPresent(NativeQueryUseJdbcTemplate.class);
        if (info.useJdbcTemplate) {
            NativeQueryUseJdbcTemplate jdbcTemplate = method.getAnnotation(NativeQueryUseJdbcTemplate.class);
            info.useTenant = jdbcTemplate.useTenant();
        }

        if (method.isAnnotationPresent(NativeQueryReplaceSql.class)) {
            if (method.getAnnotation(NativeQueryReplaceSql.class).values().length > 0) {
                for (NativeQueryReplaceSqlParams value : method.getAnnotation(NativeQueryReplaceSql.class).values()) {
                    info.replaceSql.put(value.key(), value.value());
                }
                info.processorSqlList.addAll(Arrays.asList(method.getAnnotation(NativeQueryReplaceSql.class).processorParams()));
            }
        }

        info.returnType = method.getReturnType();
        info.returnTypeIsIterable = Iterable.class.isAssignableFrom(info.returnType);
        if (info.returnTypeIsIterable || info.returnTypeIsOptional()) {
            TypeInformation<?> componentType = ClassTypeInformation.fromReturnTypeOf(method).getComponentType();
            info.aliasToBean = Objects.requireNonNull(componentType).getType();
        } else {
            info.aliasToBean = info.returnType;
        }

        return info;
    }

    public static void setParameters(NativeQueryInfo info, MethodInvocation invocation) {
        info.sql = null;
        info.sort = null;
        info.parameterList = new ArrayList<>();
        info.pageable = null;
        for (int i = 0; i < invocation.getArguments().length; i++) {
            Object argument = invocation.getArguments()[i];
            Parameter parameter = invocation.getMethod().getParameters()[i];
            if (parameter.getType().isAssignableFrom(Pageable.class)) {
                info.pageable = (Pageable) argument;
                if (info.sort == null) {
                    info.sort = info.pageable.getSort();
                }
            } else if (parameter.getType().isAssignableFrom(Sort.class)) {
                info.sort = (Sort) argument;
            } else {
                if (parameter.isAnnotationPresent(NativeQueryParam.class)) {
                    NativeQueryParam param = parameter.getAnnotation(NativeQueryParam.class);
                    if (param.addChildren()) {
                        info.parameterList.addAll(NativeQueryParameter.ofDeclaredMethods(param.value(), parameter.getType(), argument));
                    } else {
                        if (argument instanceof Map) {
                            info.parameterList.addAll(NativeQueryParameter.ofMap((Map) argument, param.value()));
                        } else {
                            info.parameterList.add(new NativeQueryParameter(param.value(), param.operator().getTransformParam().apply(argument)));
                        }
                    }
                } else {
                    if (argument instanceof Map) {
                        info.parameterList.addAll(NativeQueryParameter.ofMap((Map) argument, parameter.getName()));
                    } else {
                        info.parameterList.add(new NativeQueryParameter(parameter.getName(), argument));
                    }
                }
            }
        }
    }

    private static void setFile(Class<? extends NativeQuery> classe, MethodInvocation invocation, NativeQueryInfo info) {
        info.file = PropertyUtil.getValue("native-query.sql.directory", NativeQueryAutoConfiguration.SQL_DIRECTORY);

        if (!info.file.endsWith("/")) {
            info.file += "/";
        }

        if (classe.isAnnotationPresent(NativeQueryFolder.class)) {
            info.file += classe.getAnnotation(NativeQueryFolder.class).value() + File.separator;
        }

        final Method method = invocation.getMethod();
        if (method.isAnnotationPresent(NativeQueryFileName.class)) {
            info.file += method.getAnnotation(NativeQueryFileName.class).value() + ".";
        } else {
            info.file += method.getName() + ".";
        }

        String fileSufix = PropertyUtil.getValue("native-query.file.sufix", "twig");

        // backwards compatibility where the default extension was twig
        if (new ClassPathResource(info.file + fileSufix).exists()) {
            info.file += fileSufix;
        } else if (new ClassPathResource(info.file + "sql").exists()) {
            info.file += "sql";
        } else {
            info.file += "twig";
        }
    }

    String getSql() {
        if (sql != null) {
            return sql;
        }

        sql = getSqlProcessed();

        for (Class<ProcessorSql> aClass : processorSqlList) {
            try {
                ProcessorSql processor = aClass.newInstance();
                processor.execute(sql, replaceSql);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }

        for (Map.Entry<String, String> replaceSqlEntry : replaceSql.entrySet()) {
            sql = sql.replaceAll("\\$\\{"+replaceSqlEntry.getKey()+"}", replaceSqlEntry.getValue());
        }

        if (sort != null) {
            StringBuilder orderBuilder = new StringBuilder();
            for (Sort.Order order : sort) {
                if (orderBuilder.length() == 0) {
                    orderBuilder.append(" ORDER BY ");
                } else {
                    orderBuilder.append(", ");
                }
                orderBuilder.append(order.getProperty())
                        .append(" ")
                        .append(order.getDirection().name());
            }

            sql += orderBuilder.toString();
        }

        if (useTenant) {
            NativeQueryTenantNamedParameterJdbcTemplateInterceptor tenantJdbcTemplate = ApplicationContextProvider.getApplicationContext().getBean(NativeQueryTenantNamedParameterJdbcTemplateInterceptor.class);
            sql = sql.replace(":SCHEMA", tenantJdbcTemplate.getTenant());
        }

        return sql;
    }

    private String getSqlProcessed() {
        return new JtwigTemplateEngineSQLProcessor()
                .setParameter(parameterList)
                .inline(useSqlInline)
                .setClasspathTemplate(file)
                .setInlineTemplate(sqlInline)
                .getSql();
    }

    String getSqlTotalRecord() {
        return "select count(*) as totalRecords from (" + getSql() + ") x";
    }

    public boolean isUseJdbcTemplate() {
        return useJdbcTemplate;
    }

    boolean isEntity() {
        if (isEntity == null) {
            isEntity = aliasToBean.isAnnotationPresent(Entity.class);
        }
        return isEntity;
    }

    boolean isJavaObject() {
        return getPackageName(aliasToBean).startsWith("java");
    }

    private String getPackageName(Class<?> c) {
        final String pn;
        while (c.isArray()) {
            c = c.getComponentType();
        }
        if (c.isPrimitive()) {
            pn = "java.lang";
        } else {
            String cn = c.getName();
            int dot = cn.lastIndexOf('.');
            pn = (dot != -1) ? cn.substring(0, dot).intern() : "";
        }
        return pn;
    }

    boolean isPagination() {
        return Page.class.isAssignableFrom(returnType);
    }

    boolean isSingleResult() {
        return !returnTypeIsIterable;
    }

    boolean hasPagination() {
        return pageable != null;
    }

    Pageable getPageable() {
        return pageable;
    }

    int getFirstResult() {
        return pageable.getPageSize() * pageable.getPageNumber();
    }

    int getMaxResult() {
        return pageable.getPageSize();
    }

    public String getFile() {
        return this.file;
    }

    List<NativeQueryParameter> getParameterList() {
        return this.parameterList;
    }

    Class<?> getAliasToBean() {
        return this.aliasToBean;
    }

    Class<?> getReturnType() {
        return this.returnType;
    }

    public boolean isReturnTypeIsIterable() {
        return this.returnTypeIsIterable;
    }

    public Boolean getIsEntity() {
        return this.isEntity;
    }

    public Map<String, String> getReplaceSql() {
        return this.replaceSql;
    }

    public List<Class<ProcessorSql>> getProcessorSqlList() {
        return this.processorSqlList;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public boolean returnTypeIsOptional() {
        return this.returnType.getSimpleName().equals(Optional.class.getSimpleName());
    }
}
