package io.github.gasparbarancelli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to perform replacements in a SQL query and to apply custom processors.
 *
 * <p>This annotation allows for dynamic modification of a SQL query by defining a set of
 * key-value pairs for replacement. It can also be used to register custom
 * {@link ProcessorSql} implementations that can perform more complex transformations
 * on the SQL string.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @NativeQueryReplaceSql(
 *     values = {
 *         @NativeQueryReplaceSqlParams(key = "columns", value = "id, name, email")
 *     },
 *     processorParams = {
 *         MyCustomSqlProcessor.class
 *     }
 * )
 * List<User> findUsers();
 * }</pre>
 *
 * @see NativeQueryReplaceSqlParams
 * @see ProcessorSql
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeQueryReplaceSql {

    /**
     * An array of {@link NativeQueryReplaceSqlParams} defining the key-value pairs for replacement.
     *
     * @return The replacement parameters.
     */
    NativeQueryReplaceSqlParams[] values() default {};

    /**
     * An array of custom {@link ProcessorSql} classes to be applied to the SQL query.
     *
     * @return The custom SQL processors.
     */
    Class<? extends ProcessorSql>[] processorParams() default {};

}