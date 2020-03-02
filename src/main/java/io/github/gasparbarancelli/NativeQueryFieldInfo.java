package io.github.gasparbarancelli;

public class NativeQueryFieldInfo {

    private final NativeQueryParam param;

    private final  Class<?> type;

    public NativeQueryFieldInfo(NativeQueryParam param, Class<?> type) {
        this.param = param;
        this.type = type;
    }

    public NativeQueryParam getParam() {
        return param;
    }

    public Class<?> getType() {
        return type;
    }
}
