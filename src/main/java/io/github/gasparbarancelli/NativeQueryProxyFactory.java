package io.github.gasparbarancelli;

public interface NativeQueryProxyFactory {

    Object create(Class<? extends NativeQuery> classe);

}
