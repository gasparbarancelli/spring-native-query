package io.github.gasparbarancelli;

/**
 * A marker interface for repositories that use the Spring Native Query library.
 *
 * <p>This interface serves as a signal to the library's auto-configuration mechanism
 * that the implementing interface should be processed to generate a proxy-based
 * implementation for executing native SQL queries.</p>
 *
 * <p>By extending this interface, a repository gains the ability to define methods
 * that are automatically mapped to native SQL queries located in external files.
 * The library handles the boilerplate code for query execution, parameter binding,
 * and result mapping, allowing developers to focus on the SQL itself.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * import io.github.gasparbarancelli.NativeQuery;
 * import org.springframework.stereotype.Repository;
 *
 * @Repository
 * public interface SaleRepository extends NativeQuery {
 *     List<SaleFullResult> findSales();
 * }
 * }</pre>
 *
 * @see NativeQueryAutoConfiguration
 * @see NativeQueryProxyFactory
 */
public interface NativeQuery {

}