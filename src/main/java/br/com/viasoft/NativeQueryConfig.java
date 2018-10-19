package br.com.viasoft;

import org.reflections.Reflections;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class NativeQueryConfig {

    @Bean
    public BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return bf -> {
            BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) bf;
            Reflections reflections = new Reflections("br.com.viasoft");
            Set<Class<? extends NativeQuery>> nimitzNativeQueryList = reflections.getSubTypesOf(NativeQuery.class);
            NativeQueryRegistry nativeQueryRegistry = new NativeQueryRegistryImpl(beanDefinitionRegistry);
            nativeQueryRegistry.registry(nimitzNativeQueryList);
        };
    }

}
