# Spring Native Query with Spring Boot 2 and 3

## About the Project

This project demonstrates the use of the [Spring Native Query](https://github.com/gasparbarancelli/spring-native-query) library in a Spring Boot 2 and 3 application, allowing the execution of native SQL queries in a safe, flexible, and decoupled way from JPA entities. Native Query maps results directly to DTOs or Java records, making data access more performant and simple.

**Highlights:**
- This project uses **Jtwig** as the template engine for SQL files, unlike the Spring Boot 4 example which uses Freemarker.
- Compatible with Spring Boot 2.x and 3.x.

## Main Features
- **Automatic mapping:** Query results are converted to Java classes using compatible field names.
- **Parameterized queries:** Allows passing parameters to native queries, preventing SQL Injection.
- **Dynamic filters:** Supports flexible filters via annotations and filter objects.
- **Integration with Spring Data:** Supports pagination, sorting, and usage of JdbcTemplate.
- **External SQL files as Jtwig templates:** SQL files can contain Jtwig commands inside SQL comments (e.g., `/* if ... */`), making queries dynamic while keeping SQL readable and versionable.
- **Naming convention:** The SQL file name must match the method name in the interface extending `NativeQuery`.
- **Automatic Bean generation:** For each interface extending `NativeQuery`, a Spring Bean is automatically generated.
- **Support for JdbcTemplate or EntityManager:** You can choose between JdbcTemplate or EntityManager for query execution.

## Project Structure

```
src/
  main/
    java/
      io/github/gasparbarancelli/demospringnativequery/
        DemoSpringNativeQueryApplication.java
        UserController.java
        UserFilter.java
        UserNativeQuery.java
        UserTO.java
    resources/
      application.properties
      data.sql
      nativeQuery/
        findActiveUsers.sql
        findActiveUsersWithPage.sql
        findActiveUsersWithSort.sql
        findOptionalUserById.sql
        findUserById.sql
        findUsers.sql
        findUsersByFilter.sql
        findWithMap.sql
        getOptionalUserName.sql
        getUserName.sql
        getUsersId.sql
```

## Dependencies
Add to your `pom.xml`:

To use NativeQuery in your Spring Boot 2 or 3 project, add the following dependency to your `pom.xml`:

For Spring Boot 2 (javax):
```xml
<dependency>
  <groupId>io.github.gasparbarancelli</groupId>
  <artifactId>spring-native-query</artifactId>
  <version>1.0.30</version>
</dependency>
```
For Spring Boot 3 (jakarta):
```xml
<dependency>
  <groupId>io.github.gasparbarancelli</groupId>
  <artifactId>spring-native-query</artifactId>
  <version>2.0.0</version>
</dependency>
```

```xml
<!-- Spring Boot Starter Data JPA -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>

<!-- Database driver -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Code Examples

### DemoSpringNativeQueryApplication.java
```java
package io.github.gasparbarancelli.demospringnativequery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Main application class for Spring Boot Native Query demo.
 */
@SpringBootApplication
@ComponentScan(basePackages = {"io.github"})
public class DemoSpringNativeQueryApplication {

    /**
     * Application entry point.
     */
    public static void main(String[] args) {
        SpringApplication.run(DemoSpringNativeQueryApplication.class, args);
    }

}
```

### UserTO.java
```java
/**
 * Data Transfer Object for User.
 */
public class UserTO {
    private Integer id; // User ID
    private BigDecimal height; // User height
    private String name; // User name
    // Getters and setters
}
```

### UserFilter.java
```java
package io.github.gasparbarancelli.demospringnativequery;

import io.github.gasparbarancelli.NativeQueryOperator;
import io.github.gasparbarancelli.NativeQueryParam;
import lombok.*;

/**
 * Filter object for querying users with dynamic parameters.
 */
public class UserFilter {

    private Number id; // User ID filter

    @NativeQueryParam(value = "name", operator = NativeQueryOperator.CONTAINING)
    private String name; // Name filter (contains)
    // Getters and setters
}
```

### UserNativeQuery.java
```java
package io.github.gasparbarancelli.demospringnativequery;

import io.github.gasparbarancelli.NativeQuery;
import io.github.gasparbarancelli.NativeQueryParam;
import io.github.gasparbarancelli.NativeQuerySql;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Native Query interface for User operations.
 */
@Repository
public interface UserNativeQuery extends NativeQuery {

    /**
     * Find all users.
     */
    List<UserTO> findUsers();

    /**
     * Find users using inline SQL.
     */
    @NativeQuerySql("SELECT cod as \"id\", full_name as \"name\" FROM USER")
    List<UserTO> findBySqlInline();

    /**
     * Find users using a map of parameters.
     */
    List<UserTO> findWithMap(Map<String, Object> params);

    /**
     * Find users by filter object.
     */
    List<UserTO> findUsersByFilter(@NativeQueryParam(value = "filter", addChildren = true) UserFilter filter);

    /**
     * Find active users with pagination.
     */
    List<UserTO> findActiveUsers(Pageable pageable);

    /**
     * Find active users with sorting.
     */
    List<UserTO> findActiveUsersWithSort(Sort sort);

    /**
     * Find active users with pagination (returns Page).
     */
    Page<UserTO> findActiveUsersWithPage(Pageable pageable);

    /**
     * Find user by ID.
     */
    UserTO findUserById(@NativeQueryParam(value = "codigo") Number id);

    /**
     * Get all user IDs.
     */
    List<Number> getUsersId();

    /**
     * Get user name by ID.
     */
    String getUserName(Number id);

    /**
     * Get user name by ID (Optional).
     */
    Optional<String> getOptionalUserName(Number id);

    /**
     * Find user by ID (Optional).
     */
    Optional<UserTO> findOptionalUserById(@NativeQueryParam(value = "codigo") Number id);

}
```

### UserController.java
```java
package io.github.gasparbarancelli.demospringnativequery;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST controller for User endpoints.
 */
@RestController
@RequestMapping("user")
public class UserController {

    private final UserNativeQuery userNativeQuery;

    /**
     * Constructor injection of UserNativeQuery.
     */
    public UserController(UserNativeQuery userNativeQuery) {
        this.userNativeQuery = userNativeQuery;
    }

    /**
     * Get all users.
     */
    @GetMapping()
    public List<UserTO> findUsers() {
        return userNativeQuery.findUsers();
    }

    /**
     * Get users using inline SQL.
     */
    @GetMapping("inline")
    public List<UserTO> findBySqlInline() {
        return userNativeQuery.findBySqlInline();
    }

    /**
     * Get users using parameters via Map.
     */
    @GetMapping("map")
    public List<UserTO> findWithMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("cod", 1);
        map.put("full_name", "Gaspar");
        return userNativeQuery.findWithMap(map);
    }

    /**
     * Get users using filter (POST).
     */
    @PostMapping("filter")
    public List<UserTO> findUsers(@RequestBody UserFilter filter) {
        return userNativeQuery.findUsersByFilter(filter);
    }

    /**
     * Get active users (pagination).
     */
    @GetMapping("active")
    public List<UserTO> findUsers(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size) {
        return userNativeQuery.findActiveUsers(PageRequest.of(page, size));
    }

    /**
     * Get active users with pagination (Page).
     */
    @GetMapping("activeWithPage")
    public Page<UserTO> findActiveUsersWithPage(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "5", required = false) int size) {
        return userNativeQuery.findActiveUsersWithPage(PageRequest.of(page, size));
    }

    /**
     * Get active users with sorting.
     */
    @GetMapping("activeWithSort")
    public List<UserTO> findActiveUsersWithSort(
            @RequestParam(value = "columnName") String columnName) {
        return userNativeQuery.findActiveUsersWithSort(Sort.by(columnName));
    }

    /**
     * Get user by ID.
     */
    @GetMapping("{id}")
    public UserTO findUser(@PathVariable("id") Number id) {
        return userNativeQuery.findUserById(id);
    }

    /**
     * Get all user IDs.
     */
    @GetMapping("ids")
    public List<Number> getIds() {
        return userNativeQuery.getUsersId();
    }

    /**
     * Get user name by ID.
     */
    @GetMapping("{id}/name")
    public String getUserName(@PathVariable("id") Number id) {
        return userNativeQuery.getUserName(id);
    }

    /**
     * Get user name by ID (Optional).
     */
    @GetMapping("{id}/optional/name")
    public Optional<String> getOptionalUserName(@PathVariable("id") Number id) {
        return userNativeQuery.getOptionalUserName(id);
    }

    /**
     * Get user by ID (Optional).
     */
    @GetMapping("{id}/optional")
    public Optional<UserTO> findOptionalUser(@PathVariable("id") Number id) {
        return userNativeQuery.findOptionalUserById(id);
    }

}
```

## SQL Examples

Below are all SQL files used in the project, with their respective names and contents:

### findActiveUsers.sql
```sql
SELECT cod as "id", full_name as "name" FROM USER WHERE ACTIVE = true
```

### findActiveUsersWithPage.sql
```sql
SELECT cod as "id", full_name as "name" FROM USER WHERE ACTIVE = true
```

### findActiveUsersWithSort.sql
```sql
SELECT cod as "id", full_name as "name" FROM USER WHERE ACTIVE = true
```

### findOptionalUserById.sql
```sql
SELECT cod as "id", full_name as "name" FROM USER WHERE cod = :codigo
```

### findUserById.sql
```sql
SELECT cod as "id", full_name as "name" FROM USER WHERE cod = :codigo
```

### findUsers.sql
```sql
SELECT cod as "id", height as "height", full_name as "name" FROM USER
```

### findUsersByFilter.sql
```sql
SELECT cod as "id", full_name as "name" FROM USER
WHERE 1=1
/* if (filterId != null) */
AND cod = :filterId
/* endif  */
/* if (filterName != null) */
AND full_name like :filterName
/* endif  */
```

### findWithMap.sql
```sql
SELECT cod as "id", full_name as "name" FROM USER
WHERE 1=1
/* for item in params */
AND {{item}} = :{{item}}
/* endfor */
```

### getOptionalUserName.sql
```sql
SELECT full_name as "name" FROM USER WHERE cod = :id
```

### getUserName.sql
```sql
SELECT full_name as "name" FROM USER WHERE cod = :id
```

### getUsersId.sql
```sql
SELECT cod as "id" FROM USER
```

## Configuration (application.properties)
```properties
native-query.package-scan=io.github.gasparbarancelli.demospringnativequery
native-query.file.sufix=sql
native-query.use-hibernate-types=false
```

## Testing Endpoints with cURL

Below are example cURL commands to test all API endpoints:

### 1. List all users
```bash
curl -X GET "http://localhost:8080/user"
```

### 2. Get users via inline SQL
```bash
curl -X GET "http://localhost:8080/user/inline"
```

### 3. Get users using parameters via Map
```bash
curl -X GET "http://localhost:8080/user/map"
```

### 4. Get users using filter (POST)
```bash
curl -X POST "http://localhost:8080/user/filter" \
     -H "Content-Type: application/json" \
     -d '{"id":1, "name":"Gaspar"}'
```

### 5. Get active users (pagination)
```bash
curl -X GET "http://localhost:8080/user/active?page=0&size=10"
```

### 6. Get active users with pagination (Page)
```bash
curl -X GET "http://localhost:8080/user/activeWithPage?page=0&size=5"
```

### 7. Get active users with sorting
```bash
curl -X GET "http://localhost:8080/user/activeWithSort?columnName=full_name"
```

### 8. Get user by ID
```bash
curl -X GET "http://localhost:8080/user/1"
```

### 9. Get all user IDs
```bash
curl -X GET "http://localhost:8080/user/ids"
```

### 10. Get user name by ID
```bash
curl -X GET "http://localhost:8080/user/1/name"
```

### 11. Get user name by ID (Optional)
```bash
curl -X GET "http://localhost:8080/user/1/optional/name"
```

### 12. Get user by ID (Optional)
```bash
curl -X GET "http://localhost:8080/user/1/optional"
```

## Notes
- SQL files use **Jtwig** for conditional and dynamic logic, always inside SQL comments (`/* if ... */`).
- Native Query makes it easy to map results directly to DTOs, without needing JPA entities.
- Adapt the examples according to your data model.
