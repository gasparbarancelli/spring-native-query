package io.github.gasparbarancelli;

/**
 * An interface for providing tenant-specific information in a multi-tenant environment.
 *
 * <p>When using the Spring Native Query library in a multi-tenant application, you may
 * need to dynamically replace a schema placeholder in your SQL queries with the
 * current tenant's schema. This interface allows you to do that.</p>
 *
 * <p>To use it, create a Spring bean that implements this interface and provides the
 * logic for retrieving the current tenant's schema name. The library will then
 * automatically replace the {@code :SCHEMA} placeholder in your queries with the
 * value returned by the {@link #getTenant()} method.</p>
 *
 * <p>This feature is only active when using {@link NativeQueryUseJdbcTemplate} with
 * the {@code useTenant} attribute set to {@code true}.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Component
 * public class MyTenantInterceptor implements NativeQueryTenantNamedParameterJdbcTemplateInterceptor {
 *
 *     @Override
 *     public String getTenant() {
 *         // Logic to get the current tenant's schema
 *         return TenantContext.getCurrentTenantSchema();
 *     }
 * }
 * }</pre>
 *
 * @see NativeQueryUseJdbcTemplate
 */
public interface NativeQueryTenantNamedParameterJdbcTemplateInterceptor {

    /**
     * Returns the name of the current tenant's schema.
     *
     * @return The schema name.
     */
    String getTenant();

}