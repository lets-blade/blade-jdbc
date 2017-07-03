package com.blade.jdbc;

import com.blade.jdbc.annotation.Table;
import com.blade.jdbc.annotation.Transient;
import com.blade.jdbc.helpers.ColumnWithValue;
import com.blade.jdbc.helpers.Config;
import com.blade.jdbc.helpers.SqlFragment;
import com.blade.jdbc.helpers.SqlInfo;
import com.blade.jdbc.utils.ArrayUtils;

import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author ifonly
 * @version 1.0 2015-12-04 12:58
 * @since JDK 1.6
 */
public class SqlBuilder {

    private static SqlFragment createWithValuesFragmentSql(Map<String, Object> propertyAndValues,
                                                           Set<String> saveOrUpdateProperties) {
        StringBuilder columnsFragment = new StringBuilder();
        StringBuilder valuesFragment = new StringBuilder();
        Set<Map.Entry<String, Object>> entrySet = propertyAndValues.entrySet();
        int size = saveOrUpdateProperties.size();
        int index = 0;
        int end = size - 1;
        Object[] params = new Object[size];

        for (Map.Entry<String, Object> entry : entrySet) {
            String column = entry.getKey();
            if (!saveOrUpdateProperties.contains(column)) {
                continue;
            }
            Object value = entry.getValue();

            columnsFragment.append(column);
            valuesFragment.append(Config.SQL_PLACEHOLDER);

            if (index != end) {
                columnsFragment.append(", ");
                valuesFragment.append(", ");
            }
            params[index++] = value;
        }

        return new SqlFragment(columnsFragment.toString(), valuesFragment.toString(), params);
    }


    private static SqlFragment createWithWheresFragmentSql(ColumnWithValue[] whereColumnAndValues) {
        StringBuilder wheresFragment = new StringBuilder();
        if (null == whereColumnAndValues || whereColumnAndValues.length == 0) {
            return null;
        }
        int index = 0;
        int length = whereColumnAndValues.length;
        int end = length - 1;
        Object[] params = new Object[length];

        whereCause(whereColumnAndValues, wheresFragment, index, end, params);

        return new SqlFragment(null, null, wheresFragment.toString(), params);
    }

    private static void whereCause(ColumnWithValue[] whereColumnAndValues, StringBuilder wheresFragment, int index, int end, Object[] params) {
        for (ColumnWithValue cv : whereColumnAndValues) {
            String column = cv.column;
            Object value = cv.value;

            wheresFragment.append(column).append(" = ").append(Config.SQL_PLACEHOLDER);
            if (index != end) {
                wheresFragment.append(", ");
            }
            params[index++] = value;
        }
    }

    private static SqlFragment createWithWheresFragmentSql(Map<String, Object> propertyAndValues,
                                                           Set<String> saveOrUpdateProperties,
                                                           ColumnWithValue[] whereColumnAndValues) {
        StringBuilder columnsFragment = new StringBuilder();
        StringBuilder wheresFragment = new StringBuilder();

        Set<Map.Entry<String, Object>> entrySet = propertyAndValues.entrySet();
        int index = 0;
        int size = saveOrUpdateProperties.size();
        int end = size - 1;
        Object[] params = new Object[size];

        for (Map.Entry<String, Object> entry : entrySet) {
            String column = entry.getKey();
            if (!saveOrUpdateProperties.contains(column)) {
                continue;
            }
            Object value = entry.getValue();
            columnsFragment.append(column).append(" = ").append(Config.SQL_PLACEHOLDER);
            if (index != end) {
                columnsFragment.append(", ");
            }

            params[index++] = value;
        }

        if (null != whereColumnAndValues && whereColumnAndValues.length > 0) {
            Object[] whereParams = new Object[whereColumnAndValues.length];
            index = 0;
            end = whereColumnAndValues.length - 1;

            whereCause(whereColumnAndValues, wheresFragment, index, end, whereParams);

            params = ArrayUtils.merge(params, whereParams);
        }

        return new SqlFragment(columnsFragment.toString(), null, wheresFragment.toString(), params);
    }

