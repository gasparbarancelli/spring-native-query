package io.github.gasparbarancelli;

import java.util.Set;

/**
 * An interface for registering {@link NativeQuery} interfaces with the Spring application context.
 *
 * <p>Implementations of this interface are responsible for taking a set of {@code NativeQuery}
 * interface classes, creating proxy-based bean definitions for them, and registering
 * those definitions with the Spring bean registry. This is a key part of the
 * auto-configuration process that makes the native query repositories available for
 * dependency injection.</p>
 *
 * @see NativeQueryRegistryImpl
 * @see NativeQueryAutoConfiguration
 */
public interface NativeQueryRegistry {

    /**
     * Registers a set of {@link NativeQuery} interfaces.
     *
     * @param nativeQueryList A set of classes that extend {@code NativeQuery}.
     */
    void registry(Set<Class<? extends NativeQuery>> nativeQueryList);

}