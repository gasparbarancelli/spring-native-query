package io.github.gasparbarancelli.transform;

import java.util.function.Function;

public class TransformParamContaining extends TransformParamString implements Function<Object, Object> {

    @Override
    public Object apply(Object o) {
        return executeWhenValueIsNotBlank(o, o1 -> "%" + o1.toString() + "%");
    }

}
