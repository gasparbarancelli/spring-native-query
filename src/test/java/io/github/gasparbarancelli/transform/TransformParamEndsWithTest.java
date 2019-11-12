package io.github.gasparbarancelli.transform;

import org.junit.Assert;
import org.junit.Test;

import java.util.function.Function;

public class TransformParamEndsWithTest {

    private Function<Object, Object> transform = new TransformParamEndsWith();

    @Test
    public void applyNullable() {
        Object result = transform.apply(null);
        Assert.assertNull(result);
    }

    @Test
    public void applyEmpty() {
        Object result = transform.apply("");
        Assert.assertNull(result);
    }

    @Test
    public void applyWithValue() {
        Object result = transform.apply("test");
        Assert.assertEquals("%test", result);
    }

}