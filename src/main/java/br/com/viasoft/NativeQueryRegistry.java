package br.com.viasoft;

import java.util.Set;

public interface NativeQueryRegistry {

    void registry(Set<Class<? extends NativeQuery>> nimitzNativeQueryList);

}
