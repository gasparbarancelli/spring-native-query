package io.github.gasparbarancelli.engine.freemarker;

import freemarker.template.*;
import io.github.gasparbarancelli.engine.TemplateEngineSQLProcessor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * A {@link TemplateEngineSQLProcessor} that uses Freemarker to process SQL templates.
 *
 * <p>This class is responsible for processing SQL templates, whether they are provided as
 * inline strings or as file paths on the classpath. It uses a shared, pre-configured
 * Freemarker instance to parse and render the templates, injecting the query parameters
 * into the SQL.</p>
 *
 * <p>The Freemarker configuration is optimized for performance and security, with features
 * like exception re-throwing and disabled caching.</p>
 *
 * @see TemplateEngineSQLProcessor
 * @see Configuration
 */
public class FreemarkerTemplateEngineSQLProcessor extends TemplateEngineSQLProcessor {

    private static final Configuration freemarkerConfiguration;

    static {
        freemarkerConfiguration = new Configuration(new Version("2.3.34"));
        freemarkerConfiguration.setDefaultEncoding("UTF-8");
        freemarkerConfiguration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        freemarkerConfiguration.setLogTemplateExceptions(false);
        freemarkerConfiguration.setWrapUncheckedExceptions(true);
        freemarkerConfiguration.setCacheStorage(new freemarker.cache.NullCacheStorage());
        freemarkerConfiguration.setClassLoaderForTemplateLoading(
            FreemarkerTemplateEngineSQLProcessor.class.getClassLoader(), "/"
        );
    }

    /**
     * Processes an inline SQL template using Freemarker.
     *
     * @param sql The inline SQL template string.
     * @return The processed SQL with parameters rendered.
     */
    @Override
    protected String processInline(String sql) {
        return render(new StringReader(sql));
    }

    /**
     * Processes a SQL template from a file on the classpath using Freemarker.
     *
     * @param classpathTemplate The path to the template file on the classpath.
     * @return The processed SQL with parameters rendered.
     * @throws RuntimeException if the template file cannot be loaded.
     */
    @Override
    protected String processFile(String classpathTemplate) {
        try {
            Template template = freemarkerConfiguration.getTemplate(classpathTemplate);
            return render(template);
        } catch (IOException e) {
            throw new RuntimeException("Error loading Freemarker template", e);
        }
    }

    /**
     * Renders a Freemarker template from a {@link StringReader}.
     *
     * @param reader The reader containing the template content.
     * @return The rendered SQL string.
     * @throws RuntimeException if the template cannot be created.
     */
    private String render(StringReader reader) {
        try {
            Template template = new Template("inline", reader, freemarkerConfiguration);
            return render(template);
        } catch (IOException e) {
            throw new RuntimeException("Error creating Freemarker template", e);
        }
    }

    /**
     * Renders a given Freemarker {@link Template} with the current query parameters.
     *
     * @param template The template to be processed.
     * @return The final SQL string after rendering.
     * @throws RuntimeException if an error occurs during template processing.
     */
    private String render(Template template) {
        try (StringWriter writer = new StringWriter()) {
            Map<String, Object> params = getParameters();
            template.process(params, writer);
            return writer.toString();
        } catch (TemplateException | IOException e) {
            throw new RuntimeException("Error rendering Freemarker template", e);
        }
    }

}