package io.github.gasparbarancelli;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.LongType;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

public class NativeQueryMethodInterceptorImpl implements NativeQueryMethodInterceptor {

    @Override
    public Object executeQuery(NativeQueryInfo info) {
        if (!info.isUseJdbcTemplate()) {
            return executeWithEntityManager(info);
        }
        return executeWithJdbcTemplate(info);
    }

    private Object executeWithJdbcTemplate(NativeQueryInfo info) {
        NamedParameterJdbcTemplate jdbcTemplate = ApplicationContextProvider.getApplicationContext().getBean(NamedParameterJdbcTemplate.class);

        Map<String, Object> parametroList = new HashMap<>();
        for (NativeQueryParameter parametro : info.getParameterList()) {
            if (parametro.getValue() != null && info.getSql().contains(":" + parametro.getName())) {
                parametroList.put(parametro.getName(), parametro.getValue());
            }
        }

        BeanPropertyRowMapper<?> beanPropertyRowMapper = new BeanPropertyRowMapper<>(info.getAliasToBean());
        if (info.getReturnType().getSimpleName().equals(Void.TYPE.getName())) {
            jdbcTemplate.update(info.getSql(), parametroList);
            return null;
        }

        if (info.isSingleResult()) {
            if (info.isJavaObject()) {
                return jdbcTemplate.queryForObject(info.getSql(), parametroList, info.getAliasToBean());
            }

            if (info.returnTypeIsOptional()) {
                return getOptionalReturn(() -> jdbcTemplate.queryForObject(info.getSql(), parametroList, beanPropertyRowMapper));
            }

            return jdbcTemplate.queryForObject(info.getSql(), parametroList, beanPropertyRowMapper);
        }

        if (info.isJavaObject()) {
            return jdbcTemplate.queryForList(info.getSql(), parametroList, info.getAliasToBean());
        }
        return jdbcTemplate.query(info.getSql(), parametroList, beanPropertyRowMapper);
    }

    private Object executeWithEntityManager(NativeQueryInfo info) {
        EntityManager entityManager = ApplicationContextProvider.getApplicationContext().getBean(EntityManager.class);
        Session session = entityManager.unwrap(Session.class);
        NativeQuery<?> query;
        if (info.isEntity()) {
            query = session.createNativeQuery(info.getSql(), info.getAliasToBean());
        } else {
            query = session.createNativeQuery(info.getSql());
        }

        addParameterJpa(query, info);

        if (info.hasPagination()) {
            query.setFirstResult(info.getFirstResult());
            query.setMaxResults(info.getMaxResult());
        }

        query.getQueryString();

        if (!info.isJavaObject() && !info.isEntity()) {
            if (info.isUseHibernateTypes()) {
                HibernateNumericTypesMapper.map(query, info.getAliasToBean());
            }
            query.setResultTransformer(Transformers.aliasToBean(info.getAliasToBean()));
        }
        if (info.getReturnType().getSimpleName().equals(Void.TYPE.getName())) {
            query.executeUpdate();
            return null;
        }

        if (info.returnTypeIsOptional()) {
            return getOptionalReturn(query::getSingleResult);
        }

        if (info.isSingleResult()) {
            return query.getSingleResult();
        }

        List<?> resultList = query.list();
        if (info.isPagination()) {
            return new PageImpl(resultList, info.getPageable(), getTotalRecords(info, session));
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
        NativeQuery<?> query = session.createNativeQuery(info.getSqlTotalRecord());
        query.unwrap(NativeQuery.class).addScalar("totalRecords", LongType.INSTANCE);
        addParameterJpa(query, info);
        return (Long) query.getSingleResult();
    }

    private void addParameterJpa(NativeQuery<?> query, NativeQueryInfo info) {
        info.getParameterList().forEach(p -> {
            if (p.getValue() != null && info.getSql().contains(":" + p.getName())) {
                query.setParameter(p.getName(), p.getValue());
            }
        });
    }

}
