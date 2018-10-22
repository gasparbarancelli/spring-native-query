package br.com.viasoft.transform;

import java.util.function.Function;

public class TransformParamContaining extends TransformParamString implements Function {

    @Override
    public Object apply(Object o) {
        return executeWhenValueIsNotBlank(o, o1 -> "%" + o1.toString() + "%");
    }

}
