package com.blade.jdbc.core;

import com.blade.jdbc.Base;
import com.blade.jdbc.annotation.Column;
import com.blade.jdbc.annotation.Transient;
import com.blade.jdbc.page.PageRow;
import com.blade.jdbc.utils.NameUtils;
import com.blade.jdbc.utils.Pair;
import com.blade.jdbc.utils.StringUtils;
import com.blade.jdbc.utils.Unchecked;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SQl构造器
 *
 * @author biezhi
 * @date 2017/7/25
 */
class SqlBuilder {

    /**
     * 构造一个插入SQL
     *
     * @param activeRecord ActiveRecord对象
     * @return 返回insert sql语句
     */
    static QueryMeta buildInsertSql(ActiveRecord activeRecord) {
        QueryMeta     queryMeta = new QueryMeta();
        String        tableName = activeRecord.getTableName();
        StringBuilder sb        = new StringBuilder("insert into ");
        sb.append(tableName);
        sb.append(" (");

        StringBuffer values = new StringBuffer(" values (");
        Stream.of(activeRecord.getClass().getDeclaredFields())
                .filter(field -> null == field.getAnnotation(Transient.class))
                .forEach(field -> {
                    try {
                        field.setAccessible(true);
                        if (field.get(activeRecord) != null) {
                            Pair<String, String> pair = getColumnName(field);
                            sb.append(pair.getLeft()).append(", ");
                            values.append(':').append(pair.getRight()).append(", ");
                        }
                    } catch (Exception e) {
                    }
                });

        sb.append(')');
        values.append(')');

        String sql = sb.append(values).toString().replace(", )", ")");
        queryMeta.setSql(sql);
        return queryMeta;
    }

    /**
     * 构造一个更新SQL
     *
     * @param activeRecord ActiveRecord对象
     * @return 返回QueryMeta对象
     */
    static QueryMeta buildUpdateSql(ActiveRecord activeRecord) {
        QueryMeta     queryMeta = new QueryMeta();
        String        sql;
        String        tableName = activeRecord.getTableName();
        StringBuilder sb        = new StringBuilder("update ");
        sb.append(tableName);
        sb.append(" set ");

        final int[]  pos  = {1};
        List<Object> list = parseSet(activeRecord, pos, sb);

        sb.append("where ");
        activeRecord.whereValues.forEach(where -> {
            sb.append(where.getKey()).append(" ").append(where.getOpt()).append(" ").append(":p").append(pos[0]++).append(" and ");
            list.add(where.getValue());
        });

        sql = sb.toString().replace(", where ", " where ").replace("and  or", "or");
        if (sql.endsWith("and ")) {
            sql = sql.substring(0, sql.length() - 5);
        }

        Object[] args = list.toArray();
        queryMeta.setParams(args);
        queryMeta.setSql(sql);
        return queryMeta;
    }

    /**
     * 构造一个查询单个SQL
     *
     * @param activeRecord ActiveRecord对象
     * @return 返回QueryMeta对象
     */
    static QueryMeta buildFindSql(ActiveRecord activeRecord) {
        QueryMeta queryMeta = new QueryMeta();
        String    initSql   = parseFieldsSql(activeRecord.getTableName(), null);

        StringBuilder sqlBuf = new StringBuilder(initSql);

        int[] pos = {1};
        // 解析尸体设置的
        List<Object> list = parseWhere(activeRecord, pos, sqlBuf);
        List<Object> temp = andWhere(activeRecord, pos, sqlBuf);
        if (null != temp) {
            list.addAll(temp);
        }

        String sql = sqlFilter(sqlBuf.toString());
        queryMeta.setSql(sql);
        queryMeta.setParams(list.toArray());
        mapping(queryMeta, activeRecord.getClass());
        return queryMeta;
    }

