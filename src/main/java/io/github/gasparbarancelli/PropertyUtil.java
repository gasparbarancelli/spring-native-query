package io.github.gasparbarancelli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyUtil {

    private static final Map<String, String> cache = new HashMap<>();

    public static String getValue(String propertyName, String defaultValue) {
        try {
            String value = cache.get(propertyName);
            if (value != null) {
                return value;
            }

            Properties prop = new Properties();
            InputStream inputStream = getInputStream("application.properties");
            prop.load(inputStream);
            value = prop.getProperty(propertyName);
            if (value == null || value.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                InputStream inputStreamYml = getInputStream("bootstrap.yaml");
                if (inputStreamYml == null) {
                    inputStreamYml = getInputStream("bootstrap.yml");
                }
                Map<String, Object> obj = mapper.readValue(inputStreamYml, HashMap.class);
                Map<String, String> map = (HashMap<String, String>) obj.get("native-query");
                value = map.get(propertyName.replace("native-query.", ""));
            }
            if (value == null || value.isEmpty()) {
                value = defaultValue;
            }
            cache.put(propertyName, value);
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static InputStream getInputStream(String s) {
        return PropertyUtil.class
                .getClassLoader()
                .getResourceAsStream(s);
    }

}
