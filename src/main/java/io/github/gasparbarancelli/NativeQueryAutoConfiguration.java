package io.github.gasparbarancelli;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.Set;

@Configuration
public class NativeQueryAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeQueryAutoConfiguration.class);

    private static String sqlDirectory;

    public static String getSqlDirectory() {
        return sqlDirectory;
    }

    @Bean
    public BeanFactoryPostProcessor beanFactoryPostProcessor(
            @Value("${native-query.package-scan:io.github.gasparbarancelli}") String propertyPackageScan,
            @Value("${native-query.sql.directory:nativeQuery}") String sqlDirectory,
            Optional<NativeQueryConfig> nativeQueryConfig
    ) {
        var packageScan = nativeQueryConfig.map(NativeQueryConfig::getPackageScan)
                .orElse(propertyPackageScan);

        NativeQueryAutoConfiguration.sqlDirectory = nativeQueryConfig.map(NativeQueryConfig::getSQLDirectory)
                .orElse(sqlDirectory);

        return bf -> {
            LOGGER.debug("starting configuration");
            BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) bf;
            LOGGER.debug("packageScan {}", packageScan);
            Reflections reflections = new Reflections(packageScan);
            LOGGER.debug("looking for interfaces that implement NativeQuery");
            Set<Class<? extends NativeQuery>> nativeQueryList = reflections.getSubTypesOf(NativeQuery.class);
            LOGGER.debug("{} found interfaces", nativeQueryList.size());
            NativeQueryRegistry nativeQueryRegistry = new NativeQueryRegistryImpl(beanDefinitionRegistry);
            nativeQueryRegistry.registry(nativeQueryList);
        };
    }

}
