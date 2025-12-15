package io.github.gasparbarancelli;

/**
 * A utility class for string manipulation.
 *
 * <p>This class provides helper methods for common string operations used within the
 * Spring Native Query library.</p>
 */
public class NativeQueryStringUtils {

    private NativeQueryStringUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Capitalizes the first letter of a string.
     *
     * @param str The string to be capitalized.
     * @return The capitalized string, or the original string if it is null or empty.
     */
    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

}