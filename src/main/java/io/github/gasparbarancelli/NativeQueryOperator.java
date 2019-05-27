package io.github.gasparbarancelli;

import io.github.gasparbarancelli.transform.TransformParamContaining;
import io.github.gasparbarancelli.transform.TransformParamEndsWith;
import io.github.gasparbarancelli.transform.TransformParamStartsWith;
import lombok.Getter;

import java.util.function.Function;

public enum NativeQueryOperator {

    DEFAULT(value -> value),
    CONTAINING(new TransformParamContaining()),
    STARTS_WITH(new TransformParamStartsWith()),
    ENDS_WITH(new TransformParamEndsWith());

    @Getter
    private Function<Object, Object> transformParam;

    NativeQueryOperator(Function transformParam) {
        this.transformParam = transformParam;
    }

}
