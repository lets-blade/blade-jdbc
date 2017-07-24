package com.blade.jdbc.core;

import com.blade.jdbc.Base;
import com.blade.jdbc.annotation.Table;
import com.blade.jdbc.annotation.Transient;
import com.blade.jdbc.page.Page;
import com.blade.jdbc.page.PageRow;
import com.blade.jdbc.page.Pager;
import com.blade.jdbc.utils.Unchecked;
import lombok.Setter;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ActiveRecord implements Serializable {

    @Transient
    @Setter
    protected Sql2o sql2o;

    @Transient
    private Set<WhereParam> whereValues = new LinkedHashSet<>();

    @Transient
    private Set<String> saveOrUpdateProperties = new TreeSet<>();

    public ActiveRecord() {
    }

    public Sql2o getSql2o() {
        if (null != sql2o) {
            return sql2o;
        }
        return Base.sql2o;
    }

    public <T extends ActiveRecord> T where(String key, Object value) {
        return this.where(key, "=", value);
    }

    public <T extends ActiveRecord> T where(String key, String opt, Object value) {
        this.whereValues.add(WhereParam.builder().key(key).opt(opt).value(value).build());
        this.saveOrUpdateProperties.add(key);
        return (T) this;
    }

    public <T extends ActiveRecord> T and(String key, Object value) {
        return this.where(key, value);
    }

    public <T extends ActiveRecord> T and(String key, String opt, Object value) {
        return this.where(key, opt, value);
    }

    public <T extends ActiveRecord> T or(String key, Object value) {
        return this.or(key, "=", value);
    }

    public <T extends ActiveRecord> T or(String key, String opt, Object value) {
        return this.where(" or " + key, opt, value);
    }

    public void save() {
        String tableName = getTableName();

        StringBuilder sb = new StringBuilder("insert into ");
        sb.append(tableName);
        sb.append(" (");

        StringBuffer values = new StringBuffer(" values (");
        Stream.of(getClass().getDeclaredFields())
                .filter(field -> null == field.getAnnotation(Transient.class))
                .forEach(field -> {
                    sb.append(field.getName()).append(", ");
                    values.append(':').append(field.getName()).append(", ");
                });

        sb.append(')');
        values.append(')');

        String sql = sb.append(values).toString().replace(", )", ")");
        try (Connection con = getConn()) {
            con.createQuery(sql).bind(this).executeUpdate();
        }
    }

    public void update(Serializable pk) {
        this.update(getPk(), pk);
    }

    public void update(String field, Object value) {
        this.whereValues.add(WhereParam.builder().key(field).opt("=").value(value).build());
        this.update();
    }

    public void update() {
        String        sql;
        String        tableName = getTableName();
        StringBuilder sb        = new StringBuilder("update ");
        sb.append(tableName);
        sb.append(" set ");

        final int[]  pos  = {1};
        List<Object> list = this.parseSet(pos, sb);

        sb.append("where ");
        whereValues.forEach(where -> {
            sb.append(where.getKey()).append(" " + where.getOpt() + " ").append(":p").append(pos[0]++).append(" and ");
            list.add(where.getValue());
        });

        sql = sb.toString().replace(", where ", " where ").replace("and  or", "or");
        if (sql.endsWith("and ")) {
            sql = sql.substring(0, sql.length() - 5);
        }

        Object[] args = list.toArray();
        this.invoke(sql, args);
        this.cleanParam();
    }

    public void execute(String sql, Object... params) {
        int pos = 1;
        while (sql.indexOf("?") != -1) {
            sql = sql.replaceFirst("\\?", ":p" + pos);
        }
        invoke(sql, params);
    }

    private void invoke(String sql, Object[] args) {
        if (null == connectionThreadLocal) {
            try (Connection con = getSql2o().open()) {
                con.createQuery(sql).withParams(args).executeUpdate();
            }
        } else {
            Connection con = getConn();
            con.createQuery(sql).withParams(args).executeUpdate();
        }
    }

    private Connection getConn() {
        return null != connectionThreadLocal.get() ? connectionThreadLocal.get() : getSql2o().open();
    }

    public <T extends ActiveRecord> T query(String sql, Object... args) {
        int pos = 1;
        while (sql.indexOf("?") != -1) {
            sql = sql.replaceFirst("\\?", ":p" + pos);
        }
        Class<T> type = (Class<T>) getClass();
        try (Connection con = getSql2o().open()) {
            this.cleanParam();
            return con.createQuery(sql).withParams(args)
                    .executeAndFetchFirst(type);
        }
    }

    public <T extends ActiveRecord> List<T> queryAll(String sql, Object... args) {
        int pos = 1;
        while (sql.indexOf("?") != -1) {
            sql = sql.replaceFirst("\\?", ":p" + pos);
        }
        Class<T> type = (Class<T>) getClass();
        try (Connection con = getSql2o().open()) {
            this.cleanParam();
            return con.createQuery(sql).withParams(args)
                    .executeAndFetch(type);
        }
    }

    public <T extends ActiveRecord> List<T> findAll() {
        return this.findAll(null);
    }

    public <T extends ActiveRecord> List<T> findAll(Supplier<ConditionEnum>... conditions) {

        String initSql = this.parseFieldsSql(conditions);

        StringBuilder sqlBuf = new StringBuilder(initSql);

        int[]        pos  = {1};
        List<Object> list = this.parseWhere(pos, sqlBuf);
        List<Object> temp = andWhere(pos, sqlBuf);
        if (null != temp) {
            list.addAll(temp);
        }

        String sql     = this.sqlFilter(sqlBuf.toString());
        String orderBy = this.parseOrderBySql(conditions);
        if (null != orderBy) {
            sql += orderBy;
        }

        PageRow pageRow = Pager.pageLocal.get();
        String  limit   = this.parseLimitBySql(pageRow);
        if (null != limit) {
            sql += limit;
        }

        Object[] args = list.toArray();

        Class<T> type = (Class<T>) getClass();
        try (Connection con = getSql2o().open()) {
            this.cleanParam();
            return con.createQuery(sql).withParams(args)
                    .executeAndFetch(type);
        }
    }

    public <T extends ActiveRecord> Page<T> page(int page, int limit) {
        return this.page(new PageRow(page, limit));
    }

    public <T extends ActiveRecord> Page<T> page(PageRow pageRow) {
        return page(pageRow, null, null);
    }

    public <T extends ActiveRecord> Page<T> page(PageRow pageRow, String sql, Object... params) {
        Pager.pageLocal.set(pageRow);
        int page  = pageRow.getPage();
        int limit = pageRow.getLimit();
        if (null != sql) {
            int pos = 1;
            while (sql.indexOf("?") != -1) {
                sql = sql.replaceFirst("\\?", ":p" + pos);
            }
            if (null != params) {

            }
        }
        long    count = this.count(false);
        List<T> list  = this.findAll();

        Page<T> pageBean = new Page<>();
        pageBean.setTotalRow(count);
        pageBean.setRows(list);
        pageBean.setPage(page);
        pageBean.setPrevPage(page < 2 ? 1 : page - 1);
        pageBean.setNextPage(page + 1);
        pageBean.setTotalPages(count / limit + (count % limit != 0 ? 1 : 0));

        Pager.pageLocal.remove();
        return pageBean;
    }

    private String parseLimitBySql(PageRow pageRow) {
        if (null == pageRow) {
            return null;
        }
        return String.format(" limit %s, %s", pageRow.getOffset(), pageRow.getLimit());
    }

    private String parseOrderBySql(Supplier<ConditionEnum>[] conditions) {
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

    private String parseFieldsSql(Supplier<ConditionEnum>[] conditions) {
        final String[] sql = {"select * from " + getTableName()};
        if (null == conditions) {
            return sql[0];
        }
        Stream.of(conditions)
                .filter(conditionEnumSupplier -> conditionEnumSupplier.get().equals(ConditionEnum.FIELDS))
                .findFirst()
                .ifPresent(conditionEnumSupplier -> {
                    Fields      fields    = (Fields) conditionEnumSupplier;
                    Set<String> fieldsSet = fields.getFields();
                    sql[0] = "select " + fieldsSet.stream().collect(Collectors.joining(",")) + " from " + getTableName();
                });
        return sql[0];
    }

    public <T extends ActiveRecord> T find(Serializable id) {
        String   sql  = "select * from " + getTableName() + " where " + getPk() + " = :p1";
        Class<T> type = (Class<T>) getClass();
        try (Connection con = getSql2o().open()) {
            this.cleanParam();
            return con.createQuery(sql)
                    .withParams(id)
                    .executeAndFetchFirst(type);
        }
    }

    private long count(boolean cleanParam) {
        StringBuilder sqlBuf = new StringBuilder("select count(*) from " + getTableName());

        int[]        pos  = {1};
        List<Object> list = this.parseWhere(pos, sqlBuf);

        List<Object> temp = andWhere(pos, sqlBuf);
        if (null != temp) {
            list.addAll(temp);
        }

        String   sql  = this.sqlFilter(sqlBuf.toString());
        Object[] args = list.toArray();

        try (Connection con = getSql2o().open()) {
            if (cleanParam) this.cleanParam();
            return con.createQuery(sql).withParams(args)
                    .executeAndFetchFirst(Long.class);
        }
    }

    public long count() {
        return this.count(true);
    }

    private String getTableName() {
        Class<?> modelType = getClass();
        Table    table     = modelType.getAnnotation(Table.class);
        if (null != table) {
            return table.value().toLowerCase();
        }
        return modelType.getSimpleName().toLowerCase();
    }

    private String getPk() {
        Class<?> modelType = getClass();
        Table    table     = modelType.getAnnotation(Table.class);
        if (null != table) {
            return table.pk().toLowerCase();
        }
        return "id";
    }

    public void delete() {
        StringBuilder sqlBuf = new StringBuilder("delete from " + getTableName());

        int[]        pos  = {1};
        List<Object> list = this.parseWhere(pos, sqlBuf);

        if (whereValues.isEmpty()) {
            throw new RuntimeException("Delete operation must take conditions.");
        } else {
            if (sqlBuf.indexOf(" where ") == -1) {
                sqlBuf.append(" where ");
            }
        }

        List<Object> temp = andWhere(pos, sqlBuf);
        if (null != temp) {
            list.addAll(temp);
        }

        String sql = sqlBuf.toString();

        sql = sql.replace(", where", " where").replace("and  or", "or");
        if (sql.endsWith("and ")) {
            sql = sql.substring(0, sql.length() - 5);
        }

        Object[] args = list.toArray();

        this.invoke(sql, args);
        this.cleanParam();
    }

    public void delete(Serializable pk) {
        this.delete(getPk(), pk);
    }

    public void delete(String field, Object value) {
        whereValues.add(WhereParam.builder().key(field).opt("=").value(value).build());
        this.delete();
    }

    private static final ThreadLocal<Connection> connectionThreadLocal = new ThreadLocal<>();

    public void atomic(Tx tx) {
        try {
            connectionThreadLocal.set(getSql2o().beginTransaction());
            try (Connection con = connectionThreadLocal.get()) {
                tx.run();
                con.commit();
            }
        } catch (RuntimeException e) {
            connectionThreadLocal.get().rollback();
            throw e;
        } finally {
            connectionThreadLocal.remove();
        }
    }

    private void cleanParam() {
        this.whereValues.clear();
        this.saveOrUpdateProperties.clear();
        Stream.of(getClass().getDeclaredFields())
                .filter(field -> Objects.isNull(field.getAnnotation(Transient.class)))
                .forEach(field -> Unchecked.wrap(() -> {
                    field.setAccessible(true);
                    field.set(this, null);
                    return null;
                }));
    }

    private List<Object> parseSet(int[] pos, StringBuilder sqlBuf) {
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

    private List<Object> parseWhere(int[] pos, StringBuilder sqlBuf) {
        return Stream.of(getClass().getDeclaredFields())
                .filter(field -> Objects.isNull(field.getAnnotation(Transient.class)))
                .filter(field -> null != Unchecked.wrap(() -> {
                    field.setAccessible(true);
                    return field.get(this);
                }))
                .map(field -> Unchecked.wrap(() -> {
                    if (sqlBuf.indexOf(" where ") == -1) {
                        sqlBuf.append(" where ");
                    }
                    Object value = field.get(this);
                    sqlBuf.append(field.getName()).append(" = ").append(":p").append(pos[0]++).append(" and ");
                    return value;
                }))
                .collect(Collectors.toList());
    }

    private String sqlFilter(String sql) {
        sql = sql.replace(", where", " where").replace("and  or", "or");
        if (sql.endsWith(" and ")) {
            sql = sql.substring(0, sql.length() - 5);
        }
        if (sql.endsWith(", ")) {
            sql = sql.substring(0, sql.length() - 2);
        }
        return sql;
    }

    private List<Object> andWhere(int[] pos, StringBuilder sqlBuf) {
        if (!whereValues.isEmpty()) {
            if (sqlBuf.indexOf(" where ") == -1) {
                sqlBuf.append(" where ");
            }
            return whereValues.stream()
                    .map(where -> {
                        sqlBuf.append(where.getKey()).append(" " + where.getOpt() + " ").append(":p").append(pos[0]++).append(" and ");
                        return where.getValue();
                    })
                    .collect(Collectors.toList());
        }
        return null;
    }
}
