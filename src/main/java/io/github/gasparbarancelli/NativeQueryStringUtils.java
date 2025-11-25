package io.github.gasparbarancelli;

public class NativeQueryStringUtils {

    private NativeQueryStringUtils() {
    }

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }


}
