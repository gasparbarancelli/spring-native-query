package io.github.gasparbarancelli;

import java.util.Map;

/**
 * An interface for processing a SQL string before it is executed.
 *
 * <p>Implementations of this interface can be used to perform custom transformations
 * on a SQL query string. This can include tasks like removing comments, replacing
 * placeholders, or any other form of string manipulation.</p>
 *
 * <p>Custom processors can be registered on a native query method using the
 * {@link NativeQueryReplaceSql} annotation.</p>
 *
 * @see NativeQueryReplaceSql
 * @see FreemarkerProcessorSql
 */
public interface ProcessorSql {

    /**
     * Executes the SQL processing logic.
     *
     * @param sql        The SQL string to be processed.
     * @param replaceSql A map of key-value pairs for replacement, as defined in
     *                   the {@link NativeQueryReplaceSql} annotation.
     * @return The processed SQL string.
     */
    String execute(String sql, Map<String, String> replaceSql);

}