package io.github.gasparbarancelli;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * Represents metadata about an accessor method (getter) in a filter object used for native queries.
 *
 * <p>This class encapsulates information about a method, including the method itself, its
 * derived property name, its return type, and an optional {@link NativeQueryParam} annotation.
 * It is used to dynamically construct query parameters based on the accessor methods of a filter object.</p>
 *
 * <p>The property name is derived by stripping the "get" or "is" prefix from the method name.</p>
 *
 * @see NativeQueryParam
 * @see NativeQueryAccessField
 */
public class NativeQueryAccessMethod {

    private final Method method;

    private final String name;

    private final Class<?> type;

    private NativeQueryParam param;

    /**
     * Constructs a new {@code NativeQueryAccessMethod} based on a {@link Method}.
     *
     * @param method The accessor method to be introspected.
     */
    public NativeQueryAccessMethod(Method method) {
        this.method = method;
        this.name = method.getName().substring(method.getName().startsWith("get") ? 3 : method.getName().startsWith("is") ? 2 : 0);
        if (method.getAnnotatedReturnType().getType() instanceof ParameterizedType) {
            this.type = (Class<?>) ((ParameterizedType) method.getAnnotatedReturnType().getType()).getRawType();
        } else {
            this.type = (Class<?>) method.getAnnotatedReturnType().getType();
        }
        if (method.isAnnotationPresent(NativeQueryParam.class)) {
            this.param = method.getAnnotation(NativeQueryParam.class);
        }
    }

    /**
     * Returns the underlying {@link Method} instance.
     *
     * @return The method.
     */
    public Method getMethod() {
        return method;
    }

    /**
     * Returns the derived property name of the method.
     *
     * @return The property name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the {@link NativeQueryParam} annotation present on the method, if any.
     *
     * @return The annotation, or {@code null} if it is not present.
     */
    public NativeQueryParam getParam() {
        return param;
    }

    /**
     * Returns the return type of the method.
     *
     * @return The method's return type.
     */
    public Class<?> getType() {
        return type;
    }
}