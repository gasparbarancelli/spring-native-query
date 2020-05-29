package io.github.gasparbarancelli.engine;

import io.github.gasparbarancelli.NativeQueryParameter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TemplateEngineSQLProcessor {

    private String inlineTemplate;
    private String classpathTemplate;
    private boolean inline;
    private Map<String, Object> parameters;

    protected abstract String processInline(String sql);

    protected abstract String processFile(String classpathTemplate);

    public final TemplateEngineSQLProcessor inline(boolean inline) {
        this.inline = inline;
        return this;
    }

    public final TemplateEngineSQLProcessor setClasspathTemplate(String classpathTemplate) {
        this.classpathTemplate = classpathTemplate;
        return this;
    }

    public final TemplateEngineSQLProcessor setInlineTemplate(String inlineTemplate) {
        this.inlineTemplate = inlineTemplate;
        return this;
    }

    public final TemplateEngineSQLProcessor setParameter(List<NativeQueryParameter> parameters) {
        this.parameters = parameters.stream()
                .collect(HashMap::new, (m, v) -> m.put(v.getName(), v.getValue()), HashMap::putAll);
        return this;
    }

    protected Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public final String getSql() {
        return inline ? processInline(inlineTemplate) : processFile(classpathTemplate);
    }
}
