package io.github.gasparbarancelli;

import java.util.Set;

public interface NativeQueryRegistry {

    void registry(Set<Class<? extends NativeQuery>> nimitzNativeQueryList);

}
