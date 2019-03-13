package br.com.viasoft;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.reflections.Reflections;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Configuration
public class NativeQueryConfig {

    @Bean
    public BeanFactoryPostProcessor beanFactoryPostProcessor() {
        return bf -> {
            BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) bf;
            Reflections reflections = new Reflections(getPackageScan());
            Set<Class<? extends NativeQuery>> nimitzNativeQueryList = reflections.getSubTypesOf(NativeQuery.class);
            NativeQueryRegistry nativeQueryRegistry = new NativeQueryRegistryImpl(beanDefinitionRegistry);
            nativeQueryRegistry.registry(nimitzNativeQueryList);
        };
    }

    public String getPackageScan() {
        try {
            Properties prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("application.properties");
            prop.load(inputStream);
            String packageScan = prop.getProperty("native-query.package-scan");
            if (packageScan == null || packageScan.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                InputStream inputStreamYml = this.getClass()
                        .getClassLoader()
                        .getResourceAsStream("bootstrap.yaml");
                if (inputStreamYml == null) {
                    inputStreamYml = this.getClass()
                            .getClassLoader()
                            .getResourceAsStream("bootstrap.yml");
                }
                Map<String, Object> obj = mapper.readValue(inputStreamYml, HashMap.class);
                Map<String, String> map = (HashMap<String, String>) obj.get("native-query");
                packageScan = map.get("package-scan");
            }
            if (packageScan == null || packageScan.isEmpty()) {
                return "br.com.viasoft";
            }
            return packageScan;
        } catch (Exception e) {
            return "br.com.viasoft";
        }
    }
}
