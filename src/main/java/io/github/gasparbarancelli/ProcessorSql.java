package io.github.gasparbarancelli;

import java.util.Map;

public interface ProcessorSql {

    String execute(String sql, Map<String, String> replaceSql);

}
