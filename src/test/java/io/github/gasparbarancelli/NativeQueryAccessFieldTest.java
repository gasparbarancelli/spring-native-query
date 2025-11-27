package io.github.gasparbarancelli;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class NativeQueryAccessFieldTest {

    static class TestDto {
        public String name;
        @NativeQueryParam(value = "age")
        public Integer age;
    }

    @Test
    void testConstructor_WithoutAnnotation() throws Exception {
        Field field = TestDto.class.getField("name");
        NativeQueryAccessField accessField = new NativeQueryAccessField(field);

        assertEquals("Name", accessField.getName());
        assertEquals(String.class, accessField.getType());
        assertNull(accessField.getParam());
    }

    @Test
    void testConstructor_WithAnnotation() throws Exception {
        Field field = TestDto.class.getField("age");
        NativeQueryAccessField accessField = new NativeQueryAccessField(field);

        assertEquals("Age", accessField.getName());
        assertEquals(Integer.class, accessField.getType());
        assertNotNull(accessField.getParam());
    }
}
