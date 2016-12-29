package com.blade.jdbc.core;

/**
 * 名称处理接口
 */
public interface NameHandler {

    /**
     * 根据实体名获取表名
     *
     * @param entityClass
     * @return
     */
    String getTableName(Class<?> entityClass);

    /**
     * 根据表名获取主键名
     * 
     * @param entityClass
     * @return
     */
    String getPKName(Class<?> entityClass);

    /**
     * 根据属性名获取列名
     *
     * @param fieldName
     * @return
     */
    String getColumnName(String fieldName);

    /**
     * 根据实体名获取主键序列名 oracle才有用 自增类主键数据库直接返回null即可
     *
     * @param entityClass the entity class
     * @param dialect the dialect
     * @return pK value
     */
    String getPKValue(Class<?> entityClass, String dialect);
}
