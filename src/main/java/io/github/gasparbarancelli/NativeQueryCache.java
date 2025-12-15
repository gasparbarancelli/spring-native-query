package io.github.gasparbarancelli;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * A cache for storing metadata related to native query processing.
 *
 * <p>This class provides a caching mechanism for various pieces of information used by the
 * library, such as query details, field information, and accessor methods. Caching this
 * data avoids the overhead of repeated reflection and annotation processing, improving
 * the overall performance of the query execution.</p>
 *
 * <p>The cache is divided into several parts:</p>
 * <ul>
 *   <li>{@code CACHE_NATIVE_QUERY_INFO}: Caches {@link NativeQueryInfo} objects, which contain
 *       all the necessary information to execute a native query for a specific method.</li>
 *   <li>{@code CACHE_FIELD_INFO}: Caches metadata about the fields of filter objects.</li>
 *   <li>{@code CACHE_ACCESS_METHODS}: Caches accessor methods (getters) of filter objects.</li>
 * </ul>
 *
 * @see NativeQueryInfo
 * @see NativeQueryFieldInfo
 * @see NativeQueryAccessMethod
 */
public class NativeQueryCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeQueryCache.class);

    private static final Map<NativeQueryInfoKey, NativeQueryInfo> CACHE_NATIVE_QUERY_INFO = new HashMap<>();

    private static final Map<String, Map<String, NativeQueryFieldInfo>> CACHE_FIELD_INFO = new HashMap<>();

    private static final Map<String, List<NativeQueryAccessMethod>> CACHE_ACCESS_METHODS = new HashMap<>();

    private static final List<String> IGNORE_METHODS = Arrays.asList("toString", "hashCode", "equals");

    /**
     * Retrieves {@link NativeQueryInfo} for a given method invocation, using the cache if available.
     *
     * <p>If the information is not in the cache, it is created, cached, and then returned.
     * If it is already cached, a clone of the cached object is returned to ensure thread safety.</p>
     *
     * @param classe     The {@link NativeQuery} interface class.
     * @param invocation The method invocation for which to retrieve the query info.
     * @return The {@link NativeQueryInfo} for the invocation.
     */
    static NativeQueryInfo get(Class<? extends NativeQuery> classe, MethodInvocation invocation) {
        NativeQueryInfoKey nativeQueryInfoKey = new NativeQueryInfoKey(
                classe.getName(),
                invocation.getMethod().getName(),
                Arrays.stream(invocation.getMethod().getParameterTypes())
                        .map(Class::getName)
                        .collect(Collectors.toList())
        );
        LOGGER.debug("information cache key {}", nativeQueryInfoKey);

        NativeQueryInfo info = NativeQueryCache.CACHE_NATIVE_QUERY_INFO.get(nativeQueryInfoKey);
        if (info == null) {
            info = NativeQueryInfo.of(classe, invocation);
            LOGGER.debug("caching method {} information from interface {}", invocation.getMethod().getName(), classe.getName());
            NativeQueryCache.CACHE_NATIVE_QUERY_INFO.put(nativeQueryInfoKey, info);
        } else {
            try {
                LOGGER.debug("getting from the cache the information of method {} of class {}", invocation.getMethod().getName(), classe.getName());
                info = (NativeQueryInfo) info.clone();
            } catch (CloneNotSupportedException e) {
                LOGGER.debug("error in cloning the information that was cached in method {} of class {}", invocation.getMethod().getName(), classe.getName());
                throw new RuntimeException(e);
            }
        }
        NativeQueryInfo.setParameters(info, invocation);
        return info;
    }

    /**
     * Retrieves a list of {@link NativeQueryAccessMethod}s for a given class, using the cache if available.
     *
     * @param classe The class to introspect.
     * @return A list of accessor methods.
     */
    static List<NativeQueryAccessMethod> getAccessMethods(Class<?> classe) {
        String className = classe.getName();
        List<NativeQueryAccessMethod> methods = CACHE_ACCESS_METHODS.get(className);
        if (methods == null) {
            methods = new ArrayList<>();
            for (Method method : classe.getDeclaredMethods()) {
                if (!IGNORE_METHODS.contains(method.getName())) {
                    methods.add(new NativeQueryAccessMethod(method));
                }
            }
            CACHE_ACCESS_METHODS.put(className, methods);
        }
        return methods;
    }

    /**
     * Retrieves a map of field information for a given class, using the cache if available.
     *
     * @param classe The class to introspect.
     * @return A map where keys are field names and values are {@link NativeQueryFieldInfo} objects.
     */
    static Map<String, NativeQueryFieldInfo> getFieldInfo(Class<?> classe) {
        String className = classe.getName();
        Map<String, NativeQueryFieldInfo> fieldInfoMap = CACHE_FIELD_INFO.get(className);
        if (fieldInfoMap == null) {
            fieldInfoMap = new HashMap<>();

            List<NativeQueryAccessField> accessFields = getAccessFields(classe);
            for (NativeQueryAccessField accessField : accessFields) {
                fieldInfoMap.put(accessField.getName(), new NativeQueryFieldInfo(accessField.getParam(), accessField.getType()));
            }

            List<NativeQueryAccessMethod> accessMethods = getAccessMethods(classe);
            for (NativeQueryAccessMethod accessMethod : accessMethods) {
                fieldInfoMap.computeIfAbsent(
                        accessMethod.getName(),
                        k -> new NativeQueryFieldInfo(accessMethod.getParam(), accessMethod.getType())
                );
            }

            CACHE_FIELD_INFO.put(className, fieldInfoMap);
        }
        return fieldInfoMap;
    }

    private static List<NativeQueryAccessField> getAccessFields(Class<?> classe) {
        List<NativeQueryAccessField> fields = new ArrayList<>();
        for (Field field : classe.getDeclaredFields()) {
            fields.add(new NativeQueryAccessField(field));
        }
        return fields;
    }

    /**
     * A key for caching {@link NativeQueryInfo} objects.
     *
     * <p>This class is used as the key in the {@code CACHE_NATIVE_QUERY_INFO} map. It uniquely
     * identifies a method in a {@link NativeQuery} interface by its class name, method name,
     * and parameter types.</p>
     */
    private static class NativeQueryInfoKey {

        String className;
        String methodName;
        List<String> parameterTypes;

        public NativeQueryInfoKey(String className, String methodName, List<String> parameterTypes) {
            this.className = className;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NativeQueryInfoKey that = (NativeQueryInfoKey) o;
            return Objects.equals(className, that.className) &&
                    Objects.equals(methodName, that.methodName) &&
                    Objects.equals(parameterTypes, that.parameterTypes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, methodName, parameterTypes);
        }

        @Override
        public String toString() {
            return "NativeQueryInfoKey{"
                    + "className='" + className + "'" +
                    ", methodName='" + methodName + "'" +
                    ", parameterTypes=" + parameterTypes +
                    '}';
        }
    }

}