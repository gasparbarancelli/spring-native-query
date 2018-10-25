package br.com.viasoft;

import lombok.Getter;
import org.aopalliance.intercept.MethodInvocation;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.ClassTypeInformation;

import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

@Getter
public class NativeQueryInfo {

    private String file;

    private List<NativeQueryParameter> parameterList;

    private Pageable pageable;

    private Class<?> aliasToBean;

    private Class<?> returnType;

    private boolean returnTypeIsIterable;

    private String sql;

    private Boolean isEntity;

    private NativeQueryInfo() {
    }

    public static NativeQueryInfo of(Class<? extends NativeQuery> classe, MethodInvocation invocation) {
        var info = new NativeQueryInfo();

        info.file = "nativeQuery/";
        if (classe.isAnnotationPresent(NativeQueryFolder.class)) {
            info.file += classe.getAnnotation(NativeQueryFolder.class).value() + "/";
        }
        info.file += invocation.getMethod().getName() + ".twig";

        info.parameterList = new ArrayList<>();
        info.pageable = null;
        for (int i = 0; i < invocation.getArguments().length; i++) {
            var argument = invocation.getArguments()[i];
            var parameter = invocation.getMethod().getParameters()[i];
            if (parameter.getType().isAssignableFrom(Pageable.class)) {
                info.pageable = (Pageable) argument;
            } else {
                if (parameter.isAnnotationPresent(NativeQueryParam.class)) {
                    var param = parameter.getAnnotation(NativeQueryParam.class);
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
            var componentType = ClassTypeInformation.fromReturnTypeOf(invocation.getMethod()).getComponentType();
            info.aliasToBean = componentType.getType();
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
        return "java.lang".equals(aliasToBean.getPackageName());
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

}