    /**
     * 构造一个查询列表SQL
     *
     * @param activeRecord ActiveRecord对象
     * @param conditions   条件
     * @return 返回QueryMeta对象
     */
    @SafeVarargs
    static QueryMeta buildFindAllSql(ActiveRecord activeRecord, Supplier<ConditionEnum>... conditions) {

        QueryMeta queryMeta = new QueryMeta();

        String        initSql = parseFieldsSql(activeRecord.getTableName(), conditions);
        StringBuilder sqlBuf  = new StringBuilder(initSql);

        int[]        pos  = {1};
        List<Object> list = parseWhere(activeRecord, pos, sqlBuf);
        List<Object> temp = andWhere(activeRecord, pos, sqlBuf);
        if (null != temp) {
            list.addAll(temp);
        }

        String sql     = sqlFilter(sqlBuf.toString());
        String orderBy = parseOrderBySql(conditions);
        if (null != orderBy) {
            sql += orderBy;
        }

        PageRow pageRow = Base.pageLocal.get();
        String  limit   = appendLimit(pageRow);
        if (null != limit) {
            sql += limit;
        }

        Object[] args = list.toArray();

        queryMeta.setSql(sql);
        queryMeta.setParams(args);
        mapping(queryMeta, activeRecord.getClass());
        return queryMeta;
    }

    /**
     * 构造一个查询记录数SQL
     *
     * @param activeRecord ActiveRecord
     * @return 返回QueryMeta对象
     */
    static QueryMeta buildCountSql(ActiveRecord activeRecord) {
        QueryMeta     queryMeta = new QueryMeta();
        StringBuilder sqlBuf    = new StringBuilder("select count(*) from " + activeRecord.getTableName());
        int[]         pos       = {1};
        List<Object>  list      = parseWhere(activeRecord, pos, sqlBuf);

        List<Object> temp = andWhere(activeRecord, pos, sqlBuf);
        if (null != temp) {
            list.addAll(temp);
        }

        String   sql  = sqlFilter(sqlBuf.toString());
        Object[] args = list.toArray();
        queryMeta.setSql(sql);
        queryMeta.setParams(args);
        return queryMeta;
    }

    /**
     * 构造一个删除SQL
     *
     * @param activeRecord ActiveRecord
     * @return 返回QueryMeta对象
     */
    static QueryMeta buildDeleteSql(ActiveRecord activeRecord) {
        QueryMeta     queryMeta = new QueryMeta();
        StringBuilder sqlBuf    = new StringBuilder("delete from " + activeRecord.getTableName());

        int[]        pos  = {1};
        List<Object> list = parseWhere(activeRecord, pos, sqlBuf);

        if (activeRecord.whereValues.isEmpty()) {
            throw new RuntimeException("Delete operation must take conditions.");
        } else {
            if (sqlBuf.indexOf(" where ") == -1) {
                sqlBuf.append(" where ");
            }
        }

        List<Object> temp = andWhere(activeRecord, pos, sqlBuf);
        if (null != temp) {
            list.addAll(temp);
        }

        String sql = sqlBuf.toString();

        sql = sql.replace(", where", " where").replace("and  or", "or");
        if (sql.endsWith("and ")) {
            sql = sql.substring(0, sql.length() - 5);
        }

        Object[] args = list.toArray();
        queryMeta.setSql(sql);
        queryMeta.setParams(args);
        return queryMeta;
    }

