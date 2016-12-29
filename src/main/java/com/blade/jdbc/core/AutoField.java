package com.blade.jdbc.core;

/**
 * 组装sql时的列信息
 */
public class AutoField {
    
    /** sql中的update 字段 */
    public static final int UPDATE_FIELD = 1;

    /** sql中的where 字段 */
    public static final int WHERE_FIELD  = 2;

    /** 排序字段 */
    public static final int ORDER_BY_FIELD = 3;

    /** 主键值名称 例如oracle的序列名，非直接主键值 */
    public static final int PK_VALUE_NAME = 4;

    /** 名称 */
    private String          name;

    /** 操作符 and or */
    private String          sqlOperator;

    /** 本身操作符 值大于、小于、in等 */
    private String          fieldOperator;

    /** 值 */
    private Object[]        values;

    /** 类型 对应上面申明的常量 */
    private int             type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object[] getValues() {
        return values;
    }

    public void setValues(Object[] values) {
        this.values = values;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSqlOperator() {
        return sqlOperator;
    }

    public void setSqlOperator(String sqlOperator) {
        this.sqlOperator = sqlOperator;
    }

    public String getFieldOperator() {
        return fieldOperator;
    }

    public void setFieldOperator(String fieldOperator) {
        this.fieldOperator = fieldOperator;
    }
}
