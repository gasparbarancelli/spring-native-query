package io.github.gasparbarancelli;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class NativeQueryBeanDefinitionTest {

    private interface DummyNativeQuery extends NativeQuery {

    }

    @Test
    void testOf_CreatesBeanDefinitionWithExpectedAttributes() {
        Object source = new Object();
        AbstractBeanDefinition beanDefinition = NativeQueryBeanDefinition.of(DummyNativeQuery.class, source);

        assertEquals(DummyNativeQuery.class.getName(), beanDefinition.getAttribute("factoryBeanObjectType"));
        assertEquals(source, beanDefinition.getInstanceSupplier().get());
        assertEquals("singleton", beanDefinition.getScope());
        assertFalse(beanDefinition.isLazyInit());
        assertEquals(source, beanDefinition.getSource());
    }
}
