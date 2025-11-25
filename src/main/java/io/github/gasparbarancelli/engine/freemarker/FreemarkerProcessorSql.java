package io.github.gasparbarancelli.engine.freemarker;

import io.github.gasparbarancelli.ProcessorSql;

import java.util.Map;

public class FreemarkerProcessorSql implements ProcessorSql {

    @Override
    public String execute(String sql, Map<String, String> replaceSql) {
        return sql.replaceAll("--.*?(\\r?\\n|$)", "");
    }

}
