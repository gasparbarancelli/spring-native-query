package io.github.gasparbarancelli;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class NativeQueryStringUtilsTest {

    @Test
    void testCapitalize_NullOrEmpty() {
        assertNull(NativeQueryStringUtils.capitalize(null));
        assertEquals("", NativeQueryStringUtils.capitalize(""));
    }

    @Test
    void testCapitalize_Normal() {
        assertEquals("Test", NativeQueryStringUtils.capitalize("test"));
        assertEquals("Test", NativeQueryStringUtils.capitalize("Test"));
        assertEquals("T", NativeQueryStringUtils.capitalize("t"));
    }

}
