package io.github.gasparbarancelli;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;

import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class HibernateNumericTypesMapper {

    public static void map(NativeQuery<?> query, Class<?> dto) {
        for (Field field : dto.getDeclaredFields()) {
            Type hibernateType = getHibernateType(field.getType());
            if (hibernateType != null) {
                query.addScalar(field.getName(), hibernateType);
            }
        }
    }

    private static Type getHibernateType(Class<?> fieldType) {
        if (fieldType.isAssignableFrom(Integer.class)) {
            return StandardBasicTypes.INTEGER;
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
