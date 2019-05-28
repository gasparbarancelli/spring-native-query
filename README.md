# spring-native-query
![Github Issues](https://img.shields.io/github/issues/gasparbarancelli/spring-native-query.svg) ![Github Stars](https://img.shields.io/github/stars/gasparbarancelli/spring-native-query.svg) ![Java](https://img.shields.io/badge/java-100%25-brightgreen.svg) ![Twitter](https://img.shields.io/twitter/url/https/github.com%2Fgasparbarancelli%2Fspring-native-query.svg) ![LICENSE](https://img.shields.io/badge/license-MIT-blue.svg)

# about spring-native-query

This project aims to facilitate the execution of native queries in the database.

The idea of ​​the project is to run queries by convention, similar to Spring Data.

When creating a new interface that extends the NativeQuery interface, we create false objects of these interfaces, which we use proxy to intercept the method calls and execute the queries, at the end we register the beans of these interfaces dynamically, thus being able to inject the interfaces in all components of the Spring.

The convention works the following way, the method name is the name of the file that will contain the query sql, the parameters of the methods will be passed as parameters to the entity manager, the return of the method is the object that will be transformed with the return of the Query.

The file that contains the SQL query is a Jtwig template, where we can apply validations in the query, a practical example to explain why we use template and not just a file containing SQL simply is the following, if you have a dynamic query, that conforms the values ​​of the methods you want to apply add or not a WHERE in the query, or modify it by completeness. The parameters of the methods besides being passed to the entity manager to execute the query are sent to the template.

Files with queries must be added to a folder named "nativeQuery" inside the resources folder. Remember, the file name must be the same as the method name.

# Example

Here is an example of using the framework.

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

In your project add the dependency of the library, let's take an example using maven.
```
<dependency>
    <groupId>io.github.gasparbarancelli</groupId>
    <artifactId>spring-native-query</artifactId>
    <version>1.0.1</version>
</dependency>
```    

First define in your configuration file the package scan of your project, The files application.properties, bootstrap.yml and bootstrap.yaml are supported, the property.

If you use properties file
``` properties
native-query.package-scan=io.github.gasparbarancelli.demospringnativequery
```
If you use yml file
``` yml
native-query:
  package-scan: io.github.gasparbarancelli.demospringnativequery
```

> UserTO file example
```java
import lombok.*;

@Data
public class UserTO {

  private Number id;
  private String name;

}
```

> UserTO file example
```java
import io.github.gasparbarancelli.NativeQueryOperator;
import io.github.gasparbarancelli.NativeQueryParam;
import lombok.*;

@Data
public class UserFilter {
  private Number id;
  
  /*
    Custom operator, when add parameter value in query and jwitg, the paramter is transformed
  */
  @NativeQueryParam(value = "name", operator = NativeQueryOperator.CONTAINING)
  private String name;

}
```

> UserNativeQUery file example
```java
import io.github.gasparbarancelli.NativeQuery;
import io.github.gasparbarancelli.NativeQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface UserNativeQuery extends NativeQuery {

  List<UserTO> findUsers();
  
  /*
    Add fields children of parameter
  */
  List<UserTO> findUsersByFilter(@NativeQueryParam(value = "filter", addChildren = true) UserFilter filter);
  
  /*
    Add pagination
  */
  List<UserTO> findActiveUsers(Pageable pageable);
  
  /*
    Add pagination and return object with values for the pagination (count, page, size)
  */
  Page<UserTO> findActiveUsersWithPage(Pageable pageable);
  
  /*
    Custom parameter name
  */
  UserTO findUserById(@NativeQueryParam(value = "codigo") Number id);
  
  List<Number> getUsersId();
  
  String getUserName(Number id);
  
}
```

> findUsers.twig file example
```sql
SELECT cod as "id", full_name as "name" FROM USER
```

> findUsersByFilter.twig file example, only add parameter when variables is not null
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

> findActiveUsers.twig file example
```sql
SELECT cod as "id", full_name as "name" FROM USER WHERE ACTIVE = true
```

> findActiveUsersWithPage.twig file example
```sql
SELECT cod as "id", full_name as "name" FROM USER WHERE ACTIVE = true
```

> findUserById.twig file example
```sql
SELECT cod as "id", full_name as "name" FROM USER WHERE cod = :codigo
```

> getUsersId.twig file example
```sql
SELECT cod as "id" FROM USER
```

> getUserName.twig file example
```sql
SELECT full_name as "name" FROM USER WHERE cod = :id
```

> UserController file example
```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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

}
```
