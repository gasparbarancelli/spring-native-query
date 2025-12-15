package io.github.gasparbarancelli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to customize the binding of a method parameter or a filter object field to a native query parameter.
 *
 * <p>This annotation provides several options for controlling how parameters are handled:</p>
 * <ul>
 *   <li>{@code value()}: Specifies the name of the query parameter.</li>
 *   <li>{@code operator()}: Applies an operator to transform the parameter's value (e.g., for LIKE searches).</li>
 *   <li>{@code addChildren()}: Indicates that the annotated parameter is a filter object, and its fields or methods should be added as individual parameters.</li>
 * </ul>
 *
 * <p>Example usage on a method parameter:</p>
 * <pre>{@code
 * UserTO findUserById(@NativeQueryParam("codigo") Number id);
 * }</pre>
 *
 * <p>Example usage on a filter object field:</p>
 * <pre>{@code
 * public class UserFilter {
 *     @NativeQueryParam(value = "name", operator = NativeQueryOperator.CONTAINING)
 *     private String name;
 * }
 * }</pre>
 *
 * @see NativeQueryOperator
 */
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeQueryParam {

    /**
     * The name of the parameter in the native query.
     *
     * @return The parameter name.
     */
    String value();

    /**
     * The operator to be applied to the parameter's value.
     *
     * @return The operator.
     */
    NativeQueryOperator operator() default NativeQueryOperator.DEFAULT;

    /**
     * If {@code true}, indicates that the annotated parameter is a filter object, and its
     * fields/methods should be recursively added as parameters to the query.
     *
     * @return {@code true} if the parameter is a filter object, {@code false} otherwise.
     */
    boolean addChildren() default false;

}