package io.github.gasparbarancelli;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;

/**
 * Provides access to the Spring {@link ApplicationContext} from non-Spring managed classes.
 *
 * <p>This class implements {@link ApplicationContextAware} to capture the application context
 * when the Spring container initializes. The context is stored in a static field, allowing
 * it to be accessed globally.</p>
 *
 * @see ApplicationContext
 * @see ApplicationContextAware
 */
@Configuration
public class ApplicationContextProvider implements ApplicationContextAware {

    private static ApplicationContext context;

    /**
     * Returns the Spring {@link ApplicationContext}.
     *
     * @return The application context, or {@code null} if it has not been initialized.
     */
    public static ApplicationContext getApplicationContext() {
        return context;
    }

    /**
     * Sets the {@link ApplicationContext} that this object runs in.
     *
     * <p>This method is called by the Spring container during initialization. It is not
     * intended to be called by application code.</p>
     *
     * @param ctx The {@code ApplicationContext} to be used by this object.
     */
    @Override
    public void setApplicationContext(ApplicationContext ctx) {
        context = ctx;
    }

}
