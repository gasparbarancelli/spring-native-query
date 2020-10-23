package io.github.gasparbarancelli;

public interface NativeQueryConfig {

    String getPackageScan();

    String getFileSufix();

    default String getSQLDirectory() {
        return NativeQueryAutoConfiguration.SQL_DIRECTORY;
    }

}
