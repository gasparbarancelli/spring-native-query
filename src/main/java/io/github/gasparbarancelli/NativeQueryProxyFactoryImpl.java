package io.github.gasparbarancelli;

import org.aopalliance.intercept.MethodInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;

/**
 * The default implementation of {@link NativeQueryProxyFactory}.
 *
 * <p>This class uses Spring's {@link ProxyFactory} to create proxy instances for
 * {@link NativeQuery} interfaces. The created proxy is advised with a
 * {@link MethodInterceptor} that intercepts method calls, gathers query information,
 * and delegates the execution to a {@link NativeQueryMethodInterceptor}.</p>
 *
 * @see NativeQueryProxyFactory
 * @see ProxyFactory
 * @see NativeQueryMethodInterceptor
 */
public class NativeQueryProxyFactoryImpl implements NativeQueryProxyFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeQueryProxyFactoryImpl.class);

    private final NativeQueryMethodInterceptor nativeQueryMethodInterceptor;

    /**
     * Constructs a new {@code NativeQueryProxyFactoryImpl} with a default method interceptor.
     */
    public NativeQueryProxyFactoryImpl() {
        this.nativeQueryMethodInterceptor = new NativeQueryMethodInterceptorImpl();
    }

    @Override
    public Object create(Class<? extends NativeQuery> classe) {
        LOGGER.debug("creating an {} interface proxy", classe.getName());
        ProxyFactory proxy = new ProxyFactory();
        proxy.setTarget(classe);
        proxy.setInterfaces(classe, NativeQuery.class);
        proxy.addAdvice((MethodInterceptor) invocation -> {
            if ("toString".equals(invocation.getMethod().getName())) {
                return "NativeQuery Implementation";
            }
            LOGGER.debug("intercepting the call of method {} of class {}", invocation.getMethod().getName(), classe.getName());
            NativeQueryInfo info = NativeQueryCache.get(classe, invocation);
            return nativeQueryMethodInterceptor.executeQuery(info);
        });
        return proxy.getProxy(classe.getClassLoader());
    }

}