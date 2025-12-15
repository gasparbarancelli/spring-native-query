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

/**
 * Auto-configuration class for the Spring Native Query library.
 *
 * <p>This class is responsible for scanning the classpath for interfaces that extend
 * {@link NativeQuery}, and for each one, registering a bean definition that will
 * create a proxy-based implementation. This process is triggered by the Spring
 * Boot auto-configuration mechanism.</p>
 *
 * <p>The configuration allows customization of the package to be scanned for
 * {@code NativeQuery} interfaces and the directory where SQL files are located.
 * These settings can be provided via {@code application.properties} or a
 * {@link NativeQueryConfig} bean.</p>
 *
 * @see NativeQuery
 * @see BeanFactoryPostProcessor
 * @see NativeQueryRegistry
 */
@Configuration
public class NativeQueryAutoConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeQueryAutoConfiguration.class);

    private static String sqlDirectory;

    /**
     * Returns the directory where SQL files are located.
     *
     * @return The SQL file directory path.
     */
    public static String getSqlDirectory() {
        return sqlDirectory;
    }

    /**
     * Creates a {@link BeanFactoryPostProcessor} that scans for and registers {@link NativeQuery} interfaces.
     *
     * <p>This method defines the core logic of the auto-configuration. It scans the configured
     * package for interfaces extending {@link NativeQuery}, and for each one found, it
     * registers a bean definition that will create a proxy instance. The proxy will handle
     * the execution of native queries.</p>
     *
     * @param propertyPackageScan The package to scan, configured via the {@code native-query.package-scan} property.
     * @param sqlDirectory        The directory containing SQL files, configured via the {@code native-query.sql.directory} property.
     * @param nativeQueryConfig   An optional {@link NativeQueryConfig} bean for programmatic configuration.
     * @return A {@code BeanFactoryPostProcessor} that performs the scanning and registration.
     */
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