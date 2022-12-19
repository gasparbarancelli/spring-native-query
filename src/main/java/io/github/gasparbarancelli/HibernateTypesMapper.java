package io.github.gasparbarancelli;

import org.hibernate.query.NativeQuery;
import org.hibernate.type.BasicTypeReference;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class HibernateTypesMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateTypesMapper.class);

    private static final Map<String, Map<String, BasicTypeReference<?>>> CACHE = new HashMap<>();

    public static void map(NativeQuery<?> query, Class<?> dto) {
        Map<String, BasicTypeReference<?>> map = CACHE.get(dto.getName());
        LOGGER.debug("hibernate types cache key {}", dto.getName());
        if (map == null) {
            LOGGER.debug("creating a cache for the fields of object {}", dto.getName());
            map = new HashMap<>();
            for (Field field : dto.getDeclaredFields()) {
                LOGGER.debug("getting the hibernate typing for field {} of object {}", field.getName(), dto.getName());
                BasicTypeReference<?> hibernateType = getHibernateType(field.getType());
                LOGGER.debug("obtained type is {}", hibernateType.getName());
                map.put(field.getName(), hibernateType);
            }
            CACHE.put(dto.getName(), map);
        }

        map.forEach(query::addScalar);
    }

    private static BasicTypeReference<?> getHibernateType(Class<?> fieldType) {
        return switch (fieldType.getCanonicalName()) {
            case "java.lang.Integer" -> StandardBasicTypes.INTEGER;
            case "java.lang.Long" -> StandardBasicTypes.LONG;
            case "java.math.BigDecimal" -> StandardBasicTypes.BIG_DECIMAL;
            case "java.lang.Float" -> StandardBasicTypes.FLOAT;
            case "java.math.BigInteger" -> StandardBasicTypes.BIG_INTEGER;
            case "java.lang.Short" -> StandardBasicTypes.SHORT;
            case "java.lang.Boolean" -> StandardBasicTypes.BOOLEAN;
            case "java.lang.Character" -> StandardBasicTypes.CHARACTER;
            case "java.util.Date" -> StandardBasicTypes.DATE;
            case "java.lang.Number" -> StandardBasicTypes.DOUBLE;
            default -> StandardBasicTypes.STRING;
        };
    }

}
