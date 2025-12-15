package io.github.gasparbarancelli;

import io.github.gasparbarancelli.transform.TransformParamContaining;
import io.github.gasparbarancelli.transform.TransformParamEndsWith;
import io.github.gasparbarancelli.transform.TransformParamStartsWith;

import java.util.function.Function;

/**
 * An enum representing the operators that can be applied to a query parameter.
 *
 * <p>This enum defines a set of operators that can be used to transform the value of a
 * query parameter before it is used in a query. This is particularly useful for
 * implementing common SQL patterns like {@code LIKE} searches.</p>
 *
 * <p>Each operator is associated with a {@link Function} that performs the transformation.</p>
 *
 * @see NativeQueryParam
 * @see TransformParamContaining
 * @see TransformParamStartsWith
 * @see TransformParamEndsWith
 */
public enum NativeQueryOperator {

    /**
     * The default operator, which performs no transformation.
     */
    DEFAULT(value -> value),

    /**
     * An operator that wraps the parameter value with {@code %} characters for a "containing" search.
     */
    CONTAINING(new TransformParamContaining()),

    /**
     * An operator that appends a {@code %} character to the parameter value for a "starts with" search.
     */
    STARTS_WITH(new TransformParamStartsWith()),

    /**
     * An operator that prepends a {@code %} character to the parameter value for an "ends with" search.
     */
    ENDS_WITH(new TransformParamEndsWith());

    private final Function<Object, Object> transformParam;

    NativeQueryOperator(Function<Object, Object> transformParam) {
        this.transformParam = transformParam;
    }

    /**
     * Returns the function that performs the parameter transformation.
     *
     * @return The transformation function.
     */
    public Function<Object, Object> getTransformParam() {
        return transformParam;
    }

}