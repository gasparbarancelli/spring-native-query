package io.github.gasparbarancelli;

import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class HibernateNumericTypesMapper {

    private static final Map<String, Map<String, Type>> CACHE = new HashMap<>();

    public static void map(NativeQuery<?> query, Class<?> dto) {
        Map<String, Type> map = CACHE.get(dto.getName());
        if (map == null) {
            map = new HashMap<>();
            for (Field field : dto.getDeclaredFields()) {
                Type hibernateType = getHibernateType(field.getType());
                if (hibernateType != null) {
                    map.put(field.getName(), hibernateType);
                }
            }
            CACHE.put(dto.getName(), map);
        }

        map.forEach(query::addScalar);
    }

    private static Type getHibernateType(Class<?> fieldType) {
        if (fieldType.isAssignableFrom(Integer.class)) {
            return StandardBasicTypes.INTEGER;
        } else if (fieldType.isAssignableFrom(String.class)) {
            return StandardBasicTypes.STRING;
        } else if (fieldType.isAssignableFrom(Long.class)) {
            return StandardBasicTypes.LONG;
        } else if (fieldType.isAssignableFrom(Double.class)) {
            return StandardBasicTypes.DOUBLE;
        } else if (fieldType.isAssignableFrom(BigDecimal.class)) {
            return StandardBasicTypes.BIG_DECIMAL;
        } else if (fieldType.isAssignableFrom(Float.class)) {
            return StandardBasicTypes.FLOAT;
        } else if (fieldType.isAssignableFrom(BigInteger.class)) {
            return StandardBasicTypes.BIG_INTEGER;
        } else if (fieldType.isAssignableFrom(Short.class)) {
            return StandardBasicTypes.SHORT;
        }
        return null;
    }
}
