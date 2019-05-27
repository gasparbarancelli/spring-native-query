package io.github.gasparbarancelli.transform;

import java.util.function.Function;

public class TransformParamEndsWith extends TransformParamString implements Function {

    @Override
    public Object apply(Object o) {
        return executeWhenValueIsNotBlank(o, o1 -> "%" + o1.toString());
    }

}
