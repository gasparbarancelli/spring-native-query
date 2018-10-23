package br.com.viasoft;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.text.WordUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@AllArgsConstructor
class NativeQueryParameter {

    private String name;

    private Object value;

    static List<NativeQueryParameter> ofDeclaredMethods(String parentName, Class classe, Object object) {
        var parameterList = new ArrayList<NativeQueryParameter>();

        Map<String, NativeQueryParam> mapField = new HashMap<>();
        for (Field field : classe.getDeclaredFields()) {
            if (field.isAnnotationPresent(NativeQueryParam.class)) {
                mapField.put(WordUtils.capitalize(field.getName()), field.getAnnotation(NativeQueryParam.class));
            }
        }

        for (Method method : classe.getDeclaredMethods()) {
            if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
                try {
                    var value = method.invoke(object);

                    var methodName = method.getName().substring(method.getName().startsWith("get") ? 3 : 2);

                    NativeQueryParam queryParam;
                    if (method.isAnnotationPresent(NativeQueryParam.class)) {
                        queryParam = method.getAnnotation(NativeQueryParam.class);
                    } else {
                        queryParam = mapField.get(methodName);
                    }

                    if (queryParam != null) {
                        var paramName = parentName + WordUtils.capitalize(queryParam.value());
                        var paramValue = queryParam.operator().getTransformParam().apply(value);
                        parameterList.add(new NativeQueryParameter(paramName, paramValue));
                    } else {
                        var paramValue = NativeQueryOperator.DEFAULT.getTransformParam().apply(value);
                        parameterList.add(new NativeQueryParameter(parentName + methodName, paramValue));
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return parameterList;
    }

}