    /**
     * And Where条件
     *
     * @param activeRecord ActiveRecord
     * @param pos          索引
     * @param sqlBuf       sql缓冲
     * @return 返回参数列表
     */
    private static List<Object> andWhere(ActiveRecord activeRecord, int[] pos, StringBuilder sqlBuf) {
        if (!activeRecord.whereValues.isEmpty()) {
            if (sqlBuf.indexOf(" where ") == -1) {
                sqlBuf.append(" where ");
            }
            return activeRecord.whereValues.stream()
                    .map(where -> {
                        sqlBuf.append(where.getKey()).append(" ").append(where.getOpt()).append(" ").append(":p").append(pos[0]++).append(" and ");
                        return where.getValue();
                    })
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 解析Where语句
     *
     * @param activeRecord ActiveRecord
     * @param pos          索引
     * @param sqlBuf       sql缓冲
     * @return 返回参数列表
     */
    private static List<Object> parseWhere(ActiveRecord activeRecord, int[] pos, StringBuilder sqlBuf) {
        return Stream.of(activeRecord.getClass().getDeclaredFields())
                .filter(field -> Objects.isNull(field.getAnnotation(Transient.class)))
                .filter(field -> null != Unchecked.wrap(() -> {
                    field.setAccessible(true);
                    return field.get(activeRecord);
                }))
                .map(field -> Unchecked.wrap(() -> {
                    if (sqlBuf.indexOf(" where ") == -1) {
                        sqlBuf.append(" where ");
                    }
                    Object               value = field.get(activeRecord);
                    Pair<String, String> pair  = getColumnName(field);
                    sqlBuf.append(pair.getRight()).append(" = ").append(":p").append(pos[0]++).append(" and ");
                    return value;
                }))
                .collect(Collectors.toList());
    }

    /**
     * 解析Model字段
     *
     * @param tableName  表名
     * @param conditions 条件
     * @return 返回sql语句
     */
    private static String parseFieldsSql(String tableName, Supplier<ConditionEnum>[] conditions) {
        final String[] sql = {"select * from " + tableName};
        if (null == conditions) {
            return sql[0];
        }
        Stream.of(conditions)
                .filter(conditionEnumSupplier -> conditionEnumSupplier.get().equals(ConditionEnum.FIELDS))
                .findFirst()
                .ifPresent(conditionEnumSupplier -> {
                    Fields      fields    = (Fields) conditionEnumSupplier;
                    Set<String> fieldsSet = fields.getFields();
                    sql[0] = "select " + fieldsSet.stream().collect(Collectors.joining(",")) + " from " + tableName;
                });
        return sql[0];
    }

    /**
     * SQL过滤
     *
     * @param sql sql语句
     * @return 返回过滤后的sql
     */
    private static String sqlFilter(String sql) {
        sql = sql.replace(", where", " where").replace("and  or", "or");
        if (sql.endsWith(" and ")) {
            sql = sql.substring(0, sql.length() - 5);
        }
        if (sql.endsWith(", ")) {
            sql = sql.substring(0, sql.length() - 2);
        }
        return sql;
    }

    public static String appendLimit(PageRow pageRow) {
        if (null == pageRow) {
            return null;
        }
        return String.format(" limit %s, %s", pageRow.getOffset(), pageRow.getLimit());
    }

    private static String parseOrderBySql(Supplier<ConditionEnum>[] conditions) {
        final String[] sql = {null};
        if (null == conditions) {
            return sql[0];
        }
        Stream.of(conditions)
                .filter(conditionEnumSupplier -> conditionEnumSupplier.get().equals(ConditionEnum.ORDER_BY))
                .findFirst()
                .ifPresent(conditionEnumSupplier -> {
                    OrderBy orderBy = (OrderBy) conditionEnumSupplier;
                    sql[0] = " order by " + orderBy.getOrderBy();
                });
        return sql[0];
    }

    private static List<Object> parseSet(ActiveRecord activeRecord, int[] pos, StringBuilder sqlBuf) {
        return Stream.of(activeRecord.getClass().getDeclaredFields())
                .filter(field -> null == field.getAnnotation(Transient.class))
                .filter(field -> null != Unchecked.wrap(() -> {
                    field.setAccessible(true);
                    return field.get(activeRecord);
                }))
                .map(field -> Unchecked.wrap(() -> {
                    Object               value = field.get(activeRecord);
                    Pair<String, String> pair  = getColumnName(field);

                    sqlBuf.append(pair.getRight()).append(" = ").append(":p").append(pos[0]++).append(", ");
                    return value;
                }))
                .collect(Collectors.toList());
    }

    /**
     * Pair<ColumnName, FieldName>
     *
     * @param field 字段
     * @return 返回列名和字段名
     */
    private static Pair<String, String> getColumnName(Field field) {
        String fieldName = field.getName();
        Column column    = field.getAnnotation(Column.class);
        if (null != column && StringUtils.isNotBlank(column.name())) {
            fieldName = column.name();
        }
        String columnName = NameUtils.getUnderlineName(fieldName);
        return new Pair<>(columnName, fieldName);
    }

    public static void mapping(QueryMeta queryMeta, Class<?> modelType) {
        Stream.of(modelType.getDeclaredFields())
                .filter(field -> null == field.getAnnotation(Transient.class))
                .forEach(field -> {
                    Pair<String, String> pair = getColumnName(field);
                    if (!pair.getLeft().equals(pair.getRight())) {
                        queryMeta.addColumnMapping(pair.getLeft(), pair.getRight());
                    }
                });
    }
}