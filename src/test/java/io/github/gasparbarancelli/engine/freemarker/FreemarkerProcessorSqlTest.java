package io.github.gasparbarancelli.engine.freemarker;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class FreemarkerProcessorSqlTest {

    @Test
    void shouldRemoveSqlComments() {
        FreemarkerProcessorSql processor = new FreemarkerProcessorSql();
        String sql = "SELECT cod as \"id\", full_name as \"name\" FROM USER WHERE 1=1 -- comment\nAND cod = 1 -- another";
        String expected = "SELECT cod as \"id\", full_name as \"name\" FROM USER WHERE 1=1 AND cod = 1 ";
        String result = processor.execute(sql, Map.of());
        assertEquals(expected, result);
    }

}
