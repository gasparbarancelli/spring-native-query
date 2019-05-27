package io.github.gasparbarancelli;

public interface NativeQueryMethodInterceptor {

    Object executeQuery(NativeQueryInfo info);

}
