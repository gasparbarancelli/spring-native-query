package br.com.viasoft;

import lombok.Getter;
import org.aopalliance.intercept.MethodInvocation;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.ClassTypeInformation;

import java.util.ArrayList;
import java.util.List;

@Getter
public class NativeQueryInfo {

    private String file;

    private List<NativeQueryParameter> parameterList;

    private Pageable pageable;

    private Class<?> type;

    private NativeQueryInfo() {
    }

    public static NativeQueryInfo of(MethodInvocation invocation) {
        var info = new NativeQueryInfo();

        info.file = invocation.getMethod().getName();

        info.parameterList = new ArrayList<>();
        info.pageable = null;
        for (int i = 0; i < invocation.getArguments().length; i++) {
            var argument = invocation.getArguments()[i];
            var parameter = invocation.getMethod().getParameters()[i];
            if (parameter.getType().isAssignableFrom(Pageable.class)) {
                info.pageable = (Pageable) argument;
            } else {
                info.parameterList.add(new NativeQueryParameter(parameter.getName(), argument));
            }
        }

        var componentType = ClassTypeInformation.fromReturnTypeOf(invocation.getMethod()).getComponentType();
        info.type = componentType.getType();

        return info;
    }

    String getSql() {
        JtwigTemplate template = JtwigTemplate.classpathTemplate("nativeQuery/" + file + ".twig");
        JtwigModel model = JtwigModel.newModel();
        parameterList.forEach(p -> model.with(p.getName(), p.getValue()));
        return template.render(model);
    }

    boolean hasPagination() {
        return pageable != null;
    }

    int getFirstResult() {
        return pageable.getPageSize() * pageable.getPageNumber();
    }

    int getMaxResult() {
        return pageable.getPageSize();
    }

}
