package br.com.viasoft;

import org.aopalliance.intercept.MethodInterceptor;
import org.mockito.Mockito;
import org.springframework.aop.framework.ProxyFactory;

public class NativeQueryProxyFactoryImpl implements NativeQueryProxyFactory {

    private final NativeQueryMethodInterceptor nativeQueryMethodInterceptor;

    public NativeQueryProxyFactoryImpl() {
        this.nativeQueryMethodInterceptor = new NativeQueryMethodInterceptorImpl();
    }

    @Override
    public Object create(Class<? extends NativeQuery> classe) {
        ProxyFactory proxy = new ProxyFactory();
        proxy.setTarget(Mockito.mock(classe));
        proxy.setInterfaces(classe, NativeQuery.class);
        proxy.addAdvice((MethodInterceptor) invocation -> {
            if ("toString".equals(invocation.getMethod().getName())) {
                return "NativeQuery Implementation";
            }
            var info = NativeQueryInfo.of(classe, invocation);
            return nativeQueryMethodInterceptor.executeQuery(info);
        });
        return proxy.getProxy(classe.getClassLoader());
    }

}
