package io.github.gasparbarancelli;

import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class HibernateTypesMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateTypesMapper.class);

    private static final Map<String, Map<String, Type>> CACHE = new HashMap<>();

    public static void map(NativeQuery<?> query, Class<?> dto) {
        Map<String, Type> map = CACHE.get(dto.getName());
        LOGGER.debug("hibernate types cache key {}", dto.getName());
        if (map == null) {
            LOGGER.debug("creating a cache for the fields of object {}", dto.getName());
            map = new HashMap<>();
            for (Field field : dto.getDeclaredFields()) {
                LOGGER.debug("getting the hibernate typing for field {} of object {}", field.getName(), dto.getName());
                Type hibernateType = getHibernateType(field.getType());
                LOGGER.debug("obtained type is {}", hibernateType.getName());
                map.put(field.getName(), hibernateType);
            }
            CACHE.put(dto.getName(), map);
        }

        map.forEach(query::addScalar);
    }

    private static Type getHibernateType(Class<?> fieldType) {
        switch (fieldType.getCanonicalName()) {
            case "java.lang.Integer": return StandardBasicTypes.INTEGER;
            case "java.lang.Long": return StandardBasicTypes.LONG;
            case "java.math.BigDecimal": return StandardBasicTypes.BIG_DECIMAL;
            case "java.lang.Float": return StandardBasicTypes.FLOAT;
            case "java.math.BigInteger": return StandardBasicTypes.BIG_INTEGER;
            case "java.lang.Short": return StandardBasicTypes.SHORT;
            case "java.lang.String": return StandardBasicTypes.STRING;
            case "java.lang.Boolean": return StandardBasicTypes.BOOLEAN;
            case "java.lang.Character": return StandardBasicTypes.CHARACTER;
            case "java.util.Date": return StandardBasicTypes.DATE;
            case "java.lang.Number": return StandardBasicTypes.DOUBLE;
        }
        return StandardBasicTypes.STRING;
    }

}
