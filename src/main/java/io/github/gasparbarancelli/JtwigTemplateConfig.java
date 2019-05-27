package io.github.gasparbarancelli;

import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;

public class JtwigTemplateConfig {

    private static EnvironmentConfiguration configuration;

    public static EnvironmentConfiguration get() {
        if (configuration == null) {
            configuration = EnvironmentConfigurationBuilder
                    .configuration()
                    .parser()
                    .syntax()
                    .withStartCode("/*").withEndCode("*/")
                    .and()
                    .withoutTemplateCache()
                    .and()
                    .build();
        }
        return configuration;
    }
}
