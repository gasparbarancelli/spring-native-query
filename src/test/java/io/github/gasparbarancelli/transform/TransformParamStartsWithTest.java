package io.github.gasparbarancelli.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

public class TransformParamStartsWithTest {

    private final Function<Object, Object> transform = new TransformParamStartsWith();

    @Test
    public void applyNullable() {
        Object result = transform.apply(null);
        Assertions.assertNull(result);
    }

    @Test
    public void applyEmpty() {
        Object result = transform.apply("");
        Assertions.assertNull(result);
    }

    @Test
    public void applyWithValue() {
        Object result = transform.apply("test");
        Assertions.assertEquals("test%", result);
    }

}