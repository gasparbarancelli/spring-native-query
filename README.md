# spring-native-query
![Github Issues](https://img.shields.io/github/issues/gasparbarancelli/spring-native-query.svg) ![Github Stars](https://img.shields.io/github/stars/gasparbarancelli/spring-native-query.svg) ![Java](https://img.shields.io/badge/java-100%25-brightgreen.svg) ![Twitter](https://img.shields.io/twitter/url/https/github.com%2Fgasparbarancelli%2Fspring-native-query.svg) ![LICENSE](https://img.shields.io/badge/license-MIT-blue.svg)

# Spring Native Query

**Elevate your code quality and your team's productivity.**

**Spring Native Query** is the definitive library to revolutionize native query access in Spring Boot applications (with full support for versions 3 and 4). Designed for developers who demand flexibility without sacrificing organization, it eliminates the complexity of concatenating SQL strings in Java and delivers a clean, secure, and powerful development experience.

## Why use Spring Native Query?

### 1. Clean and Decoupled Architecture
Say goodbye to SQL strings polluting your Java classes.
*   **Total Separation:** Keep your queries in dedicated SQL files. This keeps Java code focused exclusively on business logic while making SQL files easy to read, version, and maintain.
*   **Efficient Collaboration:** Facilitates teamwork between Backend developers and DBAs, allowing for query reviews and optimizations isolated from application logic.

### 2. Dynamic and Smart SQL
Unleash the full power of **Freemarker** as a *Template Engine* for your queries.
*   **Logic inside SQL:** Add conditionals (`<#if>`), loops, and complex logic directly into the SQL file, adapting the query to the received parameters.
*   **Safe Syntax:** Freemarker commands are placed inside SQL comments (e.g., `-- <#if ...>`), ensuring your `.sql` file remains valid and executable in any database client.
*   **Zero Concatenation:** Eliminate manual string concatenation, drastically reducing verbosity and the risk of errors.

### 3. Productivity and Security
Write less boilerplate code and deliver more value.
*   **Automatic Mapping (Zero Boilerplate):** Transform native query results directly into **DTOs**, **Entities**, or **Java Records**. The library handles the binding automatically based on field names.
*   **Security First:** The use of parameterized queries is native, mitigating SQL Injection risks and ensuring a robust application.

---

## Key Features

*   **Convention over Configuration:** The SQL file name automatically matches the method name in your interface (e.g., `findSales` looks for `findSales.sql`), but you have the freedom to override this via annotation.
*   **Native Spring Data Integration:** Full support for pagination (`Pageable`), sorting, and dependency injection. The library automatically generates Spring Beans for your interfaces.
*   **Execution Flexibility:** Choose between `JdbcTemplate` or `EntityManager` for query execution, depending on your application's performance needs or state management requirements.
*   **Dynamic Filters:** Advanced support for flexible filters using annotations and complex objects, simplifying search screens with multiple optional parameters.

---

# Usage Example

In this example, **S**pring **N**ative **Q**uery is used to:
- Retrieve complete and aggregated sales by customer.
- Use dynamic filters and parameters in native queries.
- Map results directly to DTOs and Java records, without JPA entities.

This example demonstrates how to use the [Spring Native Query](https://github.com/gasparbarancelli/spring-native-query) library in a Spring Boot 4 application to perform native SQL queries and map results directly to Java classes.

## Demo Repository

You can find a complete demo project here: [spring-boot-4-database-native-query-demo](https://github.com/gasparbarancelli/spring-boot-4-database-native-query-demo)

## Project Structure

```
src/
  main/
    java/
      io/github/gasparbarancelli/demo/nativequery/
        NativequeryApplication.java
        SaleCustomerResult.java
        SaleFullResult.java
        SalesController.java
        SalesFilter.java
        SalesNativeQuery.java
    resources/
      application.properties
      db/migration/V1__import.sql
      nativeQuery/findSales.sql
      nativeQuery/findSalesCustomers.sql
```

## Dependencies

To use NativeQuery in your Spring Boot project, add the following dependencies to your `pom.xml`:

```xml
<!-- NativeQuery library -->
<dependency>
    <groupId>io.github.gasparbarancelli</groupId>
    <artifactId>spring-native-query</artifactId>
    <version>4.0.0</version>
</dependency>

<!-- Spring Boot Starter Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- MySQL Connector (or your preferred database) -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>

<!-- Flyway for database migrations (optional, used in this example to migrate the database schema) -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

## Code Examples

### NativequeryApplication.java
```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

// Main Spring Boot application class
@SpringBootApplication
// Enables Spring Data Web support to use Pageable objects in REST endpoints to Spring Boot 4; do not add this configuration in Spring Boot 3.
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class NativequeryApplication {
    // Main method to start the application
    public static void main(String[] args) {
        SpringApplication.run(NativequeryApplication.class, args);
    }
}
```

### SaleCustomerResult.java
```java
import java.math.BigDecimal;
import java.time.LocalDateTime;

// DTO for sales results by customer
public class SaleCustomerResult {
    private Integer id; // Sale ID
    private BigDecimal totalAmount; // Total sale amount
    private String status; // Sale status
    private Integer customerId; // Customer ID
    private String customerFullName; // Customer full name
    private String customerEmail; // Customer email
    private Integer customerActive; // Customer active status
    // getters and setters
}
```

### SaleFullResult.java
```java
import java.math.BigDecimal;
import java.time.LocalDateTime;

// Record for complete sale result, including items, payments, and customer
public record SaleFullResult(
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

### SalesFilter.java
```java
import io.github.gasparbarancelli.NativeQueryOperator;
import io.github.gasparbarancelli.NativeQueryParam;

// Filter for sales queries
public record SalesFilter(
        Number id, // Sale ID
        // The library supports operations such as CONTAINING, STARTS_WITH, and ENDS_WITH, automatically adding wildcard characters.
        @NativeQueryParam(value = "name", operator = NativeQueryOperator.CONTAINING)
        String customerName // Customer name for search
) {
}
```

### SalesNativeQuery.java
```java
import io.github.gasparbarancelli.NativeQuery;
import io.github.gasparbarancelli.NativeQueryParam;
import io.github.gasparbarancelli.NativeQueryUseJdbcTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

// Interface for native queries using the nativequery library
@Repository
public interface SalesNativeQuery extends NativeQuery {
    // Query sales by customer
    // The library automatically executes an SQL file with the same name as this method and converts the response into a `SaleCustomerResult` result
    List<SaleCustomerResult> findSalesCustomers();
    // Paginated query of sales by customer; the library executes the query using pagination and returns a `Page<SaleCustomerResult>`.
    // Only queries performed with the entity manager can return a page; JdbcTemplate does not support this feature
    Page<SaleCustomerResult> findSalesCustomers(Pageable pageable);
    // Query all sales
    // The default NativeQuery implementation executes queries using the EntityManager, but you can change it and use the JdbcTemplate using the annotation below.
    @NativeQueryUseJdbcTemplate
    List<SaleFullResult> findSales();
    // Query sales by customer ID
    @NativeQueryUseJdbcTemplate
    // By default, the query uses the same file name as the method, but you can specify a different file name using the annotation below.
    @NativeQueryFileName("findSales")
    // You can add parameters to the query using the annotation below.
    List<SaleFullResult> findSalesByCustomerId(@NativeQueryParam(value = "customerId") int customerId);
    // Query sales using filter
    @NativeQueryUseJdbcTemplate
    // You can add custom objects to the query using the annotation below, where the library adds all the fields from the objects as parameters in the query.
    List<SaleFullResult> findSales(@NativeQueryParam(value = "filter", addChildren = true) SalesFilter filter);
    // Dynamic query using parameter map
    @NativeQueryUseJdbcTemplate
    // You can add a parameter map and use it in the query to perform more dynamic queries.
    List<SaleFullResult> findSales(Map<String, Object> params);
}
```

### SalesController.java
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// REST controller for sales endpoints
@RestController
@RequestMapping("/sales")
public class SalesController {
    private final SalesNativeQuery salesNativeQuery; // Injects the query interface
    public SalesController(SalesNativeQuery salesNativeQuery) {
        this.salesNativeQuery = salesNativeQuery;
    }
    // Returns all sales
    @GetMapping
    public List<SaleFullResult> findSales() {
        return salesNativeQuery.findSales();
    }
    // Returns sales by customer ID
    @GetMapping("/customer/{customerId}")
    public List<SaleFullResult> findSalesByCustomerId(@PathVariable("customerId") int customerId) {
        return salesNativeQuery.findSalesByCustomerId(customerId);
    }
    // Returns sales filtered
    @PostMapping("/filter")
    public List<SaleFullResult> findSalesByFilter(@RequestBody SalesFilter filter) {
        return salesNativeQuery.findSales(filter);
    }
    // Returns sales using dynamic parameters
    @GetMapping("/dynamic")
    public List<SaleFullResult> findDynamicSales() {
        Map<String, Object> map = new HashMap<>();
        map.put("p.id", 1);
        map.put("c.id", 1);
        return salesNativeQuery.findSales(map);
    }
    // Returns aggregated sales by customer
    @GetMapping("/customers")
    public List<SaleCustomerResult> findSalesCustomers() {
        return salesNativeQuery.findSalesCustomers();
    }
    // Returns paginated aggregated sales by customer
    @GetMapping("/customers/pageable")
    public Page<SaleCustomerResult> findSalesCustomersPageable(Pageable pageable) {
        return salesNativeQuery.findSalesCustomers(pageable);
    }
}
```

### application.properties
```properties
# Application and database configuration
spring.application.name=nativequery
native-query.package-scan=io.github.gasparbarancelli.demo.nativequery
spring.datasource.url=jdbc:mysql://localhost:3306/demo?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

### V1__import.sql
```sql
-- Main tables for sales domain
CREATE TABLE CUSTOMER
(
    id        INT          NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(100) NOT NULL,
    email     VARCHAR(100) NULL,
    active    INT          NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE PRODUCT
(
    id          INT            NOT NULL AUTO_INCREMENT,
    name        VARCHAR(100)   NOT NULL,
    description VARCHAR(255) NULL,
    price       DECIMAL(10, 2) NOT NULL,
    active      INT            NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE SALE
(
    id           INT            NOT NULL AUTO_INCREMENT,
    sale_date    DATETIME       NOT NULL,
    customer_id  INT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status       VARCHAR(20)    NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (customer_id) REFERENCES CUSTOMER (id)
);

CREATE TABLE SALE_ITEM
(
    id         INT            NOT NULL AUTO_INCREMENT,
    sale_id    INT            NOT NULL,
    product_id INT            NOT NULL,
    quantity   INT            NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    discount   DECIMAL(10, 2) NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (sale_id) REFERENCES SALE (id),
    FOREIGN KEY (product_id) REFERENCES PRODUCT (id)
);

CREATE TABLE SALE_PAYMENT
(
    id           INT            NOT NULL AUTO_INCREMENT,
    sale_id      INT            NOT NULL,
    payment_type VARCHAR(20)    NOT NULL,
    paid_amount  DECIMAL(10, 2) NOT NULL,
    payment_date DATETIME       NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (sale_id) REFERENCES SALE (id)
);

INSERT INTO CUSTOMER (full_name, email, active)
VALUES ('John Doe', 'john.doe@email.com', 1),
       ('Jane Smith', 'jane.smith@email.com', 1),
       ('Alice Johnson', 'alice.j@email.com', 1),
       ('Bob Brown', 'bob.b@email.com', 1),
       ('Charlie Black', 'charlie.b@email.com', 1);

INSERT INTO PRODUCT (name, description, price, active)
VALUES ('Laptop', 'High performance laptop', 2500.00, 1),
       ('Smartphone', 'Latest model smartphone', 1800.00, 1),
       ('Headphones', 'Noise cancelling headphones', 350.00, 1),
       ('Monitor', '27 inch 4K monitor', 1200.00, 1),
       ('Keyboard', 'Mechanical keyboard', 400.00, 1);

INSERT INTO SALE (sale_date, customer_id, total_amount, status)
VALUES ('2024-06-01 10:00:00', 1, 2500.00, 'COMPLETED'),
       ('2024-06-01 11:00:00', 2, 1800.00, 'COMPLETED'),
       ('2024-06-01 12:00:00', 3, 350.00, 'COMPLETED'),
       ('2024-06-01 13:00:00', 4, 1200.00, 'COMPLETED'),
       ('2024-06-01 14:00:00', 5, 400.00, 'COMPLETED'),
       ('2024-06-02 10:00:00', 1, 2200.00, 'COMPLETED'),
       ('2024-06-02 11:00:00', 2, 1550.00, 'COMPLETED'),
       ('2024-06-02 12:00:00', 3, 350.00, 'COMPLETED'),
       ('2024-06-02 13:00:00', 4, 1200.00, 'COMPLETED'),
       ('2024-06-02 14:00:00', 5, 400.00, 'COMPLETED');

INSERT INTO SALE_ITEM (sale_id, product_id, quantity, unit_price, discount)
VALUES (1, 1, 1, 2500.00, 0.00),
       (2, 2, 1, 1800.00, 0.00),
       (3, 3, 1, 350.00, 0.00),
       (4, 4, 1, 1200.00, 0.00),
       (5, 5, 1, 400.00, 0.00),
       (6, 1, 1, 2200.00, 300.00),
       (7, 2, 1, 1550.00, 250.00),
       (8, 3, 1, 350.00, 0.00),
       (9, 4, 1, 1200.00, 0.00),
       (10, 5, 1, 400.00, 0.00);

INSERT INTO SALE_PAYMENT (sale_id, payment_type, paid_amount, payment_date)
VALUES (1, 'CREDIT_CARD', 2500.00, '2024-06-01 10:05:00'),
       (2, 'CREDIT_CARD', 1800.00, '2024-06-01 11:05:00'),
       (3, 'CASH', 350.00, '2024-06-01 12:05:00'),
       (4, 'CREDIT_CARD', 1200.00, '2024-06-01 13:05:00'),
       (5, 'CASH', 400.00, '2024-06-01 14:05:00'),
       (6, 'CREDIT_CARD', 2200.00, '2024-06-02 10:05:00'),
       (7, 'CREDIT_CARD', 1550.00, '2024-06-02 11:05:00'),
       (8, 'CASH', 350.00, '2024-06-02 12:05:00'),
       (9, 'CREDIT_CARD', 1200.00, '2024-06-02 13:05:00'),
       (10, 'CASH', 400.00, '2024-06-02 14:05:00');
```

### findSales.sql
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
WHERE 1=1
-- <#if customerId??>
AND c.id = :customerId
-- </#if>
-- <#if params??>
-- <#list params as item>
AND ${item} = :${item}
-- </#list>
-- </#if>
-- <#if filterId??>
AND s.id = :filterId
-- </#if>
-- <#if filterName??>
AND c.full_name like :filterName
-- </#if>
ORDER BY s.id, si.id, sp.id
```

### findSalesCustomers.sql
```sql
-- Native query to fetch aggregated sales by customer
SELECT
    s.id AS id,
    s.total_amount AS totalAmount,
    s.status AS status,
    c.id AS customerId,
    c.full_name AS customerFullName,
    c.email AS customerEmail,
    c.active AS customerActive
FROM SALE s
    LEFT JOIN CUSTOMER c ON s.customer_id = c.id
ORDER BY s.id
```

## Testing Endpoints with cURL

Below are examples of cURL commands to test the main endpoints of the application:

### 1. Fetch all sales
```bash
curl -X GET http://localhost:8080/sales
```

### 2. Fetch sales by customer ID
```bash
curl -X GET http://localhost:8080/sales/customer/1
```

### 3. Fetch filtered sales (POST with JSON)
```bash
curl -X POST http://localhost:8080/sales/filter \
  -H "Content-Type: application/json" \
  -d '{"customerName":"smith"}'
```

### 4. Fetch dynamic sales
```bash
curl -X GET http://localhost:8080/sales/dynamic
```

### 5. Fetch aggregated sales by customer
```bash
curl -X GET http://localhost:8080/sales/customers
```

### 6. Fetch aggregated sales by customer (paginated)
```bash
curl -X GET "http://localhost:8080/sales/customers/pageable?page=0&size=10"
```

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Breaking Notes

**Template Engine Change:**

The project previously used Jtwig as the SQL template engine. However, Jtwig has been discontinued, so the template engine was replaced with Freemarker. All SQL file templating now uses Freemarker syntax. This change improves maintainability and future compatibility.

---

## Notes
- The SQL examples and query implementations can be adapted to your data model.
- The `nativequery` library makes it easy to map native query results directly to DTOs and Java records.

---

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
