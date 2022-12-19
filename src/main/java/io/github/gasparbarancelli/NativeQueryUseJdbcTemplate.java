package io.github.gasparbarancelli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By default, all queries are executed with Hibernate, but you can change this adding
 * the annotation @NativeQueryUseJdbcTemplate before the method.
 * Warning: Doesn't work with methods that uses Pageable
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeQueryUseJdbcTemplate {

    /**
     * If useTenant is true then you have to create a service that implements the interface
     * NativeQueryTenantNamedParameterJdbcTemplateInterceptor.
     * In your SQL queries you have to add the parameter :SCHEMA before the table name.
     * Example SELECT * FROM :SCHEMA.USER;
     * The return of implementation NativeQueryTenantNamedParameterJdbcTemplateInterceptor
     * will replace the parameter :SCHEMA.
     *
     * @return true if uses multi tenant
     */
    boolean useTenant() default false;

}
