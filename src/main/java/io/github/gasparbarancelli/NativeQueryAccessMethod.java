package io.github.gasparbarancelli;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

public class NativeQueryAccessMethod {

    private final Method method;

    private final String name;

    private final Class<?> type;

    private NativeQueryParam param;

    public NativeQueryAccessMethod(Method method) {
        this.method = method;
        this.name = method.getName().substring(method.getName().startsWith("get") ? 3 : 2);
        if (method.getAnnotatedReturnType().getType() instanceof ParameterizedType) {
            this.type = (Class<?>) ((ParameterizedType) method.getAnnotatedReturnType().getType()).getRawType();
        } else {
            this.type = (Class<?>) method.getAnnotatedReturnType().getType();
        }
        if (method.isAnnotationPresent(NativeQueryParam.class)) {
            this.param = method.getAnnotation(NativeQueryParam.class);
        }
    }

    public Method getMethod() {
        return method;
    }

    public String getName() {
        return name;
    }

    public NativeQueryParam getParam() {
        return param;
    }

    public Class<?> getType() {
        return type;
    }
}
