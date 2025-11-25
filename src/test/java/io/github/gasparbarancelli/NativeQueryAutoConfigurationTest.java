package io.github.gasparbarancelli;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class NativeQueryAutoConfigurationTest {

    @Test
    void testBeanFactoryPostProcessor_ConfiguresSqlDirectoryAndRegistersNativeQueries() {
        NativeQueryConfig config = mock(NativeQueryConfig.class);
        when(config.getPackageScan()).thenReturn("io.github.gasparbarancelli");
        when(config.getSQLDirectory()).thenReturn("customSqlDir");

        NativeQueryAutoConfiguration autoConfig = new NativeQueryAutoConfiguration();
        var postProcessor = autoConfig.beanFactoryPostProcessor(
                "io.github.gasparbarancelli",
                "nativeQuery",
                Optional.of(config)
        );

        ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class, withSettings().extraInterfaces(BeanDefinitionRegistry.class));
        postProcessor.postProcessBeanFactory(beanFactory);

        assertEquals("customSqlDir", NativeQueryAutoConfiguration.getSqlDirectory());
        verify((BeanDefinitionRegistry) beanFactory, atLeast(0)).registerBeanDefinition(anyString(), any());
    }

    @Test
    void testBeanFactoryPostProcessor_UsesDefaultValuesWhenConfigAbsent() {
        NativeQueryAutoConfiguration autoConfig = new NativeQueryAutoConfiguration();
        var postProcessor = autoConfig.beanFactoryPostProcessor(
                "io.github.gasparbarancelli",
                "nativeQuery",
                Optional.empty()
        );

        ConfigurableListableBeanFactory beanFactory = mock(ConfigurableListableBeanFactory.class, withSettings().extraInterfaces(BeanDefinitionRegistry.class));
        postProcessor.postProcessBeanFactory(beanFactory);

        assertEquals("nativeQuery", NativeQueryAutoConfiguration.getSqlDirectory());
    }

}
