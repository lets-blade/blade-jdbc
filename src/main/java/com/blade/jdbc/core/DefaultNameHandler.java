package com.blade.jdbc.core;

import com.blade.jdbc.annotation.Table;
import com.blade.jdbc.utils.NameUtils;

/**
 * 默认名称处理handler
 */
public class DefaultNameHandler implements NameHandler {

    /**
     * 根据实体名获取表名
     *
     * @param entityClass
     * @return
     */
    public String getTableName(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if(null != table){
            return table.name();
        }
        //Java属性的骆驼命名法转换回数据库下划线“_”分隔的格式
        return NameUtils.getUnderlineName(entityClass.getSimpleName());
    }

    /**
     * 根据表名获取主键名
     *
     * @param entityClass
     * @return
     */
    public String getPKName(Class<?> entityClass) {
        Table table = entityClass.getAnnotation(Table.class);
        if(null != table){
            return table.pk();
        }
        return "id";
    }

    /**
     * 根据属性名获取列名
     *
     * @param fieldName
     * @return
     */
    public String getColumnName(String fieldName) {
        return NameUtils.getUnderlineName(fieldName);
    }

    /**
     * 根据实体名获取主键值 自增类主键数据库直接返回null即可
     *
     * @param entityClass the entity class
     * @param dialect the dialect
     * @return pK value
     */
    public String getPKValue(Class<?> entityClass, String dialect) {
        if (dialect.equalsIgnoreCase("oracle")) {
            //获取序列就可以了，默认seq_加上表名为序列名
            String tableName = this.getTableName(entityClass);
            return String.format("SEQ_%s.NEXTVAL", tableName);
        }
        return null;
    }
}
