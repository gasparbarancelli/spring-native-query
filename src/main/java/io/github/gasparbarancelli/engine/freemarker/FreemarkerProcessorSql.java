package io.github.gasparbarancelli.engine.freemarker;

import io.github.gasparbarancelli.ProcessorSql;

import java.util.Map;

/**
 * A SQL processor that removes Freemarker-style comments from a SQL string.
 *
 * <p>This implementation of {@link ProcessorSql} is designed to clean up SQL queries
 * that use Freemarker template comments (e.g., {@code -- <#if ...>}). It removes all
 * lines or parts of lines that start with {@code --}, which are treated as comments
 * in standard SQL but may contain template logic in Freemarker.</p>
 *
 * <p>The {@code replaceSql} parameter is not used in this implementation, as the
 * primary goal is to strip comments rather than perform replacements.</p>
 *
 * @see ProcessorSql
 * @see FreemarkerTemplateEngineSQLProcessor
 */
public class FreemarkerProcessorSql implements ProcessorSql {

    /**
     * Removes all Freemarker-style comments from the given SQL string.
     *
     * <p>This method uses a regular expression to find and remove any text that
     * starts with {@code --} and continues to the end of the line. This is
     * effective for cleaning up SQL templates where Freemarker directives are
     * embedded in SQL comments.</p>
     *
     * @param sql The SQL string to be processed, potentially containing Freemarker comments.
     * @param replaceSql A map of replacements, which is ignored by this implementation.
     * @return The processed SQL string with all comments removed.
     */
    @Override
    public String execute(String sql, Map<String, String> replaceSql) {
        return sql.replaceAll("--.*?(\r?\n|$)", "");
    }

}