package io.github.gasparbarancelli.engine.jtwig;

import io.github.gasparbarancelli.engine.TemplateEngineSQLProcessor;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;

public class JtwigTemplateEngineSQLProcessor extends TemplateEngineSQLProcessor {

    @Override
    protected String processInline(String sql) {
        return render(JtwigTemplate.inlineTemplate(sql, JtwigTemplateConfig.get()));
    }

    @Override
    protected String processFile(String classpathTemplate) {
        return render(JtwigTemplate.classpathTemplate(classpathTemplate, JtwigTemplateConfig.get()));
    }

    private String render(JtwigTemplate jtwigTemplate) {
        JtwigModel model = JtwigModel.newModel();
        getParameters().forEach(model::with);

        return jtwigTemplate.render(model);
    }

}