    public static String buildInsertSql(Class<?> modelType) {

        String tableName = getTableName(modelType);

        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(tableName);
        sb.append(" (");

        StringBuffer values = new StringBuffer(" values (");
        Stream.of(modelType.getDeclaredFields())
                .filter(field -> null == field.getAnnotation(Transient.class))
                .forEach(field -> {
                    sb.append(field.getName()).append(", ");
                    values.append(':').append(field.getName()).append(", ");
                });

        sb.append(')');
        values.append(')');

        return sb.append(values).toString().replace(", )", ")");
    }

    private static String getTableName(Class<?> modelType) {
        Table table = modelType.getAnnotation(Table.class);
        if (null != table) {
            return table.value().toLowerCase();
        }
        return modelType.getSimpleName().toLowerCase();
    }

    public static SqlInfo buildInsertSql(String tableName, Map<String, Object> propertyAndValues, Set<String> saveOrUpdateProperties) {
        if (propertyAndValues == null || propertyAndValues.isEmpty()) {
            throw new IllegalArgumentException("build insert sql failed");
        }
        SqlFragment sqlFragment = createWithValuesFragmentSql(propertyAndValues, saveOrUpdateProperties);
        StringBuilder sb = new StringBuilder("INSERT INTO ");
        sb.append(tableName);
        sb.append("(").append(sqlFragment.getColumnsFragment()).append(")");
        sb.append(" VALUES ");
        sb.append("(").append(sqlFragment.getValuesFragment()).append(")");

        return new SqlInfo(sb.toString(), sqlFragment.getParams());
    }

    public static SqlInfo buildUpdateSql(String tableName,
                                         Map<String, Object> propertyAndValues,
                                         Set<String> saveOrUpdateProperties,
                                         ColumnWithValue[] primaryKeyWithValues) {
        if (propertyAndValues == null || propertyAndValues.isEmpty()) {
            throw new IllegalArgumentException("build insert sql failed");
        }

        SqlFragment sqlFragment = createWithWheresFragmentSql(propertyAndValues, saveOrUpdateProperties, primaryKeyWithValues);
        StringBuilder sb = new StringBuilder("UPDATE ");
        sb.append(tableName).append(" SET ");
        sb.append(sqlFragment.getColumnsFragment());
        if (sqlFragment.getWheresFragment() != null && !"".equals(sqlFragment.getWheresFragment().trim())) {
            sb.append(" WHERE ")
                    .append(sqlFragment.getWheresFragment());
        }
        return new SqlInfo(sb.toString(), sqlFragment.getParams());
    }

    public static SqlInfo buildDeleteSql(String tableName, ColumnWithValue[] primaryKeyWithValues) {
        StringBuilder sb = new StringBuilder("DELETE FROM ");
        sb.append(tableName);
        SqlFragment sqlFragment = createWithWheresFragmentSql(primaryKeyWithValues);
        if (sqlFragment != null && !"".equals(sqlFragment.getWheresFragment().trim())) {
            sb.append(" WHERE ")
                    .append(sqlFragment.getWheresFragment());
            Object[] params = sqlFragment.getParams();
            return new SqlInfo(sb.toString(), params);
        }
        return new SqlInfo(sb.toString(), new Object[0]);
    }


    public static SqlInfo buildSelectSql(String tableName, ColumnWithValue[] columnWithValues) {
        SqlFragment sqlFragment = createWithWheresFragmentSql(columnWithValues);
        StringBuilder sb = new StringBuilder("SELECT * FROM ");
        sb.append(tableName);

        if (sqlFragment != null) {
            sb.append(" WHERE ").append(sqlFragment.getWheresFragment());
            return new SqlInfo(sb.toString(), sqlFragment.getParams());
        }

        return new SqlInfo(sb.toString(), null);
    }

    public static SqlInfo buildSelectSql(String tableName, String[] columns, ColumnWithValue[] columnWithValues) {
        if (columns == null || columns.length == 0) {
            return buildSelectSql(tableName, columnWithValues);
        }
        SqlFragment sqlFragment = createWithWheresFragmentSql(columnWithValues);
        StringBuilder sb = new StringBuilder("SELECT ");
        for (int i = 0, len = columns.length; i < len; i++) {
            sb.append(columns[i]);
            if (i != len - 1) {
                sb.append(", ");
            }
        }
        sb.append(" FROM ").append(tableName);
        if (sqlFragment != null) {
            sb.append(" WHERE ").append(sqlFragment.getWheresFragment());
            return new SqlInfo(sb.toString(), sqlFragment.getParams());
        }

        return new SqlInfo(sb.toString(), null);
    }

}
