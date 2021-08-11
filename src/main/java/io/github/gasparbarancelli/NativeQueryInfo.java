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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;

import io.github.gasparbarancelli.engine.jtwig.JtwigTemplateEngineSQLProcessor;

public class NativeQueryInfo implements Serializable, Cloneable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeQueryInfo.class);

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

    private boolean useHibernateTypes;

    private final Map<String, String> replaceSql = new HashMap<>();

    private final List<Class<ProcessorSql>> processorSqlList = new ArrayList<>();

    private NativeQueryInfo() {
    }

    public static NativeQueryInfo of(Class<? extends NativeQuery> classe, MethodInvocation invocation) {
        NativeQueryInfo info = new NativeQueryInfo();

        Method method = invocation.getMethod();
        LOGGER.debug("invoked method {}", method.getName());
        info.useSqlInline = method.isAnnotationPresent(NativeQuerySql.class);
        if (info.useSqlInline) {
            LOGGER.debug("sql obtained using the NativeQuerySql annotation");
            info.sqlInline = method.getAnnotation(NativeQuerySql.class).value();
        } else {
            setFile(classe, invocation, info);
        }

        if (method.isAnnotationPresent(NativeQueryUseHibernateTypes.class)) {
            info.useHibernateTypes = method.getAnnotation(NativeQueryUseHibernateTypes.class).useHibernateTypes();
        } else {
            info.useHibernateTypes = Boolean.parseBoolean(PropertyUtil.getValue("native-query.use-hibernate-types", "true"));
        }
        LOGGER.debug("use hibernate types {}", info.useHibernateTypes);

        info.useJdbcTemplate = method.isAnnotationPresent(NativeQueryUseJdbcTemplate.class);
        if (info.useJdbcTemplate) {
            LOGGER.debug("use JdbcTemplate");
            NativeQueryUseJdbcTemplate jdbcTemplate = method.getAnnotation(NativeQueryUseJdbcTemplate.class);
            info.useTenant = jdbcTemplate.useTenant();
            LOGGER.debug("use JdbcTemplate with tenant {}", info.useTenant);
        }

        if (method.isAnnotationPresent(NativeQueryReplaceSql.class)) {
            if (method.getAnnotation(NativeQueryReplaceSql.class).values().length > 0) {
                LOGGER.debug("makes use of sql change");
                for (NativeQueryReplaceSqlParams value : method.getAnnotation(NativeQueryReplaceSql.class).values()) {
                    LOGGER.debug("replace key {} and value {}", value.key(), value.value());
                    info.replaceSql.put(value.key(), value.value());
                }
                info.processorSqlList.addAll(Arrays.asList(method.getAnnotation(NativeQueryReplaceSql.class).processorParams()));
            }
        }

        info.returnType = method.getReturnType();
        LOGGER.debug("return type {}", info.returnType.getName());
        info.returnTypeIsIterable = Iterable.class.isAssignableFrom(info.returnType);
        LOGGER.debug("return type is iterable {}", info.returnTypeIsIterable);
        if (info.returnTypeIsIterable || info.returnTypeIsOptional()) {
            TypeInformation<?> componentType = ClassTypeInformation.fromReturnTypeOf(method).getComponentType();
            info.aliasToBean = Objects.requireNonNull(componentType).getType();
        } else {
            info.aliasToBean = info.returnType;
        }
        LOGGER.debug("return object is {}", info.aliasToBean.getName());

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

        for (NativeQueryParameter parameter : info.parameterList) {
            LOGGER.debug("Parameter {} containing the value {} added", parameter.getName(), parameter.getValue());
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

        LOGGER.debug("sql obtained through the {} file", info.file);
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

                Sort.NullHandling nulls = order.getNullHandling();
                if (nulls != Sort.NullHandling.NATIVE) {
                    orderBuilder.append(" ")
                            .append(nulls.name().replace('_', ' '));
                }
            }

            sql += orderBuilder.toString();
        }

        if (useTenant) {
            NativeQueryTenantNamedParameterJdbcTemplateInterceptor tenantJdbcTemplate = ApplicationContextProvider.getApplicationContext().getBean(NativeQueryTenantNamedParameterJdbcTemplateInterceptor.class);
            sql = sql.replace(":SCHEMA", tenantJdbcTemplate.getTenant());
        }

        LOGGER.debug("SQL to be executed: {}", sql);

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
        String sqlCount = "select count(*) as totalRecords from (" + getSql() + ") x";
        LOGGER.debug("SQL Count to be executed: {}", sql);
        return sqlCount;
    }

    public boolean isUseJdbcTemplate() {
        return useJdbcTemplate;
    }

    boolean isEntity() {
        if (isEntity == null) {
            isEntity = aliasToBean.isAnnotationPresent(Entity.class);
        }
        LOGGER.debug("is entity {}", this.isEntity);
        return isEntity;
    }

    boolean isJavaObject() {
        boolean isJavaObject = getPackageName(aliasToBean).startsWith("java");
        LOGGER.debug("is java Object {}", isJavaObject);
        return isJavaObject;
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
        boolean isPagination = Page.class.isAssignableFrom(returnType);
        LOGGER.debug("is pagination {}", isPagination);
        return isPagination;
    }

    boolean isSingleResult() {
        boolean isSingleResult = !returnTypeIsIterable;
        LOGGER.debug("is single result {}", isSingleResult);
        return isSingleResult;
    }

    boolean hasPagination() {
        boolean hasPagiation = pageable != null;
        LOGGER.debug("has pagination {}", hasPagiation);
        return hasPagiation;
    }

    Pageable getPageable() {
        return pageable;
    }

    int getFirstResult() {
        int firstResult = pageable.getPageSize() * pageable.getPageNumber();
        LOGGER.debug("first result {}", firstResult);
        return firstResult;
    }

    int getMaxResult() {
        int maxResult = pageable.getPageSize();
        LOGGER.debug("max result {}", maxResult);
        return maxResult;
    }

    public String getFile() {
        return this.file;
    }

    List<NativeQueryParameter> getParameterList() {
        return this.parameterList;
    }

    Class<?> getAliasToBean() {
        LOGGER.debug("alias to bean {}", this.aliasToBean.getName());
        return this.aliasToBean;
    }

    Class<?> getReturnType() {
        LOGGER.debug("return type {}", this.returnType.getName());
        return this.returnType;
    }

    public boolean isReturnTypeIsIterable() {
        LOGGER.debug("is return type is iterable {}", this.returnTypeIsIterable);
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
        boolean typeIsOptional = this.returnType.getSimpleName().equals(Optional.class.getSimpleName());
        LOGGER.debug("Return type is optional {}", typeIsOptional);
        return typeIsOptional;
    }

    public boolean isUseHibernateTypes() {
        return this.useHibernateTypes;
    }
}
