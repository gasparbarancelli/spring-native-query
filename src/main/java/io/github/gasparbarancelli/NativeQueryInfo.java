package io.github.gasparbarancelli;

import org.aopalliance.intercept.MethodInvocation;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.ClassTypeInformation;
import org.springframework.data.util.TypeInformation;

import javax.persistence.Entity;
import java.io.File;
import java.lang.reflect.Parameter;
import java.util.*;

public class NativeQueryInfo {

    private String file;

    private List<NativeQueryParameter> parameterList;

    private Pageable pageable;

    private Class<?> aliasToBean;

    private Class<?> returnType;

    private boolean returnTypeIsIterable;

    private String sql;

    private Boolean isEntity;

    private Map<String, String> replaceSql = new HashMap<>();

    private List<Class<ProcessorSql>> processorSqlList = new ArrayList<>();

    private NativeQueryInfo() {
    }

    public static NativeQueryInfo of(Class<? extends NativeQuery> classe, MethodInvocation invocation) {
        NativeQueryInfo info = new NativeQueryInfo();

        info.file = "nativeQuery/";
        if (classe.isAnnotationPresent(NativeQueryFolder.class)) {
            info.file += classe.getAnnotation(NativeQueryFolder.class).value() + File.separator;
        }
        info.file += invocation.getMethod().getName() + ".twig";

        if (invocation.getMethod().isAnnotationPresent(NativeQueryReplaceSql.class)) {
            if (invocation.getMethod().getAnnotation(NativeQueryReplaceSql.class).values().length > 0) {
                for (NativeQueryReplaceSqlParams value : invocation.getMethod().getAnnotation(NativeQueryReplaceSql.class).values()) {
                    info.replaceSql.put(value.key(), value.value());
                }
                info.processorSqlList.addAll(Arrays.asList(invocation.getMethod().getAnnotation(NativeQueryReplaceSql.class).processorParams()));
            }
        }

        info.parameterList = new ArrayList<>();
        info.pageable = null;
        for (int i = 0; i < invocation.getArguments().length; i++) {
            Object argument = invocation.getArguments()[i];
            Parameter parameter = invocation.getMethod().getParameters()[i];
            if (parameter.getType().isAssignableFrom(Pageable.class)) {
                info.pageable = (Pageable) argument;
            } else {
                if (parameter.isAnnotationPresent(NativeQueryParam.class)) {
                    NativeQueryParam param = parameter.getAnnotation(NativeQueryParam.class);
                    if (param.addChildren()) {
                        info.parameterList.addAll(NativeQueryParameter.ofDeclaredMethods(param.value(), parameter.getType(), argument));
                    } else {
                        info.parameterList.add(new NativeQueryParameter(param.value(), param.operator().getTransformParam().apply(argument)));
                    }
                } else {
                    info.parameterList.add(new NativeQueryParameter(parameter.getName(), argument));
                }
            }
        }

        info.returnType = invocation.getMethod().getReturnType();
        info.returnTypeIsIterable = Iterable.class.isAssignableFrom(info.returnType);
        if (info.returnTypeIsIterable) {
            TypeInformation<?> componentType = ClassTypeInformation.fromReturnTypeOf(invocation.getMethod()).getComponentType();
            info.aliasToBean = Objects.requireNonNull(componentType).getType();
        } else {
            info.aliasToBean = info.returnType;
        }

        return info;
    }

    String getSql() {
        if (sql == null) {
            JtwigTemplate template = JtwigTemplate.classpathTemplate(file, JtwigTemplateConfig.get());
            JtwigModel model = JtwigModel.newModel();
            parameterList.forEach(p -> model.with(p.getName(), p.getValue()));
            sql = template.render(model);

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

            if (pageable != null) {
                StringBuilder orderBuilder = new StringBuilder();
                for (Sort.Order order : pageable.getSort()) {
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
        }
        return sql;
    }

    String getSqlTotalRecord() {
        return "select count(*) as totalRecords from (" + getSql() + ") x";
    }

    boolean isEntity() {
        if (isEntity == null) {
            isEntity = aliasToBean.isAnnotationPresent(Entity.class);
        }
        return isEntity;
    }

    boolean isJavaObject() {
        return aliasToBean.getPackage().getName().startsWith("java");
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
}
