package io.github.gasparbarancelli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Represents a parameter to be used in a native query.
 *
 * <p>This class encapsulates the name and value of a query parameter. It also provides
 * static factory methods for creating lists of parameters from filter objects or maps.</p>
 *
 * <p>When creating parameters from a filter object, this class uses reflection to inspect
 * the object's fields and methods, taking into account {@link NativeQueryParam} annotations
 * to determine the parameter names and transformations.</p>
 *
 * @see NativeQueryParam
 * @see NativeQueryInfo
 */
public class NativeQueryParameter implements Serializable, Cloneable {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeQueryParameter.class);

    private final String name;

    private final Object value;

    /**
     * Constructs a new {@code NativeQueryParameter}.
     *
     * @param name  The name of the parameter.
     * @param value The value of the parameter.
     */
    public NativeQueryParameter(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Creates a list of {@code NativeQueryParameter}s from a filter object.
     *
     * <p>This method recursively introspects the fields and methods of a filter object,
     * creating parameters based on their values and annotations.</p>
     *
     * @param parentName The parent name to be prepended to the parameter names.
     * @param classe     The class of the filter object.
     * @param object     The filter object instance.
     * @return A list of query parameters.
     */
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
                        String parentNameChildren = parentName + NativeQueryStringUtils.capitalize(queryParam.value());
                        parameterList.addAll(ofDeclaredMethods(parentNameChildren, fieldInfo.getType(), value));
                    } else {
                        String paramName = parentName + NativeQueryStringUtils.capitalize(queryParam.value());
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

    /**
     * Creates a list of {@code NativeQueryParameter}s from a map.
     *
     * @param map  The map containing the parameters.
     * @param name The name to be used for the map's keyset parameter.
     * @return A list of query parameters.
     */
    static List<NativeQueryParameter> ofMap(Map map, String name) {
        ArrayList<NativeQueryParameter> parameterList = new ArrayList<>();
        parameterList.add(new NativeQueryParameter(name, map.keySet()));
        map.forEach((k, v) -> parameterList.add(new NativeQueryParameter(k.toString(), v)));
        return parameterList;
    }

    /**
     * Returns the name of the parameter.
     *
     * @return The parameter name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Returns the value of the parameter.
     *
     * @return The parameter value.
     */
    public Object getValue() {
        return this.value;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @Override
    public String toString() {
        return "NativeQueryParameter{"
                + "name='" + name + "'" +
                ", value=" + value +
                '}';
    }
}