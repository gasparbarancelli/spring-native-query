package io.github.gasparbarancelli;

/**
 * Encapsulates metadata about a field in a filter object, specifically its type and associated query parameter annotation.
 *
 * <p>This class is used to store information about a field that will be used to generate
 * a dynamic query. It holds the {@link NativeQueryParam} annotation, which provides

 * details on how the field should be treated in the query, and the field's data type.</p>
 *
 * <p>This information is typically cached to avoid repeated reflection.</p>
 *
 * @see NativeQueryParam
 * @see NativeQueryCache
 */
public class NativeQueryFieldInfo {

    private final NativeQueryParam param;

    private final Class<?> type;

    /**
     * Constructs a new {@code NativeQueryFieldInfo}.
     *
     * @param param The {@link NativeQueryParam} annotation associated with the field.
     * @param type  The data type of the field.
     */
    public NativeQueryFieldInfo(NativeQueryParam param, Class<?> type) {
        this.param = param;
        this.type = type;
    }

    /**
     * Returns the {@link NativeQueryParam} annotation.
     *
     * @return The query parameter annotation.
     */
    public NativeQueryParam getParam() {
        return param;
    }

    /**
     * Returns the data type of the field.
     *
     * @return The field's class type.
     */
    public Class<?> getType() {
        return type;
    }
}