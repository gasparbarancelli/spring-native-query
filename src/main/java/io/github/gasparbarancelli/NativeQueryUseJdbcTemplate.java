package io.github.gasparbarancelli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that indicates a native query method should be executed using
 * Spring's {@code NamedParameterJdbcTemplate} instead of the default Hibernate
 * {@code EntityManager}.
 *
 * <p>By default, all queries are executed with Hibernate. This annotation allows you
 * to switch to {@code JdbcTemplate} on a per-method basis. This can be useful for
 * queries that do not map well to Hibernate entities or for projects that prefer
 * the explicitness of JDBC.</p>
 *
 * <p><b>Warning:</b> This annotation does not support methods that use {@code Pageable}
 * for pagination, as {@code JdbcTemplate} does not have a built-in mechanism for
 * efficient pagination in the same way that Hibernate does.</p>
 *
 * <p>This annotation also provides support for multi-tenancy through the
 * {@link #useTenant()} attribute.</p>
 *
 * @see NativeQueryTenantNamedParameterJdbcTemplateInterceptor
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeQueryUseJdbcTemplate {

    /**
     * If {@code true}, enables multi-tenant support for the query.
     *
     * <p>When this is enabled, you must provide a bean that implements the
     * {@link NativeQueryTenantNamedParameterJdbcTemplateInterceptor} interface.
     * In your SQL query, you can then use the {@code :SCHEMA} placeholder, which
     * will be replaced by the value returned from the interceptor.</p>
     *
     * <p>Example SQL:</p>
     * <pre>{@code
     * SELECT * FROM :SCHEMA.USER;
     * }</pre>
     *
     * @return {@code true} to enable multi-tenant support, {@code false} otherwise.
     */
    boolean useTenant() default false;

}