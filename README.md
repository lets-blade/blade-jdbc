# blade-jdbc

[![Build Status](https://img.shields.io/travis/bladejava/blade-jdbc.svg?style=flat-square)](https://travis-ci.org/bladejava/blade-jdbc)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

blade-jdbc是基于sql2o二次开发的一个简洁的ORM库，可作为学习使用。

## 使用

```java
// 连接数据库
DB.open("com.mysql.jdbc.Driver", "jdbc:mysql://127.0.0.1/test", "root", "root");

// 查询列表
List<User> users = AR.find("where age > 20 order by id desc limit ?,?", 
	0, 3).list(User.class);

// 按主键查询
User user = AR.findById(User.class, 25);

// 查询记录数
long c1 = AR.executeSQL("select count(1) from user_t").count();

long c2 = AR.find("select count(1) from user_t").first(Long.class);

// 使用对象查询
QueryParam queryParam = QueryParam.me();
queryParam.eq("age", 19).lt("update_time", 1409126303).like("title", "王尼玛%");

List<Post> posts = AR.find(queryParam).list(Post.class);

// 更新操作
int c1 = AR.executeSQL("update user_t set password = 'haha' where id = 26").executeUpdate();

int c2 = AR.executeSQL("update user_t set password = ? where id = ?", "haha2", 26).executeUpdate();

int c3 = AR.update("update user_t set password = 'haha3' where id = 26").executeUpdate();

int c4 = AR.update("update user_t set password = ? where id = ?", "haha4", 26).executeUpdate();

// 启用缓存
DB.setCache(new FIFOCache());
```

[测试代码](https://github.com/bladejava/blade-jdbc/tree/master/src/test/java/com/blade/jdbc/test)

