package io.github.gasparbarancelli;

import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class NativeQueryCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeQueryCache.class);

    private static final Map<NativeQueryInfoKey, NativeQueryInfo> CACHE_NATIVE_QUERY_INFO = new HashMap<>();

    private static final Map<String, Map<String, NativeQueryFieldInfo>> CACHE_FIELD_INFO = new HashMap<>();

    private static final Map<String, List<NativeQueryAccessMethod>> CACHE_ACCESS_METHODS = new HashMap<>();

    private static final List<String> IGNORE_METHODS = Arrays.asList("toString", "hashCode", "equals");

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
            return "NativeQueryInfoKey{" +
                    "className='" + className + '\'' +
                    ", methodName='" + methodName + '\'' +
                    ", parameterTypes=" + parameterTypes +
                    '}';
        }
    }

}
