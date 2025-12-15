package io.github.gasparbarancelli.transform;

import java.util.function.Function;

/**
 * An abstract base class for parameter transformations that should only be applied to non-blank strings.
 *
 * <p>This class provides a helper method, {@link #executeWhenValueIsNotBlank}, that
 * ensures a given transformation function is only executed if the input value is not
 * {@code null} and not a blank string. If the value is blank, {@code null} is returned,
 * which is useful for preventing empty or whitespace-only strings from being used in
 * query parameters.</p>
 *
 * @see TransformParamContaining
 * @see TransformParamStartsWith
 * @see TransformParamEndsWith
 */
abstract class TransformParamString {

    /**
     * Executes a transformation function only if the given value is a non-blank string.
     *
     * @param value    The value to be checked and potentially transformed.
     * @param function The transformation function to be applied.
     * @return The result of the transformation, or {@code null} if the input value is blank.
     */
    Object executeWhenValueIsNotBlank(Object value, Function<Object, Object> function) {
        if (value != null && !value.toString().trim().isEmpty()) {
            return function.apply(value);
        }
        return null;
    }

}