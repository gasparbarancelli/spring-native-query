package io.github.gasparbarancelli;

import java.lang.reflect.Field;

/**
 * Represents metadata about a field in a filter object used for native queries.
 *
 * <p>This class encapsulates information about a field, including its name, type, and
 * an optional {@link NativeQueryParam} annotation. It is used to dynamically construct
 * query parameters based on the fields of a filter object.</p>
 *
 * <p>The field name is capitalized to follow standard getter/setter naming conventions,
 * which is then used to invoke the corresponding accessor method via reflection.</p>
 *
 * @see NativeQueryParam
 * @see NativeQueryAccessMethod
 */
public class NativeQueryAccessField {

    private final String name;

    private final Class<?> type;

    private NativeQueryParam param;

    /**
     * Constructs a new {@code NativeQueryAccessField} based on a {@link Field}.
     *
     * @param field The field to be introspected.
     */
    public NativeQueryAccessField(Field field) {
        this.name = NativeQueryStringUtils.capitalize(field.getName());
        this.type = field.getType();
        if (field.isAnnotationPresent(NativeQueryParam.class)) {
            this.param = field.getAnnotation(NativeQueryParam.class);
        }
    }

    /**
     * Returns the capitalized name of the field.
     *
     * @return The field name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the field.
     *
     * @return The field's class type.
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the {@link NativeQueryParam} annotation present on the field, if any.
     *
     * @return The annotation, or {@code null} if it is not present.
     */
    public NativeQueryParam getParam() {
        return param;
    }

}