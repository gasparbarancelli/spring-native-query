package io.github.gasparbarancelli;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Method;
import static org.junit.jupiter.api.Assertions.*;

class NativeQueryAccessMethodTest {

    static class Dummy {
        public String getName() { return ""; }
        @NativeQueryParam(value = "age")
        public int getAge() { return 0; }
        public boolean isActive() { return true; };
    }

    @Test
    void shouldInitializeFieldsCorrectly() throws Exception {
        Method method = Dummy.class.getMethod("getName");
        NativeQueryAccessMethod accessMethod = new NativeQueryAccessMethod(method);

        assertEquals(method, accessMethod.getMethod());
        assertEquals("Name", accessMethod.getName());
        assertEquals(String.class, accessMethod.getType());
        assertNull(accessMethod.getParam());
    }

    @Test
    void shouldReturnPrimitiveType() throws Exception {
        Method methodAge = Dummy.class.getMethod("getAge");
        NativeQueryAccessMethod accessMethodAge = new NativeQueryAccessMethod(methodAge);
        assertEquals(int.class, accessMethodAge.getType());

        Method methodActive = Dummy.class.getMethod("isActive");
        NativeQueryAccessMethod accessMethodActive = new NativeQueryAccessMethod(methodActive);
        assertEquals(boolean.class, accessMethodActive.getType());
    }

}
