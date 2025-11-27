package io.github.gasparbarancelli.engine.freemarker;

import freemarker.template.*;
import io.github.gasparbarancelli.engine.TemplateEngineSQLProcessor;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

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

    @Override
    protected String processInline(String sql) {
        return render(new StringReader(sql));
    }

    @Override
    protected String processFile(String classpathTemplate) {
        try {
            Template template = freemarkerConfiguration.getTemplate(classpathTemplate);
            return render(template);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar template Freemarker", e);
        }
    }

    private String render(StringReader reader) {
        try {
            Template template = new Template("inline", reader, freemarkerConfiguration);
            return render(template);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar template Freemarker", e);
        }
    }

    private String render(Template template) {
        try (StringWriter writer = new StringWriter()) {
            Map<String, Object> params = getParameters();
            template.process(params, writer);
            return writer.toString();
        } catch (TemplateException | IOException e) {
            throw new RuntimeException("Erro ao renderizar template Freemarker", e);
        }
    }

}
