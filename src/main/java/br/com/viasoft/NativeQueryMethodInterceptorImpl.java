package br.com.viasoft;

import org.hibernate.Session;
import org.hibernate.transform.Transformers;

import javax.persistence.EntityManager;

public class NativeQueryMethodInterceptorImpl implements NativeQueryMethodInterceptor {

    @Override
    public Object executeQuery(NativeQueryInfo info) {
        var entityManager = ApplicationContextProvider.getApplicationContext().getBean(EntityManager.class);
        var session = entityManager.unwrap(Session.class);
        var query = session.createNativeQuery(info.getSql());
        info.getParameterList().forEach(p -> query.setParameter(p.getName(), p.getValue()));
        if (info.hasPagination()) {
            query.setFirstResult(info.getFirstResult());
            query.setMaxResults(info.getMaxResult());
        }
        return query.setResultTransformer(Transformers.aliasToBean(info.getType())).list();
    }

}
