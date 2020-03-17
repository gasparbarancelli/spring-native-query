package io.github.gasparbarancelli;

import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.SerializationUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class NativeQueryCache {

    private static final Map<NativeQueryInfoKey, NativeQueryInfo> CACHE_NATIVE_QUERY_INFO = new HashMap<>();

    private static final Map<String, Map<String, NativeQueryFieldInfo>> CACHE_FIELD_INFO = new HashMap<>();

    private static final Map<String, List<NativeQueryAccessMethod>> CACHE_ACCESS_METHODS = new HashMap<>();

    static NativeQueryInfo get(Class<? extends NativeQuery> classe, MethodInvocation invocation) {
        NativeQueryInfoKey nativeQueryInfoKey = new NativeQueryInfoKey(
                classe.getName(), 
                invocation.getMethod().getName()
        );

        NativeQueryInfo info = NativeQueryCache.CACHE_NATIVE_QUERY_INFO.get(nativeQueryInfoKey);
        if (info == null) {
            info = NativeQueryInfo.of(classe, invocation);
            NativeQueryCache.CACHE_NATIVE_QUERY_INFO.put(nativeQueryInfoKey, info);
        } else {
            try {
                info = (NativeQueryInfo) info.clone();
            } catch (CloneNotSupportedException e) {
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
                if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
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
                if (fieldInfoMap.get(accessMethod.getName()) == null) {
                    fieldInfoMap.put(accessMethod.getName(), new NativeQueryFieldInfo(accessMethod.getParam(), accessMethod.getType()));
                }
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

        public NativeQueryInfoKey(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NativeQueryInfoKey nativeQueryInfoKey = (NativeQueryInfoKey) o;
            return Objects.equals(className, nativeQueryInfoKey.className) &&
                    Objects.equals(methodName, nativeQueryInfoKey.methodName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, methodName);
        }
    }

}
