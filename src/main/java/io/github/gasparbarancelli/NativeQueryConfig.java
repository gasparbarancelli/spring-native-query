package io.github.gasparbarancelli;

public interface NativeQueryConfig {

    String getPackageScan();

    default String getFileSufix() {
        return "sql";
    }

    default Boolean getUseHibernateTypes() {
        return Boolean.TRUE;
    }

    default String getSQLDirectory() {
        return NativeQueryAutoConfiguration.SQL_DIRECTORY;
    }

}
