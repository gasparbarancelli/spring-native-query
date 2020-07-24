# spring-native-query
![Github Issues](https://img.shields.io/github/issues/gasparbarancelli/spring-native-query.svg) ![Github Stars](https://img.shields.io/github/stars/gasparbarancelli/spring-native-query.svg) ![Java](https://img.shields.io/badge/java-100%25-brightgreen.svg) ![Twitter](https://img.shields.io/twitter/url/https/github.com%2Fgasparbarancelli%2Fspring-native-query.svg) ![LICENSE](https://img.shields.io/badge/license-MIT-blue.svg)

# about spring-native-query

Running native queries to relational database using Java often leaves the source code confusing and extensive, when one has too many filter conditions and also changes in table bindings.

Because of this I decided to create the "Spring Native Query" library to facilitate the execution of native queries, with a focus on simplifying the source code, making it more readable and clean, creating files that contain the native queries and dynamically injecting assets to execute those queries.

The library's idea is to run convention queries, similar to Spring Data, and was built to work only with Spring Boot and Spring Data Jpa.

When creating a new interface that extends the NativeQuery interface, we create fake objects from these interfaces, where we use proxy to intercept method calls and execute queries, in the end we register the beans of those interfaces dynamically, so we can inject the interfaces into all the components of the Spring.

The convention works as follows, the method name is the name of the file that contains the sql query, the parameters of the methods will be passed as parameters to the entity manager, the method return is the object that will be transformed with the result returned from the query.

The file that contains the SQL query is a Jtwig template, where we can apply validations modifying the whole query, adding filters, changing links between tables, finally any changes in sql.

By default native query files must be added to a folder named "nativeQuery" inside the resource folder. Remember, the file name must be the same as the method name.

# Example

Here are some examples for a better understanding. Let's create a Spring Boot project with dependence, Spring Data Jpa and the H2 database. When starting the project, let's create a sql script by creating a new table and inserting some records. All sample source code is in [github](https://github.com/gasparbarancelli/demo-spring-native-query).

In your project add the dependency of the library, let's take an example using maven.

```
<dependency>
    <groupId>io.github.gasparbarancelli</groupId>
    <artifactId>spring-native-query</artifactId>
    <version>1.0.25</version>
</dependency>
```    

Inside the resource folder create a file named data.sql and insert the script.

```sql
CREATE TABLE USER (
  cod INT NOT NULL,
  full_name VARCHAR(45) NULL,
  active INT NULL,
  PRIMARY KEY (cod)
);

INSERT INTO USER (cod, full_name, active)
VALUES (1, 'Gaspar', 1),
       (2, 'Elton', 1),
       (3, 'Lucini', 1),
       (4, 'Diogo', 1),
       (5, 'Daniel', 1),
       (6, 'Marcos', 1),
       (7, 'Fernanda', 1),
       (8, 'Maicon', 1),
       (9, 'Rafael', 0);
```

First define in your configuration file the package scan of your project, The files application.properties, bootstrap.properties, application.yaml, application.yml, bootstrap.yml and bootstrap.yaml are supported, the property.

If you use properties file

``` properties
native-query.package-scan=io.github.gasparbarancelli.demospringnativequery
native-query.file.sufix=sql
```
If you use yml file

``` yml
native-query:
  package-scan: io.github.gasparbarancelli.demospringnativequery
  file:
    sufix: sql
```

We can also define programatically implementing the interface NativeQueryConfig.

``` java
import io.github.gasparbarancelli.NativeQueryConfig;

public class NativeQueryDefaultConfig implements NativeQueryConfig {

    @Override
    public String getPackageScan() {
        return "io.github.gasparbarancelli.demospringnativequery";
    }

    @Override
    public String getFileSufix() {
        return "sql";
    }

}
```

UserTO file example

```java
import lombok.*;

@Data
public class UserTO {

  private Number id;
  private String name;

}
```

UserTO file example

```java
import io.github.gasparbarancelli.NativeQueryOperator;
import io.github.gasparbarancelli.NativeQueryParam;
import lombok.*;

@Data
public class UserFilter {
  private Number id;
  
  // Custom operator, when add parameter value in query and jwitg, the paramter is transformed
  @NativeQueryParam(value = "name", operator = NativeQueryOperator.CONTAINING)
  private String name;

}
```

UserNativeQUery file example

```java
import io.github.gasparbarancelli.NativeQuery;
import io.github.gasparbarancelli.NativeQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;


public interface UserNativeQuery extends NativeQuery {

  List<UserTO> findUsers();

  // When using the NativeQuerySql annotation it is not necessary to have the file containing the sql statement
  @NativeQuerySql("SELECT cod as \"id\", full_name as \"name\" FROM USER")
  List<UserTO> findBySqlInline();

  List<UserTO> findWithMap(Map<String, Object> params);
  
  // Add fields children of parameter
  List<UserTO> findUsersByFilter(@NativeQueryParam(value = "filter", addChildren = true) UserFilter filter);
  
  // Add pagination
  List<UserTO> findActiveUsers(Pageable pageable);

  // Ordering
  List<UserTO> findActiveUsersWithSort(Sort sort);

  // Add pagination and return object with values for the pagination (count, page, size)
  Page<UserTO> findActiveUsersWithPage(Pageable pageable);
  
  // Custom parameter name
  UserTO findUserById(@NativeQueryParam(value = "codigo") Number id);
  
  List<Number> getUsersId();
  
  String getUserName(Number id);
  
  Optional<String> getOptionalUserName(Number id);
  
  Optional<UserTO> findOptionalUserById(@NativeQueryParam(value = "codigo") Number id);
  
}
```

findUsers.sql file example

```sql
SELECT cod as "id", full_name as "name" FROM USER
```

findWithMap.sql file example

```sql
SELECT cod as "id", full_name as "name" FROM USER
WHERE 1=1
/* for item in params */
AND {{item}} = :{{item}}
/* endfor */
```


findUsersByFilter.sql file example, only add parameter when variables is not null

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

findActiveUsers.sql file example

```sql
SELECT cod as "id", full_name as "name" FROM USER WHERE ACTIVE = true
```

findActiveUsersWithPage.sql file example

```sql
SELECT cod as "id", full_name as "name" FROM USER WHERE ACTIVE = true
```

findActiveUsersWithSort.sql file example

```sql
SELECT cod as "id", full_name as "name" FROM USER WHERE ACTIVE = true
```

findUserById.sql file example

```sql
SELECT cod as "id", full_name as "name" FROM USER WHERE cod = :codigo
```

getUsersId.sql file example

```sql
SELECT cod as "id" FROM USER
```

getUserName.sql file example

```sql
SELECT full_name as "name" FROM USER WHERE cod = :id
```

getOptionalUserName.sql file example

```sql
SELECT full_name as "name" FROM USER WHERE cod = :id
```

findOptionalUserById.sql file example

```sql
SELECT cod as "id", full_name as "name" FROM USER WHERE cod = :codigo
```

UserController file example

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {

  @Autowired private UserNativeQuery userNativeQuery;
  
  @GetMapping()
  public List<UserTO> findUsers() {
    return userNativeQuery.findUsers();
  }
  
  @GetMapping("inline")
  public List<UserTO> findBySqlInline() {
    return userNativeQuery.findBySqlInline();
  }
  
  @GetMapping("map")
  public List<UserTO> findWithMap() {
    Map<String, Object> map = new HashMap<>();
    map.put("cod", 1);
    map.put("full_name", "Gaspar");
    return userNativeQuery.findWithMap(map);
  }

  @PostMapping("filter")
  public List<UserTO> findUsers(@RequestBody UserFilter filter) {
    return userNativeQuery.findUsersByFilter(filter);
  }
  
  @GetMapping("active")
  public List<UserTO> findUsers(
          @RequestParam(value = "page", defaultValue = "0", required = false) int page,
          @RequestParam(value = "size", defaultValue = "10", required = false) int size) {
    return userNativeQuery.findActiveUsers(PageRequest.of(page, size));
  }
  
  @GetMapping("activeWithPage")
  public Page<UserTO> findActiveUsersWithPage(
          @RequestParam(value = "page", defaultValue = "0", required = false) int page,
          @RequestParam(value = "size", defaultValue = "5", required = false) int size) {
    return userNativeQuery.findActiveUsersWithPage(PageRequest.of(page, size));
  }
 
  @GetMapping("activeWithSort")
  public List<UserTO> findActiveUsersWithSort(
        @RequestParam(value = "columnName") String columnName) {
    return userNativeQuery.findActiveUsersWithSort(Sort.by(columnName));
  }

  
  @GetMapping("{id}")
  public UserTO findUsers(@PathVariable("id") Number id) {
    return userNativeQuery.findUserById(id);
  }
  
  @GetMapping("ids")
  public List<Number> getIds() {
    return userNativeQuery.getUsersId();
  }
  
  @GetMapping("{id}/name")
  public String getUserName(@PathVariable("id") Number id) {
    return userNativeQuery.getUserName(id);
  }

  @GetMapping("{id}/optional/name")
  public Optional<String> getOptionalUserName(@PathVariable("id") Number id) {
    return userNativeQuery.getOptionalUserName(id);
  }

  @GetMapping("{id}/optional")
  public Optional<UserTO> findOptionalUser(@PathVariable("id") Number id) {
    return userNativeQuery.findOptionalUserById(id);
  }


}
```
