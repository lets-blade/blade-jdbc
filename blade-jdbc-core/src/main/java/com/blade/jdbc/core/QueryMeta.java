package com.blade.jdbc.core;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author biezhi
 * @date 2017/7/25
 */
@Data
@NoArgsConstructor
public class QueryMeta {

    private String              sql;
    private Object[]            params;
    private Map<String, String> columnMapping;

    public QueryMeta(String sql, Object[] params) {
        this.sql = sql;
        this.params = params;
    }

    public void addColumnMapping(String columnName, String fieldName) {
        if (null == columnMapping) {
            columnMapping = new HashMap<>(8);
        }
        columnMapping.put(columnName, fieldName);
    }

    public boolean hasColumnMapping() {
        return null != columnMapping && !columnMapping.isEmpty();
    }

}
