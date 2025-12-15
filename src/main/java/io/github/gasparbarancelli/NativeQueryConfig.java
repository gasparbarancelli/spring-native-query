package io.github.gasparbarancelli;

/**
 * An interface for programmatically configuring the Spring Native Query library.
 *
 * <p>By implementing this interface and registering it as a Spring bean, developers can
 * provide configuration values that override the default settings and those specified
 * in {@code application.properties}. This is useful for applications that require
 * more complex or dynamic configuration.</p>
 *
 * <p>Example implementation:</p>
 * <pre>{@code
 * import io.github.gasparbarancelli.NativeQueryConfig;
 * import org.springframework.context.annotation.Configuration;
 *
 * @Configuration
 * public class MyNativeQueryConfig implements NativeQueryConfig {
 *
 *     @Override
 *     public String getPackageScan() {
 *         return "com.myapp.repository";
 *     }
 *
 *     @Override
 *     public String getSQLDirectory() {
 *         return "queries/sql";
 *     }
 * }
 * }</pre>
 *
 * @see NativeQueryAutoConfiguration
 */
public interface NativeQueryConfig {

    /**
     * Returns the package to be scanned for interfaces that extend {@link NativeQuery}.
     *
     * @return The package name.
     */
    String getPackageScan();

    /**
     * Returns the directory where SQL files are located.
     *
     * @return The SQL file directory path.
     */
    String getSQLDirectory();

}