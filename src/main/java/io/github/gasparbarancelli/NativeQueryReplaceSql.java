package io.github.gasparbarancelli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeQueryReplaceSql {

    NativeQueryReplaceSqlParams[] values() default {};

    Class<? extends ProcessorSql>[] processorParams() default {};

}
