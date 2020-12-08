package io.github.gasparbarancelli;

import org.apache.commons.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NativeQueryParameter implements Serializable, Cloneable {

    // todo adicionar log
    private static final Logger LOGGER = LoggerFactory.getLogger(NativeQueryParameter.class);

    private final String name;

    private final Object value;

    public NativeQueryParameter(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    static List<NativeQueryParameter> ofDeclaredMethods(String parentName, Class<?> classe, Object object) {
        ArrayList<NativeQueryParameter> parameterList = new ArrayList<>();

        Map<String, NativeQueryFieldInfo> fieldInfoMap = NativeQueryCache.getFieldInfo(classe);
        List<NativeQueryAccessMethod> accessMethods = NativeQueryCache.getAccessMethods(classe);
        for (NativeQueryAccessMethod accessMethod : accessMethods) {
            Object value = getValue(object, accessMethod.getMethod());

            NativeQueryFieldInfo fieldInfo = fieldInfoMap.get(accessMethod.getName());
            if (fieldInfo != null) {
                NativeQueryParam queryParam = fieldInfo.getParam() != null ? fieldInfo.getParam() : accessMethod.getParam();
                if (queryParam != null) {
                    if (queryParam.addChildren()) {
                        String parentNameChildren = parentName + WordUtils.capitalize(queryParam.value());
                        parameterList.addAll(ofDeclaredMethods(parentNameChildren, fieldInfo.getType(), value));
                    } else {
                        String paramName = parentName + WordUtils.capitalize(queryParam.value());
                        if (value instanceof Map) {
                            parameterList.addAll(ofMap((Map) value, paramName));
                        } else {
                            Object paramValue = queryParam.operator().getTransformParam().apply(value);
                            parameterList.add(new NativeQueryParameter(paramName, paramValue));
                        }
                    }
                } else {
                    if (value instanceof Map) {
                        parameterList.addAll(ofMap((Map) value, parentName + accessMethod.getName()));
                    } else {
                        Object paramValue = NativeQueryOperator.DEFAULT.getTransformParam().apply(value);
                        parameterList.add(new NativeQueryParameter(parentName + accessMethod.getName(), paramValue));
                    }
                }
            }
        }

        return parameterList;
    }

    private static Object getValue(Object object, Method method) {
        try {
            return method.invoke(object);
        } catch (Exception ignore) {
            return null;
        }
    }

    static List<NativeQueryParameter> ofMap(Map map, String name) {
        ArrayList<NativeQueryParameter> parameterList = new ArrayList<>();
        parameterList.add(new NativeQueryParameter(name, map.keySet()));
        map.forEach((k, v) -> parameterList.add(new NativeQueryParameter(k.toString(), v)));
        return parameterList;
    }

    public String getName() {
        return this.name;
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "NativeQueryParameter{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }
}
