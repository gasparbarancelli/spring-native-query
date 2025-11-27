# spring-native-query
![Github Issues](https://img.shields.io/github/issues/gasparbarancelli/spring-native-query.svg) ![Github Stars](https://img.shields.io/github/stars/gasparbarancelli/spring-native-query.svg) ![Java](https://img.shields.io/badge/java-100%25-brightgreen.svg) ![Twitter](https://img.shields.io/twitter/url/https/github.com%2Fgasparbarancelli%2Fspring-native-query.svg) ![LICENSE](https://img.shields.io/badge/license-MIT-blue.svg)

Increase your productivity and code quality with Spring Native Query, the library that revolutionizes native query access in Spring Boot applications. Designed for those seeking flexibility, organization, and power in SQL manipulation, our library delivers a superior experience for modern projects, including full support for Spring Boot 4.

- **Complete separation between SQL and Java:** Keep your native queries outside Java classes, making code easier to read, maintain, and evolve. SQL is organized in its own files, keeping your project clean and decoupled.
- **Template Engine for dynamic SQL:** Leverage engines like Freemarker to create dynamic queries, adapting SQL according to received parameters. This eliminates manual string concatenation and reduces risks of errors and vulnerabilities.
- **More readable and sustainable code:** With separation and templates, Java code focuses on business logic, while SQL can be easily reviewed and tested independently.
- **Advanced support for Spring Boot 4:** Take advantage of the latest Spring Boot features, with seamless integration and optimized resources for modern projects.
- **Automatic result mapping:** Easily transform native query results into DTOs, entities ou Java Records, without boilerplate.
- **Security and organization:** Reduce SQL Injection risks and keep your code secure and well-structured.

## Benefits for developers
- Leverage engines like Freemarker to create dynamic queries, adapting SQL according to received parameters. This eliminates manual string concatenation and reduces risks of errors and vulnerabilities.
- Less repetitive code and more focus on business logic.
- Easy to create, modify, and version queries.
- Better collaboration between backend and DBA teams.
- Quick adaptation to changing requirements or databases.
- Native integration with Spring Data, Hibernate, and the Spring ecosystem.

## Documentation

- [Spring Boot 2 and 3](spring-boot-2-and-3.md): Complete guide for integration and usage in versions 2.x and 3.x.
- [Spring Boot 4](spring-boot-4.md): Complete guide for integration and usage in version 4.x.

## Quick Example

### Querying Sales with Spring Native Query

See how simple and productive it is to use the library to query sales data:

**1. Repository Interface:**

Create an interface that extends NativeQuery, then add methods that return the desired data types. The method name must match the SQL file that will be created in the next step.

```java
import io.github.gasparbarancelli.NativeQuery;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends NativeQuery {
    List<SaleFullResult> findSales();
}
```

**2. Entity/DTO/Record:**

You can return entities, DTOs, or Records. For this example, we'll use a Record.

```java
package com.grupopan.demo.nativequery;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Record for complete sale result, including items, payments, and customer
default record SaleFullResult(
    int id, // Sale ID
    LocalDateTime saleDate, // Sale date
    int customerId, // Customer ID
    String customerFullName, // Customer full name
    String customerEmail, // Customer email
    int customerActive, // Customer active status
    int saleItemId, // Sale item ID
    int saleItemQuantity, // Item quantity
    BigDecimal saleItemUnitPrice, // Item unit price
    BigDecimal saleItemDiscount, // Item discount
    int productId, // Product ID
    String productName, // Product name
    String productDescription, // Product description
    BigDecimal productPrice, // Product price
    int productActive, // Product active status
    int salePaymentId, // Payment ID
    String salePaymentPaymentType, // Payment type
    BigDecimal salePaymentPaidAmount, // Paid amount
    LocalDateTime salePaymentPaymentDate, // Payment date
    BigDecimal totalAmount, // Total sale amount
    String status // Sale status
) {}
```

**3. SQL in `resources/nativeQuery/findSales.sql`:**

As mentioned in step 1, by convention the method name must be the same as the SQL file name.

```sql
-- Native query to fetch complete sales
SELECT
    s.id AS id,
    s.sale_date AS saleDate,
    c.id AS customerId,
    c.full_name AS customerFullName,
    c.email AS customerEmail,
    c.active AS customerActive,
    si.id AS saleItemId,
    si.quantity AS saleItemQuantity,
    si.unit_price AS saleItemUnitPrice,
    si.discount AS saleItemDiscount,
    p.id AS productId,
    p.name AS productName,
    p.description AS productDescription,
    p.price AS productPrice,
    p.active AS productActive,
    sp.id AS salePaymentId,
    sp.payment_type AS salePaymentPaymentType,
    sp.paid_amount AS salePaymentPaidAmount,
    sp.payment_date AS salePaymentPaymentDate,
    s.total_amount AS totalAmount,
    s.status AS status
FROM SALE s
         LEFT JOIN CUSTOMER c ON s.customer_id = c.id
         LEFT JOIN SALE_ITEM si ON si.sale_id = s.id
         LEFT JOIN PRODUCT p ON si.product_id = p.id
         LEFT JOIN SALE_PAYMENT sp ON sp.sale_id = s.id
ORDER BY s.id, si.id, sp.id
```

**4. Simple Controller:**

Finally, you can inject the SaleRepository interface into your Controller like any other Spring Bean.

```java
@RestController
public class SaleController {
    private final SaleRepository saleRepository;

    public SaleController(SaleRepository saleRepository) {
        this.saleRepository = saleRepository;
    }

    @GetMapping("/sales")
    public List<SaleFullResult> getSales() {
        return saleRepository.findSales();
    }
}
```

This example shows that with just one SQL file, an interface, and a controller, you can query native data simply and efficiently.

## Contribute and Join Us

We welcome contributions from the community! If you have ideas, improvements, or bug fixes, feel free to open issues or submit pull requests on our [GitHub repository](https://github.com/gasparbarancelli/spring-native-query).

- **How to contribute:**
    - Fork the repository and create your branch.
    - Make your changes and submit a pull request with a clear description.
    - Discuss improvements and new features in the issues section.
- **Join our community:**
    - Share your use cases and feedback.
    - Help others by answering questions and sharing tips.
    - Stay updated by following the repository and joining discussions.

Together, we can make Spring Native Query even better for everyone!
