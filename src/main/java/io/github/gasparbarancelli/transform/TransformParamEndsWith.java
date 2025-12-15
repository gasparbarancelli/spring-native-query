package io.github.gasparbarancelli.transform;

import java.util.function.Function;

/**
 * A function that transforms a parameter value for an "ends with" (LIKE) search.
 *
 * <p>This class implements the {@link Function} interface to provide a transformation
 * that prepends a {@code %} wildcard to a string value. This is intended to be used
 * with the {@code LIKE} operator in SQL queries.</p>
 *
 * <p>The transformation is only applied if the input value is a non-blank string.
 * Otherwise, the original value is returned.</p>
 *
 * @see io.github.gasparbarancelli.NativeQueryOperator#ENDS_WITH
 */
public class TransformParamEndsWith extends TransformParamString implements Function<Object, Object> {

    /**
     * Applies the "ends with" transformation to the given object.
     *
     * @param o The object to be transformed.
     * @return The transformed string (e.g., "%value"), or the original object if it's not a non-blank string.
     */
    @Override
    public Object apply(Object o) {
        return executeWhenValueIsNotBlank(o, o1 -> "%" + o1.toString());
    }

}