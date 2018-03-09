# Blade JDBC

当你需要存储或读取数据的时候很可能就需要用到数据库了，Blade不限制你使用什么数据库框架，
为了让开发更高效，Blade提供了一款简单的类 `ActiveRecord` 框架 [blade-jdbc](https://github.com/lets-blade/blade-jdbc)
用于操作关系型数据库，目前只在 `MySQL` 尝试过。如果你有更熟悉的数据库框架也可以自行取决。

## 数据库表脚本

```sql
CREATE TABLE `t_user` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(20) DEFAULT NULL,
  `password` varchar(50) DEFAULT NULL,
  `real_name` varchar(20) DEFAULT NULL,
  `age` int(10) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

INSERT INTO `t_user` (`id`, `username`, `password`, `real_name`, `age`)
VALUES
	(1, 'aaa', 'aaa', 'aaa', 32),
	(2, 'jack', '999', '杰克65', 29),
	(3, 'jack_up', '123556', 'aaa', 19),
	(4, 'jack', '123556', '杰克', 20),
	(5, 'jack', '123556', '杰克', 20);
```


## 配置 Blade-JDBC

先引入 `mysql-connector-java` 和 `blade-jdbc-core` 的较新版本。

```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>5.1.41</version>
</dependency>
<dependency>
    <groupId>com.bladejava</groupId>
    <artifactId>blade-jdbc-core</artifactId>
    <version>0.0.4-RELEASE</version>
</dependency>
```

> blade-jdbc-core 支持 MySQL 和 SQLite，如需使用 `postgresql` 需添加 `blade-jdbc-postgresql` 依赖 

加载数据库并配置数据库

```java
@Bean
public class LoadConfig implements BeanProcessor {

    @Override
    public void processor(Blade blade) {
        Base.open("jdbc:mysql://127.0.0.1:3306/app", "root", "123456");
    }
}
```

创建一个数据库实体类 `User.java`

```java
@Table(value = "t_user")
public class User extends ActiveRecord {

    private Integer id;
    private String  username;
    private String  password;
    private Integer age;
    private String  realName;

    // getter and setter
}
```

在 `User` 类上有个 `@Table` 注解来标识表名和主键，当主键为 `id` 的时候可以不配置，
同时继承了 `ActiveRecord`，这样就赋予了 `User` 操作数据库的能力。

## CRUD

**插入数据**

```java
User user = new User();
user.setUsername("jack");
user.setPassword("123556");
user.setRealName("杰克");
user.setAge(20);

// insert into t_user (id, username, password, age, real_name) values (?, ?, ?, ?, ?)
user.save();
```

**删除数据**

```java
User user = new User();
user.where("id", 44).delete();
// delete from t_user where id = ?
user.delete("id", 44);
user.delete(44);
```

```java
// 删除所有数据，请慎用
User user = new User();
user.delete();
```

**更新数据**

```java
User user = new User();
user.setUsername("jack_up");
// update t_user set username = ? where id = ?
user.where("id", 43).update();
```

```java
User user = new User();
user.setAge(19);
// update t_user set age = ? where id = ?
user.update(43);
```

```java
User user = new User();
user.setAge(32);
// update t_user set age = ? where age < ?
user.where("age", "<", 20).update();
```

```java
new User().execute("update t_user set age = 22 where age < 20");
```

**查询数据**

```java
User       user  = new User();
List<User> users = user.findAll();
System.out.println(users);

user.setId(1);
users = user.findAll();
System.out.println(users);

user.where("id", 2);
users = user.findAll();
System.out.println(users);

user.where("id", "<", 2);
users = user.findAll();
System.out.println(users);
```

```java
User user = new User();
// select * from t_user where id = ?
User u1 = user.find(1);
System.out.println(u1);
```

```java
User       user = new User();
List<User> list = user.where("username", "jack").findAll();
System.out.println(list);

// user.where(User::getUsername).is("jack").findAll();

list = user.findAll(Fields.of("username"), OrderBy.desc("id"));
System.out.println(list);
```

```java
User       user  = new User();
List<User> users = user.like("username", "%jac%").and("age", ">", 18).findAll();
System.out.println(users);
```

**查询记录数**

```java
User user  = new User();
long count = user.count();
System.out.println(count);
```

```java
User user = new User();
user.where("username", "jack").or("real_name", "jack");
long count = user.count();
System.out.println("count=" + count);
```

**写SQL查询列表**

```java
User user = new User();
System.out.println(user.queryAll("select * from t_user"));
System.out.println(user.queryAll("select * from t_user where id = ?", 1));
```

**写SQL查询单条**

```java
User user = new User();
System.out.println(user.query("select * from t_user order by id desc"));
System.out.println(user.query("select * from t_user where id = ?", 1));
```

## 分页查询

```java
User       user = new User();
Page<User> page = user.page(1, 3);
System.out.println(page);
```

```java
User       user = new User();
Page<User> page = user.page(new PageRow(1, 2));
System.out.println(page);
```

**转换结果集**

```java
User         user         = new User();
Page<User>   page         = user.page(1, 10);
Page<String> userNamePage = page.map(u -> u.getUsername());
System.out.println(userNamePage);
```

## 事务操作

```java
Base.atomic(() -> {
    User user = new User();
    user.setPassword("999");
    user.update(42);
    // int a = 1 / 0;
    System.out.println("aasdasd");
    return true;
});
```

## 更新日志

### v0.0.4-RELEASE

1. 暂时去除 `oracle` 支持
2. 修复 `MyiSAM` 存储引擎下自动 `commit` bug
3. 添加 `gt`、`lt`、`isNull`、`isNotNull` 方法

### v0.0.1-RELEASE

1. 支持多数据库 (mysql, oracle, db2, postgresql)
2. 修复事务回滚连接已关闭bug
3. 分离核心依赖

### v0.2.2

1. 修复 `in` 查询问题
2. 添加单条自定义类型查询
3. 添加查询自定义类型列表
4. SQL关键词大写

### v0.2.0

1. 使用Java8重构
2. 更好的操作ActiveRecord

### v0.1.6

1. 修复分页bug

### v0.1.6-alpha

1. 取消Spring支持
2. 添加返回 `Sql2o`, `DataSource`, `Connection` 对象
3. 添加 `saveOrUpdate` 方法
4. 添加查询列表可分页
5. 添加 `in` 可传入 `List`
6. 修复分页传入数据小于0
7. 修复自定义列名bug
8. 重写事务实现