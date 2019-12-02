package io.github.gasparbarancelli;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class PropertyUtil {

    public static String getValue(String propertyName, String defaultValue) {
        try {
            Properties prop = new Properties();
            InputStream inputStream = getInputStream("application.properties");
            prop.load(inputStream);
            String value = prop.getProperty(propertyName);
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
                return defaultValue;
            }
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
