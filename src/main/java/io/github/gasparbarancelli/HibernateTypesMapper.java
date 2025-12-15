package io.github.gasparbarancelli;

import org.hibernate.query.NativeQuery;
import org.hibernate.type.BasicTypeReference;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * A utility class for mapping DTO fields to Hibernate {@link BasicTypeReference}s.
 *
 * <p>This class provides a mechanism to automatically map the fields of a Data Transfer Object (DTO)
 * to their corresponding Hibernate types. This is particularly useful when working with native SQL
 * queries in Hibernate, as it allows for the explicit definition of scalar types in the query result,
 * ensuring correct type conversion.</p>
 *
 * <p>The mappings are cached to improve performance, avoiding the need to re-calculate the types
 * for the same DTO class multiple times.</p>
 *
 * @see NativeQuery#addScalar(String, org.hibernate.type.Type)
 */
public class HibernateTypesMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateTypesMapper.class);

    private static final Map<String, Map<String, BasicTypeReference<?>>> CACHE = new HashMap<>();

    /**
     * Maps the fields of a DTO class to their corresponding Hibernate types and adds them as scalars to a native query.
     *
     * <p>This method inspects the fields of the given DTO class, determines their Hibernate types,
     * and then calls {@link NativeQuery#addScalar(String, org.hibernate.type.Type)} for each field.
     * The type mappings are cached to avoid redundant reflection and type resolution.</p>
     *
     * @param query The native query to which the scalar mappings will be added.
     * @param dto   The DTO class whose fields will be mapped.
     */
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

    /**
     * Returns the Hibernate {@link BasicTypeReference} for a given Java class.
     *
     * <p>This method provides a mapping from common Java types to their corresponding Hibernate
     * standard basic types. If a type is not explicitly mapped, it defaults to {@link StandardBasicTypes#STRING}.</p>
     *
     * @param fieldType The Java class to be mapped.
     * @return The corresponding Hibernate type.
     */
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