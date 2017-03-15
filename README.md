# blade-jdbc

[![Build Status](https://img.shields.io/travis/bladejava/blade-jdbc.svg?style=flat-square)](https://travis-ci.org/bladejava/blade-jdbc)
[![maven-central](https://img.shields.io/maven-central/v/com.bladejava/blade-jdbc.svg?style=flat-square)](http://search.maven.org/#search%7Cga%7C1%7Cblade-jdbc)
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

## 特性

- 语法简洁，代码量少
- 不依赖第三方框架(除了日志接口)
- DSL风格，让程序写起来像一条链
- 内置连接池，支持其他连接池
- 可无缝结合spring

废话少说，上车请看 test code.

## 更新日志

### v0.1.6-alpha

1. 取消Spring支持
2. 添加返回 `Sql2o`, `DataSource`, `Connection` 对象
3. 添加 `saveOrUpdate` 方法
4. 添加查询列表可分页
5. 添加 `in` 可传入 `List`
6. 修复分页传入数据小于0
7. 修复自定义列名bug
8. 重写事务实现