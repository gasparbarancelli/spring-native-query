package io.github.gasparbarancelli;

import java.lang.reflect.Field;

import org.hibernate.query.NativeQuery;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class HibernateTypesMapper {

    public static void map(NativeQuery<?> query, Class<?> dto) {
        for (Field field : dto.getDeclaredFields()) {
            Type hibernateType = getHibernateType(field.getType());
            if (hibernateType != null) {
                query.addScalar(field.getName(), hibernateType);
            }
        }
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
//            case "java.lang.Number": return StandardBasicTypes.;
        }
        return null;
    }

    public static void main(String[] args) {
        System.out.println(Integer.class.getCanonicalName());
    }
}
