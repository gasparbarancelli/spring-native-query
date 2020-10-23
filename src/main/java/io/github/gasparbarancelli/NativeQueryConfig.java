package io.github.gasparbarancelli;

public interface NativeQueryConfig {

    String getPackageScan();

    default String getFileSufix() {
        return "sql";
    }

    default boolean getUseHibernateTypes() {
        return true;
    }

    default String getSQLDirectory() {
        return NativeQueryAutoConfiguration.SQL_DIRECTORY;
    }

}
