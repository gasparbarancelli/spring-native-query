package io.github.gasparbarancelli;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.LongType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class NativeQueryMethodInterceptorImpl implements NativeQueryMethodInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeQueryMethodInterceptorImpl.class);

    @Override
    public Object executeQuery(NativeQueryInfo info) {
        if (!info.isUseJdbcTemplate()) {
            return executeWithEntityManager(info);
        }
        return executeWithJdbcTemplate(info);
    }

    private Object executeWithJdbcTemplate(NativeQueryInfo info) {
        LOGGER.debug("SQL will be executed with JdbcTemplate");
        LOGGER.debug("getting the instance of the NamedParameterJdbcTemplate bean");
        NamedParameterJdbcTemplate jdbcTemplate = ApplicationContextProvider.getApplicationContext().getBean(NamedParameterJdbcTemplate.class);

        Map<String, Object> parametroList = new HashMap<>();
        for (NativeQueryParameter parameter : info.getParameterList()) {
            LOGGER.debug("checking if parameter {} exists in sql", parameter.getName());
            if (parameter.getValue() != null && info.getSql().contains(":" + parameter.getName())) {
                LOGGER.debug("parameter {} exists in SQL", parameter.getName());
                LOGGER.debug("parameter {} containing the value {} added to SQL", parameter.getName(), parameter.getValue().toString());
                parametroList.put(parameter.getName(), parameter.getValue());
            }
        }

        LOGGER.debug("instantiating a BeanPropertyRowMapper of type {}", info.getAliasToBean().getName());
        BeanPropertyRowMapper<?> beanPropertyRowMapper = new BeanPropertyRowMapper<>(info.getAliasToBean());
        if (info.getReturnType().getSimpleName().equals(Void.TYPE.getName())) {
            LOGGER.debug("running update");
            jdbcTemplate.update(info.getSql(), parametroList);
            return null;
        }

        if (info.isSingleResult()) {
            if (info.isJavaObject()) {
                LOGGER.debug("executing the query and returning an object of type {}", info.getAliasToBean().getName());
                return jdbcTemplate.queryForObject(info.getSql(), parametroList, info.getAliasToBean());
            }

            if (info.returnTypeIsOptional()) {
                LOGGER.debug("executing the query and returning an optional {}", info.getAliasToBean().getName());
                return getOptionalReturn(() -> jdbcTemplate.queryForObject(info.getSql(), parametroList, beanPropertyRowMapper));
            }

            LOGGER.debug("executing the query and returning an object of type {}", info.getAliasToBean().getName());
            return jdbcTemplate.queryForObject(info.getSql(), parametroList, beanPropertyRowMapper);
        }

        LOGGER.debug("executing the query and returning a list of objects of type {}", info.getAliasToBean().getName());
        if (info.isJavaObject()) {
            return jdbcTemplate.queryForList(info.getSql(), parametroList, info.getAliasToBean());
        }
        return jdbcTemplate.query(info.getSql(), parametroList, beanPropertyRowMapper);
    }

    private Object executeWithEntityManager(NativeQueryInfo info) {
        LOGGER.debug("SQL will be executed with EntityManager");
        LOGGER.debug("getting the instance of the EntityManager bean");
        EntityManager entityManager = ApplicationContextProvider.getApplicationContext().getBean(EntityManager.class);
        Session session = entityManager.unwrap(Session.class);
        NativeQuery<?> query;
        if (info.isEntity()) {
            LOGGER.debug("creating a native query with the entityManager and defining the return class {}", info.getAliasToBean().getName());
            query = session.createNativeQuery(info.getSql(), info.getAliasToBean());
        } else {
            LOGGER.debug("creating a native query with the entityManager");
            query = session.createNativeQuery(info.getSql());
        }

        addParameterJpa(query, info);

        if (info.hasPagination()) {
            LOGGER.debug("setting pagination, first {}, max {}", info.getFirstResult(), info.getMaxResult());
            query.setFirstResult(info.getFirstResult());
            query.setMaxResults(info.getMaxResult());
        }

        if (!info.isJavaObject() && !info.isEntity()) {
            if (info.isUseHibernateTypes()) {
                HibernateTypesMapper.map(query, info.getAliasToBean());
            }
            LOGGER.debug("invoking Hibernate ResultTransformer to convert the SQL query to an object of type {}", info.getAliasToBean().getName());
            query.setResultTransformer(Transformers.aliasToBean(info.getAliasToBean()));
        }
        if (info.getReturnType().getSimpleName().equals(Void.TYPE.getName())) {
            LOGGER.debug("running update");
            query.executeUpdate();
            return null;
        }

        if (info.returnTypeIsOptional()) {
            LOGGER.debug("executes the query returning an optional {}", info.getAliasToBean().getName());
            return getOptionalReturn(query::getSingleResult);
        }

        if (info.isSingleResult()) {
            LOGGER.debug("executes the query by returning an {} object", info.getAliasToBean().getName());
            return query.getSingleResult();
        }

        List<?> resultList = query.list();
        if (info.isPagination()) {
            LOGGER.debug("creating an object containing the pagination of the data returned in the query");
            return new PageImpl<>(resultList, info.getPageable(), getTotalRecords(info, session));
        }
        return resultList;
    }

    private Object getOptionalReturn(Supplier<Object> result) {
        try {
            return Optional.ofNullable(result.get());
        } catch (NoResultException | EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private Long getTotalRecords(NativeQueryInfo info, Session session) {
        LOGGER.debug("executing the query to obtain the number of records found to be used in the pagination");
        NativeQuery<?> query = session.createNativeQuery(info.getSqlTotalRecord());
        query.unwrap(NativeQuery.class).addScalar("totalRecords", LongType.INSTANCE);
        addParameterJpa(query, info);
        return (Long) query.getSingleResult();
    }

    private void addParameterJpa(NativeQuery<?> query, NativeQueryInfo info) {
        info.getParameterList().forEach(parameter -> {
            LOGGER.debug("checking if parameter {} exists in sql", parameter.getName());
            if (parameter.getValue() != null && info.getSql().contains(":" + parameter.getName())) {
                LOGGER.debug("parameter {} exists in SQL", parameter.getName());
                LOGGER.debug("parameter {} containing the value {} added to SQL", parameter.getName(), parameter.getValue().toString());
                query.setParameter(parameter.getName(), parameter.getValue());
            }
        });
    }

}
