package br.com.viasoft;

import br.com.viasoft.transform.TransformParamContaining;
import br.com.viasoft.transform.TransformParamEndsWith;
import br.com.viasoft.transform.TransformParamStartsWith;
import lombok.Getter;

import java.util.function.Function;

public enum NativeQueryOperator {

    EQUAL(value -> value),
    CONTAINING(new TransformParamContaining()),
    STARTS_WITH(new TransformParamStartsWith()),
    ENDS_WITH(new TransformParamEndsWith());

    @Getter
    private Function<Object, Object> transformParam;

    NativeQueryOperator(Function transformParam) {
        this.transformParam = transformParam;
    }

}
