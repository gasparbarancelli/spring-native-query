package io.github.gasparbarancelli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.beans.Introspector;
import java.util.Set;

public class NativeQueryRegistryImpl implements NativeQueryRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeQueryRegistryImpl.class);

    private final NativeQueryProxyFactory nativeQueryProxyFactory;

    private final BeanDefinitionRegistry registry;

    public NativeQueryRegistryImpl(BeanDefinitionRegistry registry) {
        this.nativeQueryProxyFactory = new NativeQueryProxyFactoryImpl();
        this.registry = registry;
    }

    @Override
    public void registry(Set<Class<? extends NativeQuery>> nativeQueryList) {
        for (Class<? extends NativeQuery> classe : nativeQueryList) {
            Object source = nativeQueryProxyFactory.create(classe);
            AbstractBeanDefinition beanDefinition = NativeQueryBeanDefinition.of(classe, source);
            String beanName = Introspector.decapitalize(classe.getSimpleName());
            LOGGER.debug("registering the bean {}", beanName);
            registry.registerBeanDefinition(beanName, beanDefinition);
        }
    }

}
