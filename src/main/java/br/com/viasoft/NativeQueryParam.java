package br.com.viasoft;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeQueryParam {

    String value();

    NativeQueryOperator operator() default NativeQueryOperator.DEFAULT;

    boolean addChildren() default false;

}
