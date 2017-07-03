package com.blade.jdbc;

import com.blade.jdbc.annotation.Table;
import com.blade.jdbc.annotation.Transient;
import lombok.Setter;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActiveRecord implements Serializable {

    @Transient
    @Setter
    protected Sql2o sql2o;

    @Transient
    private Map<String, Object> propertyAndValues = new TreeMap<>();

    @Transient
    private Map<String, Object> whereValues = new TreeMap<>();

    @Transient
    private Set<String> saveOrUpdateProperties = new LinkedHashSet<>();

    public ActiveRecord() {
    }

    public <T extends ActiveRecord> T set(String key, Object value) {
        this.propertyAndValues.put(key, value);
        this.saveOrUpdateProperties.add(key);
        return (T) this;
    }

    public <T extends ActiveRecord> T where(String key, Object value) {
        this.whereValues.put(key, value);
        this.saveOrUpdateProperties.add(key);
        return (T) this;
    }

    public void save() {
        String sql = SqlBuilder.buildInsertSql(getClass());
        try (Connection con = sql2o.open()) {
            con.createQuery(sql).bind(this).executeUpdate();
        }
    }

    public void update() {
        String sql = null;

        String tableName = getTableName();
        StringBuilder sb = new StringBuilder("update ");
        sb.append(tableName);
        sb.append(" set ");

        final int[] pos = {1};
        List<Object> list = this.parseWhere(pos, sb);

        sb.append("where ");
        whereValues.forEach((k, value) -> {
            sb.append(k).append(" = ").append(":p").append(pos[0]++).append(" and ");
            list.add(value);
        });

        sql = sb.toString().replace(", where", " where");
        if (sql.endsWith("and ")) {
            sql = sql.substring(0, sql.length() - 5);
        }

        Object[] args = list.toArray();

        try (Connection con = sql2o.open()) {
            con.createQuery(sql).withParams(args).executeUpdate();
        }
    }

    public <T extends ActiveRecord> List<T> findAll() {
        String sql = "select * from " + getTableName();
        Class<T> type = (Class<T>) getClass();
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .executeAndFetch(type);
        }
    }

    public <T extends ActiveRecord> T find(Serializable id) {
        String sql = "select * from " + getTableName() + " where " + getPk() + " = :p1";
        Class<T> type = (Class<T>) getClass();
        try (Connection con = sql2o.open()) {
            return con.createQuery(sql)
                    .withParams(id)
                    .executeAndFetchFirst(type);
        }
    }

    private String getTableName() {
        Class<?> modelType = getClass();
        Table table = modelType.getAnnotation(Table.class);
        if (null != table) {
            return table.value().toLowerCase();
        }
        return modelType.getSimpleName().toLowerCase();
    }

    private String getPk() {
        Class<?> modelType = getClass();
        Table table = modelType.getAnnotation(Table.class);
        if (null != table) {
            return table.pk().toLowerCase();
        }
        return "id";
    }

    public void delete() {
        StringBuilder sqlBuf = new StringBuilder("delete from " + getTableName());

        int[] pos = {1};
        List<Object> list = this.parseWhere(pos, sqlBuf);

        sqlBuf.append(" where ");
        whereValues.forEach((k, value) -> {
            sqlBuf.append(k).append(" = ").append(":p").append(pos[0]++).append(" and ");
            list.add(value);
        });

        String sql = sqlBuf.toString();

        sql = sql.replace(", where", " where");
        if (sql.endsWith("and ")) {
            sql = sql.substring(0, sql.length() - 5);
        }

        Object[] args = list.toArray();

        try (Connection con = sql2o.open()) {
            con.createQuery(sql).withParams(args).executeUpdate();
        }
    }

    public void delete(String field, Object value) {
        whereValues.put(field, value);
        this.delete();
    }

    private List<Object> parseWhere(int[] pos, StringBuilder sqlBuf) {
        return Stream.of(getClass().getDeclaredFields())
                .filter(field -> null == field.getAnnotation(Transient.class))
                .filter(field -> null != Unchecked.wrap(() -> {
                    field.setAccessible(true);
                    return field.get(this);
                }))
                .map(field -> Unchecked.wrap(() -> {
                    Object value = field.get(this);
                    sqlBuf.append(field.getName()).append(" = ").append(":p").append(pos[0]++).append(", ");
                    return value;
                }))
                .collect(Collectors.toList());
    }
}
