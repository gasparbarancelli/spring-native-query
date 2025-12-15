package io.github.gasparbarancelli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to override the default SQL file name for a native query method.
 *
 * <p>By default, the Spring Native Query library assumes that the SQL file for a method
 * has the same name as the method itself (e.g., a method named {@code findSales} will
 * look for a file named {@code findSales.sql}). This annotation allows you to specify
 * a different file name, which is useful for reusing the same SQL file for multiple
 * methods or for using more descriptive file names.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Repository
 * public interface SaleRepository extends NativeQuery {
 *
 *     @NativeQueryFileName("findSales")
 *     List<SaleFullResult> findSalesByCustomerId(@NativeQueryParam("customerId") int customerId);
 * }
 * }</pre>
 *
 * @see NativeQuery
 */
@Target({ ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeQueryFileName {

    /**
     * The name of the SQL file (without the extension) to be used for the annotated method.
     *
     * @return The SQL file name.
     */
    String value();

}