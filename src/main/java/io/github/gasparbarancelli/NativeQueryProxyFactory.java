package io.github.gasparbarancelli;

/**
 * An interface for creating proxy instances of {@link NativeQuery} interfaces.
 *
 * <p>Implementations of this interface are responsible for generating a proxy class
 * that implements a given {@code NativeQuery} interface. This proxy will intercept
 * method calls and delegate them to a {@link NativeQueryMethodInterceptor} for
 * execution.</p>
 *
 * <p>This is a core component of the library's architecture, enabling the creation
 * of dynamic, implementation-less repositories.</p>
 *
 * @see NativeQueryProxyFactoryImpl
 * @see NativeQuery
 */
public interface NativeQueryProxyFactory {

    /**
     * Creates a proxy instance for a given {@link NativeQuery} interface.
     *
     * @param classe The {@code NativeQuery} interface to be proxied.
     * @return A proxy object that implements the interface.
     */
    Object create(Class<? extends NativeQuery> classe);

}