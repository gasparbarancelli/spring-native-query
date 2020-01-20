package io.github.gasparbarancelli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class PropertyUtil {

    private static final Map<String, String> cache = new HashMap<>();

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

    private static String getProperty(String propertyName,  String defaultValue) throws IOException {
        String value = getPropertyValue(propertyName);
        if (value == null || value.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            value = getYamlValue(propertyName, mapper);
        }
        if (value == null || value.isEmpty()) {
            value = defaultValue;
        }

        return value;
    }

    private static String getPropertyValue(InputStream inputStream, String propertyName) throws IOException {
        Properties prop = new Properties();
        prop.load(inputStream);
        return prop.getProperty(propertyName);
    }

    private static String getPropertyValue(String propertyName) throws IOException {
        for (String propertyFile : propertyFileList) {
            InputStream inputStreamYml = getInputStream(propertyFile);
            if (inputStreamYml != null) {
                String value = getPropertyValue(inputStreamYml, propertyName);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private static String getYamlValue(InputStream inputStreamYml, String propertyName, ObjectMapper mapper) throws java.io.IOException {
        Map<String, Object> obj = mapper.readValue(inputStreamYml, HashMap.class);
        Map<String, String> map = (HashMap<String, String>) obj.get("native-query");
        return map.get(propertyName.replace("native-query.", ""));
    }

    private static String getYamlValue(String propertyName, ObjectMapper mapper) throws java.io.IOException {
        for (String file : yamlFileList) {
            InputStream inputStreamYml = getInputStream(file);
            if (inputStreamYml != null) {
                String value = getYamlValue(inputStreamYml, propertyName, mapper);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private static InputStream getInputStream(String s) {
        return PropertyUtil.class
                .getClassLoader()
                .getResourceAsStream(s);
    }

}
