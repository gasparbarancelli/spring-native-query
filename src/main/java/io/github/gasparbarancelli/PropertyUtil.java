package io.github.gasparbarancelli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.reflections.Reflections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class PropertyUtil {

    private static final Map<String, String> cache = new HashMap<>();

    private static final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

    private static final List<String> yamlFileList = Arrays.asList(
            "application.yaml", "application.yml", "bootstrap.yaml", "bootstrap.yml"
    );

    private static final List<String> propertyFileList = Arrays.asList(
            "application.properties", "bootstrap.properties"
    );

    public static String getValue(String propertyName, String defaultValue) {
        try {
            String value = cache.get(propertyName);
            if (value != null) {
                return value;
            }

            value = getProperty(propertyName, defaultValue);

            cache.put(propertyName, value);
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static Optional<String> getPropertyByConfig(String propertyName) {
        String mainClass = System.getProperty("sun.java.command");
        String packageMainClass = mainClass.substring(0, mainClass.lastIndexOf("."));
        Reflections reflections = new Reflections(packageMainClass);
        Set<Class<? extends NativeQueryConfig>> subTypesOfNativeQueryConfig = reflections.getSubTypesOf(NativeQueryConfig.class);
        for (Class<? extends NativeQueryConfig> subType : subTypesOfNativeQueryConfig) {
            try {
                NativeQueryConfig config = (NativeQueryConfig) subType.getConstructors()[0].newInstance();

                switch (propertyName) {
                    case "native-query.package-scan":
                        return Optional.ofNullable(config.getPackageScan());
                    case "native-query.sql.directory":
                        return Optional.ofNullable(config.getSQLDirectory());
                    case "native-query.use-hibernate-types":
                        return Optional.of(String.valueOf(config.getUseHibernateTypes()));
                    default:
                        return Optional.ofNullable(config.getFileSufix());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return Optional.empty();
    }

    private static String getProperty(String propertyName, String defaultValue) throws IOException {
        return getPropertyValue(propertyName).orElseGet(() -> getYamlValue(propertyName)
                        .orElseGet(() -> getPropertyByConfig(propertyName)
                                .orElse(defaultValue)));
    }

    private static Optional<String> getPropertyValue(InputStream inputStream, String propertyName) throws IOException {
        Properties prop = new Properties();
        prop.load(inputStream);
        return Optional.ofNullable(prop.getProperty(propertyName));
    }

    private static Optional<String> getPropertyValue(String propertyName) throws IOException {
        for (String propertyFile : propertyFileList) {
            Optional<InputStream> inputStreamYml = getInputStream(propertyFile);
            if (inputStreamYml.isPresent()) {
                Optional<String> value = getPropertyValue(inputStreamYml.get(), propertyName);
                if (value.isPresent()) {
                    return value;
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<String> getYamlValue(InputStream inputStreamYml, String propertyName) {
        try {
            Map<String, Object> obj = mapper.readValue(inputStreamYml, HashMap.class);
            Map<String, String> map = (HashMap<String, String>) obj.get("native-query");
            if (map != null) {
                return Optional.ofNullable(map.get(propertyName.replace("native-query.", "")));
            }
            return Optional.empty();
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static Optional<String> getYamlValue(String propertyName) {
        for (String file : yamlFileList) {
            Optional<InputStream> inputStreamYml = getInputStream(file);
            if (inputStreamYml.isPresent()) {
                Optional<String> value = getYamlValue(inputStreamYml.get(), propertyName);
                if (value.isPresent()) {
                    return value;
                }
            }
        }
        return Optional.empty();
    }

    private static Optional<InputStream> getInputStream(String s) {
        InputStream inputStream = PropertyUtil.class
                .getClassLoader()
                .getResourceAsStream(s);
        return Optional.ofNullable(inputStream);
    }

}
