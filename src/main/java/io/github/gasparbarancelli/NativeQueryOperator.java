package io.github.gasparbarancelli;

import io.github.gasparbarancelli.transform.TransformParamContaining;
import io.github.gasparbarancelli.transform.TransformParamEndsWith;
import io.github.gasparbarancelli.transform.TransformParamStartsWith;

import java.util.function.Function;

public enum NativeQueryOperator {

    DEFAULT(value -> value),
    CONTAINING(new TransformParamContaining()),
    STARTS_WITH(new TransformParamStartsWith()),
    ENDS_WITH(new TransformParamEndsWith());

    private Function<Object, Object> transformParam;

    NativeQueryOperator(Function<Object, Object> transformParam) {
        this.transformParam = transformParam;
    }

    public Function<Object, Object> getTransformParam() {
        return transformParam;
    }

}
