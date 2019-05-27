package io.github.gasparbarancelli;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

class NativeQueryBeanDefinition {

    private NativeQueryBeanDefinition() {}

    static AbstractBeanDefinition of(Class<? extends NativeQuery> classe, Object source) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(classe.getName());
        builder.getRawBeanDefinition().setSource(source);
        builder.setLazyInit(false);
        builder.setScope(BeanDefinition.SCOPE_SINGLETON);
        AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinition.setInstanceSupplier(() -> source);
        beanDefinition.setAttribute("factoryBeanObjectType", classe.getName());
        return beanDefinition;
    }

}
