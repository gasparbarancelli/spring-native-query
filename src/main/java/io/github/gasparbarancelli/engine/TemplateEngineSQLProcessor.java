package io.github.gasparbarancelli.engine;

import io.github.gasparbarancelli.NativeQueryParameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An abstract base class for processing SQL templates using a template engine.
 *
 * <p>This class provides a framework for processing SQL templates, which can be either
 * inline strings or files loaded from the classpath. Subclasses must implement the
 * methods for processing inline and file-based templates according to the specific
 * template engine they support (e.g., Freemarker).</p>
 *
 * <p>The processor is configured with the template content, parameters, and the mode
 * (inline or file). The final processed SQL is retrieved via the {@link #getSql()} method.</p>
 *
 * @see FreemarkerTemplateEngineSQLProcessor
 */
public abstract class TemplateEngineSQLProcessor {

    private String inlineTemplate;
    private String classpathTemplate;
    private boolean inline;
    private Map<String, Object> parameters;

    /**
     * Processes an inline SQL template.
     *
     * @param sql The inline SQL template string.
     * @return The processed SQL.
     */
    protected abstract String processInline(String sql);

    /**
     * Processes a SQL template from a file on the classpath.
     *
     * @param classpathTemplate The path to the template file on the classpath.
     * @return The processed SQL.
     */
    protected abstract String processFile(String classpathTemplate);

    /**
     * Sets whether the template is inline.
     *
     * @param inline {@code true} for an inline template, {@code false} for a file-based template.
     * @return This processor instance for method chaining.
     */
    public final TemplateEngineSQLProcessor inline(boolean inline) {
        this.inline = inline;
        return this;
    }

    /**
     * Sets the classpath location of the SQL template file.
     *
     * @param classpathTemplate The path to the template file.
     * @return This processor instance for method chaining.
     */
    public final TemplateEngineSQLProcessor setClasspathTemplate(String classpathTemplate) {
        this.classpathTemplate = classpathTemplate;
        return this;
    }

    /**
     * Sets the inline SQL template string.
     *
     * @param inlineTemplate The inline SQL template.
     * @return This processor instance for method chaining.
     */
    public final TemplateEngineSQLProcessor setInlineTemplate(String inlineTemplate) {
        this.inlineTemplate = inlineTemplate;
        return this;
    }

    /**
     * Sets the parameters to be used in the template processing.
     *
     * @param parameters A list of {@link NativeQueryParameter} objects.
     * @return This processor instance for method chaining.
     */
    public final TemplateEngineSQLProcessor setParameter(List<NativeQueryParameter> parameters) {
        this.parameters = parameters.stream()
                .collect(HashMap::new, (m, v) -> m.put(v.getName(), v.getValue()), HashMap::putAll);
        return this;
    }

    /**
     * Returns an unmodifiable map of the parameters for the template.
     *
     * @return The template parameters.
     */
    protected Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    /**
     * Returns the processed SQL string.
     *
     * <p>This method determines whether to process an inline or file-based template
     * based on the configuration and returns the final SQL.</p>
     *
     * @return The processed SQL.
     */
    public final String getSql() {
        return inline ? processInline(inlineTemplate) : processFile(classpathTemplate);
    }
}