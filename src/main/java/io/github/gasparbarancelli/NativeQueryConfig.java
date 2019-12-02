package io.github.gasparbarancelli;

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
            String packageScan = PropertyUtil.getValue("native-query.package-scan", "io.github.gasparbarancelli");
            Reflections reflections = new Reflections(packageScan);
            Set<Class<? extends NativeQuery>> nativeQueryList = reflections.getSubTypesOf(NativeQuery.class);
            NativeQueryRegistry nativeQueryRegistry = new NativeQueryRegistryImpl(beanDefinitionRegistry);
            nativeQueryRegistry.registry(nativeQueryList);
        };
    }

}
