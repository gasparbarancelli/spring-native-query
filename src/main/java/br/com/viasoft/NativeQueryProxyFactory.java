package br.com.viasoft;

public interface NativeQueryProxyFactory {

    Object create(Class<? extends NativeQuery> classe);

}
