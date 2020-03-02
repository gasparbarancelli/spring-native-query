package io.github.gasparbarancelli;

import org.apache.commons.text.WordUtils;

import java.lang.reflect.Field;

public class NativeQueryAccessField {

    private final String name;

    private final Class<?> type;

    private NativeQueryParam param;

    public NativeQueryAccessField(Field field) {
        this.name = WordUtils.capitalize(field.getName());
        this.type = field.getType();
        if (field.isAnnotationPresent(NativeQueryParam.class)) {
            this.param = field.getAnnotation(NativeQueryParam.class);
        }
    }

    public String getName() {
        return name;
    }

    public Class<?> getType() {
        return type;
    }

    public NativeQueryParam getParam() {
        return param;
    }

}
