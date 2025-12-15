package io.github.gasparbarancelli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used within {@link NativeQueryReplaceSql} to define a single key-value pair for SQL replacement.
 *
 * <p>This annotation is not intended to be used directly, but rather as part of the
 * {@code values} array in the {@link NativeQueryReplaceSql} annotation.</p>
 *
 * @see NativeQueryReplaceSql
 */
@Target({ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeQueryReplaceSqlParams {

    /**
     * The key to be replaced in the SQL query. The key should be enclosed in {@code ${...}}
     * in the SQL file (e.g., {@code ${myKey}}).
     *
     * @return The replacement key.
     */
    String key();

    /**
     * The value to replace the key with.
     *
     * @return The replacement value.
     */
    String value();

}