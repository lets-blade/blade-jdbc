package com.blade.jdbc;

import com.blade.jdbc.model.PageRow;
import com.blade.jdbc.model.Paginator;
import com.blade.jdbc.core.Take;
import com.blade.jdbc.tx.AtomTx;

import java.io.Serializable;
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
     * @param criteria the criteria
     * @return long long
     */
    <T extends Serializable> T insert(Take criteria);

    /**
     * 保存一条记录，不处理主键
     *
     * @param entity
     */
    void save(Object entity);

    /**
     * 保存一条记录，不处理主键
     *
     * @param criteria the criteria
     */
    void save(Take criteria);

    /**
     * 根据Criteria更新
     *
     * @param criteria the criteria
     */
    int update(Take criteria);

    /**
     * 根据实体更新
     *
     * @param entity the entity
     */
    int update(Object entity);

    /**
     * 根据Criteria删除
     *
     * @param criteria the criteria
     */
    int delete(Take criteria);

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
     * @param criteria the criteria
     * @return the list
     */
    <T extends Serializable> List<T> list(T entity, Take criteria);

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
     * @param criteria the criteria
     * @return int int
     */
    int count(Take criteria);

    /**
     * 查询记录数
     *
     * @param entity the entity
     * @param criteria the criteria
     * @return int int
     */
    int count(Object entity, Take criteria);

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
     * @param criteria the criteria
     * @param pk
     * @return t
     */
    <T extends Serializable> T byId(Take criteria, Serializable pk);

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
     * @param criteria the criteria
     * @return t t
     */
    <T extends Serializable> T one(Take criteria);

    /**
     *
     * @param sql
     * @param args
     * @return
     */
    int execute(String sql, Object...args);

    /**
     * 运行事务
     *
     * @param tx
     */
    void run(AtomTx tx);

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

}
