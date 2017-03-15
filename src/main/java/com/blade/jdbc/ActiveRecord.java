package com.blade.jdbc;

import com.blade.jdbc.model.PageRow;
import com.blade.jdbc.model.Paginator;
import com.blade.jdbc.core.Take;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.io.Serializable;
import java.sql.Connection;
import java.util.List;
import java.util.Map;

public interface ActiveRecord {

    /**
     * 插入一条记录 自动处理主键
     *
     * @param entity
     * @return
     */
    <T extends Serializable> T insert(Object entity);

    /**
     * 插入一条记录 自动处理主键
     *
     * @param take the criteria
     * @return long long
     */
    <T extends Serializable> T insert(Take take);

    /**
     * 保存一条记录，不处理主键
     *
     * @param entity
     */
    void save(Object entity);

    /**
     * 保存一条记录，不处理主键
     *
     * @param take the criteria
     */
    void save(Take take);

    /**
     * 保存或者更新记录
     *
     * @param take
     */
    <T extends Serializable> T saveOrUpdate(Take take);

    /**
     * 保存或者更新记录
     *
     * @param entity
     */
    <T extends Serializable> T saveOrUpdate(Object entity);

    /**
     * 根据Criteria更新
     *
     * @param take the criteria
     */
    int update(Take take);

    /**
     * 根据实体更新
     *
     * @param entity the entity
     */
    int update(Object entity);

    /**
     * 根据Criteria删除
     *
     * @param take the criteria
     */
    int delete(Take take);

    /**
     * 删除记录 此方法会以实体中不为空的字段为条件
     *
     * @param entity
     */
    int delete(Object entity);

    /**
     * 删除记录
     *
     * @param clazz the clazz
     * @param id the id
     */
    int delete(Class<?> clazz, Serializable id);

    /**
     * 删除所有记录(TRUNCATE ddl权限)
     *
     * @param clazz the clazz
     */
    int deleteAll(Class<?> clazz);

    /**
     * 按设置的条件查询
     *
     * @param <T>  the type parameter
     * @param take the criteria
     * @return list
     */
    <T extends Serializable> List<T> list(Take take);

    /**
     * 根据sql查询List<Map>数据
     *
     * @param sql
     * @param args
     * @return
     */
    List<Map<String, Object>> listMap(String sql, Object... args);

    /**
     * 根据sql查询List<Map>数据，带分页
     *
     * @param sql
     * @param pageRow
     * @param args
     * @return
     */
    List<Map<String, Object>> listMap(String sql, PageRow pageRow, Object... args);

    /**
     * 根据sql查询列表
     *
     * @param sql
     * @param type
     * @param args
     * @param <T>
     * @return
     */
    <T extends Serializable> List<T> list(Class<T> type, String sql, Object... args);

    /**
     * 根据sql查询列表，带分页
     *
     * @param type
     * @param sql
     * @param pageRow
     * @param args
     * @param <T>
     * @return
     */
    <T extends Serializable> List<T> list(Class<T> type, String sql, PageRow pageRow, Object... args);

    /**
     * 查询列表
     *
     * @param entity the entity
     * @return the list
     */
    <T extends Serializable> List<T> list(T entity);

    /**
     * 查询列表
     *
     * @param <T>  the type parameter
     * @param entity the entity
     * @param take the criteria
     * @return the list
     */
    <T extends Serializable> List<T> list(T entity, Take take);

    /**
     * 查询返回map
     *
     * @param sql
     * @param args
     * @return
     */
    Map<String, Object> map(String sql, Object...args);

    /**
     * 查询一列
     *
     * @param type
     * @param sql
     * @param args
     * @param <T>
     * @return
     */
    <T extends Serializable> T one(Class<T> type, String sql, Object...args);

    /**
     * 查询记录数
     *
     * @param entity
     * @return
     */
    int count(Object entity);

    /**
     * 查询记录数
     *
     * @param take the criteria
     * @return int int
     */
    int count(Take take);

    /**
     * 查询记录数
     *
     * @param entity the entity
     * @param take the criteria
     * @return int int
     */
    int count(Object entity, Take take);

    /**
     * 根据主键得到记录
     *
     * @param <T>  the type parameter
     * @param clazz the clazz
     * @param pk
     * @return t
     */
    <T extends Serializable> T byId(Class<T> clazz, Serializable pk);

    /**
     * 根据主键得到记录
     *
     * @param <T>  the type parameter
     * @param take the criteria
     * @param pk
     * @return t
     */
    <T extends Serializable> T byId(Take take, Serializable pk);

    /**
     * 查询单个记录
     *
     * @param <T>   the type parameter
     * @param entity the entity
     * @return t t
     */
    <T extends Serializable> T one(T entity);

    /**
     * 查询单个记录
     *
     * @param <T>     the type parameter
     * @param take the criteria
     * @return t t
     */
    <T extends Serializable> T one(Take take);

    /**
     *
     * @param sql
     * @param args
     * @return
     */
    int execute(String sql, Object...args);

    /**
     * 分页查询
     *
     * @param entity
     * @param page
     * @param limit
     * @param <T>
     * @return
     */
    <T> Paginator<T> page(T entity, int page, int limit);

    /**
     * 分页查询
     *
     * @param entity
     * @param page
     * @param limit
     * @param orderBy
     * @param <T>
     * @return
     */
    <T> Paginator<T> page(T entity, int page, int limit, String orderBy);

    /**
     * 分页查询
     *
     * @param entity
     * @param pageRow
     * @param <T>
     * @return
     */
    <T> Paginator<T> page(T entity, PageRow pageRow);

    /**
     * 分页查询
     *
     * @param take
     * @param <T>
     * @return
     */
    <T> Paginator<T> page(Take take);

    /**
     * 返回sql2o对象
     *
     * @return
     */
    Sql2o sql2o();

    /**
     * 返回数据源对象
     *
     * @return
     */
    DataSource datasource();

    /**
     * 返回SQL连接对象
     *
     * @return
     */
    Connection connection();
}
