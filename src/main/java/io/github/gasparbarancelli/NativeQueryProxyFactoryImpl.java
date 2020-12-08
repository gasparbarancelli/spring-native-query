package io.github.gasparbarancelli;

import org.aopalliance.intercept.MethodInterceptor;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NativeQueryProxyFactoryImpl implements NativeQueryProxyFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeQueryProxyFactoryImpl.class);

    private final NativeQueryMethodInterceptor nativeQueryMethodInterceptor;

    public NativeQueryProxyFactoryImpl() {
        this.nativeQueryMethodInterceptor = new NativeQueryMethodInterceptorImpl();
    }

    private static class CacheKey {

        String className;

        String methodName;

        public CacheKey(String className, String methodName) {
            this.className = className;
            this.methodName = methodName;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return Objects.equals(className, cacheKey.className) &&
                    Objects.equals(methodName, cacheKey.methodName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(className, methodName);
        }
    }

    private static final Map<CacheKey, NativeQueryInfo> cache = new HashMap<>();

    @Override
    public Object create(Class<? extends NativeQuery> classe) {
        LOGGER.debug("creating an {} interface proxy", classe.getName());
        ProxyFactory proxy = new ProxyFactory();
        proxy.setTarget(Mockito.mock(classe));
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
