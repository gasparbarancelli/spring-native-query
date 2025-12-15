package io.github.gasparbarancelli;

/**
 * An interface for intercepting and executing native queries.
 *
 * <p>Implementations of this interface are responsible for taking the metadata
 * provided in a {@link NativeQueryInfo} object and executing the corresponding
 * native SQL query. This allows for different query execution strategies, such as
 * using Hibernate's {@code EntityManager} or Spring's {@code JdbcTemplate}.</p>
 *
 * <p>The interceptor is a key component of the library's proxy-based approach,
 * as it is the final step in the chain of responsibility for handling a method
 * invocation on a {@link NativeQuery} interface.</p>
 *
 * @see NativeQueryMethodInterceptorImpl
 * @see NativeQueryInfo
 */
public interface NativeQueryMethodInterceptor {

    /**
     * Executes a native query based on the information provided.
     *
     * @param info The {@link NativeQueryInfo} object containing all the necessary
     *             metadata for the query execution.
     * @return The result of the query execution, which could be a single object,
     *         a list of objects, a {@code Page}, or an {@code Optional}.
     */
    Object executeQuery(NativeQueryInfo info);

}