package io.github.gasparbarancelli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to provide an inline SQL query for a native query method.
 *
 * <p>This annotation allows you to define the SQL query directly on the method,
 * instead of placing it in an external file. This can be convenient for simple
 * queries, but for more complex queries, using external files is recommended
 * for better readability and maintainability.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Repository
 * public interface UserRepository extends NativeQuery {
 *
 *     @NativeQuerySql("SELECT cod as \"id\", full_name as \"name\" FROM USER")
 *     List<UserTO> findBySqlInline();
 * }
 * }</pre>
 *
 * @see NativeQuery
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeQuerySql {

    /**
     * The inline SQL query to be executed.
     *
     * @return The SQL query string.
     */
    String value();

}