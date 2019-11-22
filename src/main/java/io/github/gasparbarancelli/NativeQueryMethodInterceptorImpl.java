package io.github.gasparbarancelli;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.LongType;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.persistence.EntityManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NativeQueryMethodInterceptorImpl implements NativeQueryMethodInterceptor {

    @Override
    public Object executeQuery(NativeQueryInfo info) {
        if (!info.isUseJdbcTemplate()) {
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
                query.setResultTransformer(Transformers.aliasToBean(info.getAliasToBean()));
            }
            if (info.getReturnType().getSimpleName().equals(Void.TYPE.getName())) {
                query.executeUpdate();
                return null;
            }
            if (info.isSingleResult()) {
                return query.getSingleResult();
            }

            List<?> resultList = query.list();
            if (info.isPagination()) {
                return new PageImpl(resultList, info.getPageable(), getTotalRecords(info, session));
            }
            return resultList;
        } else {
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
                return jdbcTemplate.queryForObject(info.getSql(), parametroList, beanPropertyRowMapper);
            }

            if (info.isJavaObject()) {
                return jdbcTemplate.queryForList(info.getSql(), parametroList, info.getAliasToBean());
            }
            return jdbcTemplate.query(info.getSql(), parametroList, beanPropertyRowMapper);
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
