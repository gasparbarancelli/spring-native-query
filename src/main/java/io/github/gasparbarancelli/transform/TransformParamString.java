package io.github.gasparbarancelli.transform;

import java.util.function.Function;

abstract class TransformParamString {

    Object executeWhenValueIsNotBlank(Object value, Function<Object, Object> function) {
        if (value != null && !value.toString().trim().isEmpty()) {
            return function.apply(value);
        }
        return null;
    }

}
