package io.github.gasparbarancelli;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.beans.Introspector;
import java.util.Set;

public class NativeQueryRegistryImpl implements NativeQueryRegistry {

    private final NativeQueryProxyFactory nativeQueryProxyFactory;

    private BeanDefinitionRegistry registry;

    public NativeQueryRegistryImpl(BeanDefinitionRegistry registry) {
        this.nativeQueryProxyFactory = new NativeQueryProxyFactoryImpl();
        this.registry = registry;
    }

    @Override
    public void registry(Set<Class<? extends NativeQuery>> nimitzNativeQueryList) {
        for (Class<? extends NativeQuery> classe : nimitzNativeQueryList) {
            var source = nativeQueryProxyFactory.create(classe);
            var beanDefinition = NativeQueryBeanDefinition.of(classe, source);
            var beanName = Introspector.decapitalize(classe.getSimpleName());
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
    }

}
