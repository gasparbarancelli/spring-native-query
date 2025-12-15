package io.github.gasparbarancelli;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation used to specify a custom folder for the SQL files of a {@link NativeQuery} interface.
 *
 * <p>By default, the Spring Native Query library looks for SQL files in the
 * {@code resources/nativeQuery} directory. The name of the SQL file must match the
 * corresponding method name in the interface. For example, if a method is named
 * {@code findSales()}, the library will search for a file named
 * {@code findSales.sql} in {@code resources/nativeQuery/findSales.sql}.</p>
 *
 * <p>This annotation allows you to specify a sub-folder within {@code resources/nativeQuery}
 * to better organize your SQL files. This is particularly useful when you have many
 * query files.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @Repository
 * @NativeQueryFolder("sales")
 * public interface SaleRepository extends NativeQuery {
 *     List<Sale> findSales();
 * }
 * }</pre>
 *
 * <p>In the example above, the SQL file for the {@code findSales()} method should be placed in the
 * {@code resources/nativeQuery/sales/findSales.sql} path.</p>
 *
 * @see NativeQuery
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NativeQueryFolder {

    /**
     * The name of the folder inside {@code resources/nativeQuery} where the SQL files are located.
     *
     * @return The folder name.
     */
    String value();

}
