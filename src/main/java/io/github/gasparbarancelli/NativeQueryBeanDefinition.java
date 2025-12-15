package io.github.gasparbarancelli;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

/**
 * A factory for creating {@link AbstractBeanDefinition}s for {@link NativeQuery} interfaces.
 *
 * <p>This class provides a static method to construct a bean definition for a given
 * {@code NativeQuery} interface. The created bean definition is configured to be a
 * singleton and is initialized with a supplier that provides the actual proxy instance.</p>
 *
 * <p>This is a crucial part of the library's auto-configuration process, as it allows
 * for the dynamic registration of beans that are not concrete classes but rather
 * proxy-based implementations of interfaces.</p>
 *
 * @see NativeQueryRegistry
 * @see BeanDefinitionBuilder
 */
class NativeQueryBeanDefinition {

    private NativeQueryBeanDefinition() {
        // Private constructor to prevent instantiation
    }

    /**
     * Creates an {@link AbstractBeanDefinition} for a {@link NativeQuery} interface.
     *
     * <p>The bean definition is configured with the following properties:</p>
     * <ul>
     *   <li>The bean is a singleton.</li>
     *   <li>The bean is not lazy-initialized.</li>
     *   <li>The bean instance is provided by a supplier (the proxy factory).</li>
     * </ul>
     *
     * @param classe The {@code NativeQuery} interface class for which the bean definition is being created.
     * @param source The proxy instance that will be supplied as the bean.
     * @return The configured {@code AbstractBeanDefinition}.
     */
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