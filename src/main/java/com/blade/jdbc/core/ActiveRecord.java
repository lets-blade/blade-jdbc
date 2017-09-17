package com.blade.jdbc.core;

import com.blade.jdbc.Base;
import com.blade.jdbc.annotation.Table;
import com.blade.jdbc.annotation.Transient;
import com.blade.jdbc.page.Page;
import com.blade.jdbc.page.PageRow;
import com.blade.jdbc.utils.Unchecked;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.io.Serializable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class ActiveRecord implements Serializable {

    private static final String EXECUTE_SQL_PREFIX = "⬢ Execute SQL";
    private static final String PARAMETER_PREFIX   = "⬢ Parameters ";

    @Transient
    @Setter
    protected Sql2o sql2o;

    @Transient
    Set<WhereParam> whereValues = new LinkedHashSet<>();

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

    public <T extends ActiveRecord> T like(String key, Object value) {
        return this.where(key, "like", value);
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

    public <T extends ActiveRecord, V> T in(String key, List<V> list) {
        return this.where(key, "in", "(" + list.stream().map(Object::toString).collect(Collectors.joining(",")) + ")");
    }

    //    TODO
    public <T extends ActiveRecord> T between(String key, Object val1, Object val2) {
        // date between ? and ?
        this.whereValues.add(WhereParam.builder().key(key).opt("between").value(val1).build());
        this.saveOrUpdateProperties.add(key);
        return (T) this;
    }

    public <S extends Serializable> S save() {
        QueryMeta queryMeta = SqlBuilder.buildInsertSql(this);
        try (Connection con = getConn()) {
            log.debug(EXECUTE_SQL_PREFIX + " => {}", queryMeta.getSql());
            return (S) con.createQuery(queryMeta.getSql()).bind(this).executeUpdate().getKey();
        }
    }

    public void update(Serializable pk) {
        this.update(getPk(), pk);
    }

    public void update(String field, Object value) {
        this.whereValues.add(WhereParam.builder().key(field).opt("=").value(value).build());
        this.update();
    }

    public int update() {
        QueryMeta queryMeta = SqlBuilder.buildUpdateSql(this);
        int       result    = this.invoke(queryMeta);
        this.cleanParam();
        return result;
    }

    public void execute(String sql, Object... params) {
        int pos = 1;
        while (sql.indexOf("?") != -1) {
            sql = sql.replaceFirst("\\?", ":p" + (pos++));
        }
        invoke(new QueryMeta(sql, params));
    }

    private int invoke(QueryMeta queryMeta) {
        log.debug(EXECUTE_SQL_PREFIX + " => {}", queryMeta.getSql());
        log.debug(PARAMETER_PREFIX + " => {}", Arrays.toString(queryMeta.getParams()));
        if (null == Base.connectionThreadLocal.get()) {
            try (Connection con = getSql2o().open()) {
                Query query = con.createQuery(queryMeta.getSql()).withParams(queryMeta.getParams());
                if (queryMeta.hasColumnMapping()) {
                    queryMeta.getColumnMapping().forEach(query::addColumnMapping);
                }
                return query.executeUpdate().getResult();
            }
        } else {
            try (Connection con = getConn()) {
                Query query = con.createQuery(queryMeta.getSql()).withParams(queryMeta.getParams());
                if (queryMeta.hasColumnMapping()) {
                    queryMeta.getColumnMapping().forEach(query::addColumnMapping);
                }
                return query.executeUpdate().getResult();
            }
        }
    }

    private Connection getConn() {
        return null != Base.connectionThreadLocal.get() ? Base.connectionThreadLocal.get() : getSql2o().open();
    }

    public <T> T query(String sql, Object... args) {
        int pos = 1;
        while (sql.indexOf("?") != -1) {
            sql = sql.replaceFirst("\\?", ":p" + (pos++));
        }
        Class<T> type = (Class<T>) getClass();
        try (Connection con = getSql2o().open()) {
            this.cleanParam();
            log.debug(EXECUTE_SQL_PREFIX + " => {}", sql);
            log.debug(PARAMETER_PREFIX + " => {}", Arrays.toString(args));
            return con.createQuery(sql).withParams(args)
                    .executeAndFetchFirst(type);
        }
    }

    public <T> List<T> queryAll(String sql, Object... args) {
        int pos = 1;
        while (sql.indexOf("?") != -1) {
            sql = sql.replaceFirst("\\?", ":p" + (pos++));
        }
        PageRow pageRow = Base.pageLocal.get();
        String  limit   = SqlBuilder.appendLimit(pageRow);
        if (null != limit) {
            sql += limit;
        }
        Class<T> type = (Class<T>) getClass();
        args = args == null ? new Object[]{} : args;
        try (Connection con = getSql2o().open()) {
            log.debug(EXECUTE_SQL_PREFIX + " => {}", sql);
            log.debug(PARAMETER_PREFIX + " => {}", Arrays.toString(args));
            Query     query     = con.createQuery(sql).withParams(args);
            QueryMeta queryMeta = SqlBuilder.buildFindAllSql(this, null);
            if (queryMeta.hasColumnMapping()) {
                queryMeta.getColumnMapping().forEach(query::addColumnMapping);
            }
            this.cleanParam();
            return query.executeAndFetch(type);
        }
    }

    public <T extends ActiveRecord> List<T> findAll() {
        return this.findAll(null);
    }

    public <T extends ActiveRecord> List<T> findAll(Supplier<ConditionEnum>... conditions) {
        QueryMeta queryMeta = SqlBuilder.buildFindAllSql(this, conditions);
        Class<T>  type      = (Class<T>) getClass();
        try (Connection con = getSql2o().open()) {
            log.debug(EXECUTE_SQL_PREFIX + " => {}", queryMeta.getSql());
            log.debug(PARAMETER_PREFIX + " => {}", Arrays.toString(queryMeta.getParams()));
            Query query = con.createQuery(queryMeta.getSql()).withParams(queryMeta.getParams());
            if (queryMeta.hasColumnMapping()) {
                queryMeta.getColumnMapping().forEach(query::addColumnMapping);
            }
            this.cleanParam();
            return query.executeAndFetch(type);
        }
    }

    public <T extends ActiveRecord> Page<T> page(int page, int limit) {
        return this.page(page, limit, null);
    }

    public <T extends ActiveRecord> Page<T> page(int page, int limit, String orderBy) {
        return this.page(new PageRow(page, limit), orderBy);
    }

    public <T extends ActiveRecord> Page<T> page(PageRow pageRow) {
        return page(pageRow, null);
    }

    public <T extends ActiveRecord> Page<T> page(PageRow pageRow, String orderBy) {
        QueryMeta queryMeta = SqlBuilder.buildFindAllSql(this, null);
        return page(pageRow, queryMeta.getSql(), orderBy, queryMeta.getParams());
    }

    public <T extends ActiveRecord> Page<T> page(PageRow pageRow, String sql, Object... params) {
        return page(pageRow, sql, null, params);
    }

    public <T extends ActiveRecord> Page<T> page(PageRow pageRow, String sql, String orderBy, Object... params) {
        Base.pageLocal.set(pageRow);
        int page  = pageRow.getPage();
        int limit = pageRow.getLimit();
        if (null != sql) {
            int pos = 1;
            while (sql.indexOf("?") != -1) {
                sql = sql.replaceFirst("\\?", ":p" + (pos++));
            }
        } else {
            sql = "select * from " + getTableName();
        }

        String countSql = "select count(0) from (" + sql + ") tmp";
        long   count    = this.count(countSql, params);

        if (null != orderBy) {
            sql += " order by " + orderBy;
        }

        List<T> list     = this.queryAll(sql, params);
        Page<T> pageBean = new Page<>(count, page, limit);
        pageBean.setRows(list);
        Base.pageLocal.remove();
        return pageBean;
    }

    public <T extends ActiveRecord> T find() {
        QueryMeta queryMeta = SqlBuilder.buildFindSql(this);
        Class<T>  type      = (Class<T>) getClass();
        try (Connection con = getSql2o().open()) {
            this.cleanParam();

            log.debug(EXECUTE_SQL_PREFIX + " => {}", queryMeta.getSql());
            log.debug(PARAMETER_PREFIX + " => {}", Arrays.toString(queryMeta.getParams()));

            Query query = con.createQuery(queryMeta.getSql()).withParams(queryMeta.getParams());
            if (queryMeta.hasColumnMapping()) {
                queryMeta.getColumnMapping().forEach(query::addColumnMapping);
            }
            return query.executeAndFetchFirst(type);
        }
    }

    public <T extends ActiveRecord> T find(Serializable id) {
        String    sql       = "select * from " + getTableName() + " where " + getPk() + " = :p1";
        QueryMeta queryMeta = new QueryMeta();
        SqlBuilder.mapping(queryMeta, this.getClass());

        Class<T> type = (Class<T>) getClass();
        try (Connection con = getSql2o().open()) {
            this.cleanParam();

            log.debug(EXECUTE_SQL_PREFIX + " => {}", sql);
            log.debug(PARAMETER_PREFIX + " => [{}]", id);

            Query query = con.createQuery(sql).withParams(id);
            if (queryMeta.hasColumnMapping()) {
                queryMeta.getColumnMapping().forEach(query::addColumnMapping);
            }
            return query.executeAndFetchFirst(type);
        }
    }

    private long count(boolean cleanParam) {
        QueryMeta queryMeta = SqlBuilder.buildCountSql(this);
        try (Connection con = getSql2o().open()) {
            if (cleanParam) this.cleanParam();

            log.debug(EXECUTE_SQL_PREFIX + " => {}", queryMeta.getSql());
            log.debug(PARAMETER_PREFIX + " => {}", Arrays.toString(queryMeta.getParams()));

            return con.createQuery(queryMeta.getSql())
                    .withParams(queryMeta.getParams())
                    .executeAndFetchFirst(Long.class);
        }
    }

    public long count() {
        return this.count(true);
    }

    public long count(String sql, Object... args) {
        int pos = 1;
        while (sql.indexOf("?") != -1) {
            sql = sql.replaceFirst("\\?", ":p" + (pos++));
        }
        args = args == null ? new Object[]{} : args;
        try (Connection con = getSql2o().open()) {
            this.cleanParam();

            log.debug(EXECUTE_SQL_PREFIX + " => {}", sql);
            log.debug(PARAMETER_PREFIX + " => {}", Arrays.toString(args));

            return con.createQuery(sql).withParams(args)
                    .executeAndFetchFirst(Long.class);
        }
    }

    String getTableName() {
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
        QueryMeta queryMeta = SqlBuilder.buildDeleteSql(this);
        this.invoke(queryMeta);
        this.cleanParam();
    }

    public void delete(Serializable pk) {
        this.delete(getPk(), pk);
    }

    public void delete(String field, Object value) {
        whereValues.add(WhereParam.builder().key(field).opt("=").value(value).build());
        this.delete();
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

}
