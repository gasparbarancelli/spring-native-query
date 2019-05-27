package io.github.gasparbarancelli;

import org.hibernate.Session;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.Transformers;
import org.hibernate.type.LongType;
import org.springframework.data.domain.PageImpl;

import javax.persistence.EntityManager;

public class NativeQueryMethodInterceptorImpl implements NativeQueryMethodInterceptor {

    @Override
    public Object executeQuery(NativeQueryInfo info) {
        var entityManager = ApplicationContextProvider.getApplicationContext().getBean(EntityManager.class);
        var session = entityManager.unwrap(Session.class);
        NativeQuery query;
        if (info.isEntity()) {
            query = session.createNativeQuery(info.getSql(), info.getAliasToBean());
        } else {
            query = session.createNativeQuery(info.getSql());
        }

        addParameter(query, info);

        if (info.hasPagination()) {
            query.setFirstResult(info.getFirstResult());
            query.setMaxResults(info.getMaxResult());
        }
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

        var resultList = query.list();
        if (info.isPagination()) {
            return new PageImpl(resultList, info.getPageable(), getTotalRecords(info, session));
        }
        return resultList;
    }

    private Long getTotalRecords(NativeQueryInfo info, Session session) {
        NativeQuery query = session.createNativeQuery(info.getSqlTotalRecord());
        query.unwrap(NativeQuery.class).addScalar("totalRecords", LongType.INSTANCE);
        addParameter(query, info);
        return (Long) query.getSingleResult();
    }

    private void addParameter(NativeQuery query, NativeQueryInfo info) {
        info.getParameterList().forEach(p -> {
            if (p.getValue() != null && info.getSql().contains(":" + p.getName())) {
                query.setParameter(p.getName(), p.getValue());
            }
        });
    }

}
