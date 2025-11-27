---
layout: default
---
# Spring Native Query with Spring Boot 4

## About

The [Spring Native Query](https://github.com/gasparbarancelli/spring-native-query) library enables you to execute native SQL queries in Spring Boot 4 projects in a simple, safe, and flexible way. It allows you to map query results directly to Java classes (DTOs or records) without the need for JPA entities, making data access more performant and decoupled.

Key features:
- **Automatic mapping**: Query results are converted to Java classes using compatible field names.
- **Parameterized queries**: Allows passing parameters to native queries safely, preventing SQL Injection.
- **Dynamic filters**: Supports flexible filters using annotations and filter objects.
- **Spring Data integration**: Supports pagination, sorting, and JdbcTemplate usage.
- **External SQL files as template engines**: SQL files are treated as template engines using Freemarker. This means you can add conditionals, loops, and other logic directly in your SQL files, making queries highly dynamic and maintainable. Each Freemarker command is placed inside SQL comments (e.g., `-- <#if ...>`), so the SQL remains valid and readable even outside the application. This approach allows you to:
    - Write flexible queries that adapt to input parameters.
    - Keep your SQL files clean and versionable.
    - Use advanced logic without breaking SQL compatibility.
- **SQL file name convention**: The SQL file name must match the method name in the interface that extends `NativeQuery`. For example, a method `findSales` will use the SQL from `findSales.sql`.
- **Spring Bean generation**: A Spring Bean is automatically generated for each interface that extends `NativeQuery`, allowing you to inject this bean directly into your services or controllers.
- **File name override annotation**: You can override the SQL file name using an annotation, making it possible to use different file names for methods or even reuse the same SQL file for multiple methods, as shown in the example below.
- **JdbcTemplate or EntityManager support**: You can choose between using JdbcTemplate or EntityManager for query execution, depending on your application's needs.

---

# Usage Example

In this example, SpringNativeQuery is used to:
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
      com/grupopan/demo/nativequery/
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

<!-- Flyway for database migrations (optional, but recommended) -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

## Code Examples

### NativequeryApplication.java
```java
package com.grupopan.demo.nativequery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

// Main Spring Boot application class
@SpringBootApplication
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
package com.grupopan.demo.nativequery;

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
package com.grupopan.demo.nativequery;

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
package com.grupopan.demo.nativequery;

import io.github.gasparbarancelli.NativeQueryOperator;
import io.github.gasparbarancelli.NativeQueryParam;

// Filter for sales queries
public record SalesFilter(
        Number id, // Sale ID
        @NativeQueryParam(value = "name", operator = NativeQueryOperator.CONTAINING)
        String customerName // Customer name for search
) {
}
```

### SalesNativeQuery.java
```java
package com.grupopan.demo.nativequery;

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
    List<SaleCustomerResult> findSalesCustomers();
    // Paginated query of sales by customer
    Page<SaleCustomerResult> findSalesCustomers(Pageable pageable);
    // Query all sales
    @NativeQueryUseJdbcTemplate
    List<SaleFullResult> findSales();
    // Query sales by customer ID
    @NativeQueryUseJdbcTemplate
    @NativeQueryFileName("findSales")
    List<SaleFullResult> findSalesByCustomerId(@NativeQueryParam(value = "customerId") int customerId);
    // Query sales using filter
    @NativeQueryUseJdbcTemplate
    List<SaleFullResult> findSales(@NativeQueryParam(value = "filter", addChildren = true) SalesFilter filter);
    // Dynamic query using parameter map
    @NativeQueryUseJdbcTemplate
    List<SaleFullResult> findSales(Map<String, Object> params);
}
```

### SalesController.java
```java
package com.grupopan.demo.nativequery;

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
native-query.package-scan=com.grupopan.demo.nativequery
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

### NativequeryApplicationTests.java
```java
package com.grupopan.demo.nativequery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// Application context test
@SpringBootTest
class NativequeryApplicationTests {
    @Test
    void contextLoads() {
        // Tests if the context loads correctly
    }
}
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

[Return to main documentation](index.md)

