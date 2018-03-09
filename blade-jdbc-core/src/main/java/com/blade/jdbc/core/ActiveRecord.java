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
import java.util.stream.Stream;

import static com.blade.jdbc.Const.*;

/**
 * ActiveRecord Parent Class
 *
 * @author biezhi
 * @date 2017/11/10
 */
@Slf4j
public class ActiveRecord implements Serializable {

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
        return this.where(key, "LIKE", value);
    }

    public <T extends ActiveRecord> T gt(String key, Object value) {
        return this.where(key, ">", value);
    }

    public <T extends ActiveRecord> T gte(String key, Object value) {
        return this.where(key, ">=", value);
    }

    public <T extends ActiveRecord> T lt(String key, Object value) {
        return this.where(key, "<", value);
    }

    public <T extends ActiveRecord> T let(String key, Object value) {
        return this.where(key, "<=", value);
    }

    public <T extends ActiveRecord> T notEqual(String key, Object value) {
        return this.where(key, "<>", value);
    }

    public <T extends ActiveRecord> T isNull(String key) {
        return this.where(key, "IS", "IS NULL");
    }

    public <T extends ActiveRecord> T isNotNull(String key) {
        return this.where(key, "IS", "IS NOT NULL");
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
        return this.where(" OR " + key, opt, value);
    }

    public <T extends ActiveRecord> T in(String key, List<?> args) {
        return this.where(key, "IN", args);
    }

    public <S extends Serializable> S save() {
        QueryMeta  queryMeta = SqlBuilder.buildInsertSql(this);
        Connection con       = getConn();
        log.debug(EXECUTE_SQL_PREFIX + " => {}", queryMeta.getSql());
        log.debug(PARAMETER_PREFIX + " => {}", this);
        Query query = con.createQuery(queryMeta.getSql()).bind(this);
        try {
            S s = (S) query.executeUpdate().getKey();
            if (null == Base.connectionThreadLocal.get() && !con.getJdbcConnection().getAutoCommit()) {
                con.commit();
            }
            return s;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int update(Serializable pk) {
        return this.update(getPk(), pk);
    }

    public int update(String field, Object value) {
        this.whereValues.add(WhereParam.builder().key(field).opt("=").value(value).build());
        return this.update();
    }

    public int update() {
        QueryMeta queryMeta = SqlBuilder.buildUpdateSql(this);
        int       result    = this.invoke(queryMeta);
        this.cleanParam();
        return result;
    }

    public int execute(String sql, Object... params) {
        int pos = 1;
        while (sql.contains(SQL_QM)) {
            sql = sql.replaceFirst("\\?", ":p" + (pos++));
        }
        try {
            return invoke(new QueryMeta(sql, params));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int invoke(QueryMeta queryMeta) {
        log.debug(EXECUTE_SQL_PREFIX + " => {}", queryMeta.getSql());
        log.debug(PARAMETER_PREFIX + " => {}", Arrays.toString(queryMeta.getParams()));
        Connection con   = getConn();
        Query      query = con.createQuery(queryMeta.getSql()).withParams(queryMeta.getParams());
        if (queryMeta.hasColumnMapping()) {
            queryMeta.getColumnMapping().forEach(query::addColumnMapping);
        }
        int result = query.executeUpdate().getResult();
        try {
            if (null == Base.connectionThreadLocal.get() && !con.getJdbcConnection().getAutoCommit()) {
                con.commit();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    private Connection getConn() {
        Connection connection = Base.connectionThreadLocal.get();
        if (null != connection) {
            return connection;
        }
        connection = getSql2o().open();
        return connection;
    }

    public <T extends ActiveRecord> T query(String sql, Object... args) {
        return this.query((Class<T>) getClass(), sql, args);
    }

    public <T> T query(Class<T> type, String sql, Object... args) {
        int pos = 1;
        while (sql.contains(SQL_QM)) {
            sql = sql.replaceFirst("\\?", ":p" + (pos++));
        }
        try (Connection con = getSql2o().open()) {
            log.debug(EXECUTE_SQL_PREFIX + " => {}", sql);
            log.debug(PARAMETER_PREFIX + " => {}", Arrays.toString(args));
            this.cleanParam();
            Query     query     = con.createQuery(sql).withParams(args).throwOnMappingFailure(false);
            QueryMeta queryMeta = SqlBuilder.buildFindAllSql(this, null);
            if (queryMeta.hasColumnMapping()) {
                queryMeta.getColumnMapping().forEach(query::addColumnMapping);
            }
            return query.executeAndFetchFirst(type);
        }
    }

    public <T extends ActiveRecord> List<T> queryAll(String sql, Object... args) {
        return this.queryAll((Class<T>) getClass(), sql, args);
    }

    public <T> List<T> queryAll(Class<T> type, String sql, Object... args) {
        int pos = 1;

        while (sql.contains(SQL_QM)) {
            sql = sql.replaceFirst("\\?", ":p" + (pos++));
        }

        PageRow pageRow = Base.pageLocal.get();
        sql = SqlBuilder.appendPageParams(sql, pageRow);
        args = args == null ? new Object[]{} : args;

        try (Connection con = getSql2o().open()) {
            log.debug(EXECUTE_SQL_PREFIX + " => {}", sql);
            log.debug(PARAMETER_PREFIX + " => {}", Arrays.toString(args));
            this.cleanParam();
            Query     query     = con.createQuery(sql).withParams(args).throwOnMappingFailure(false);
            QueryMeta queryMeta = SqlBuilder.buildFindAllSql(this, null);
            if (queryMeta.hasColumnMapping()) {
                queryMeta.getColumnMapping().forEach(query::addColumnMapping);
            }
            return query.executeAndFetch(type);
        }
    }

    public <T extends ActiveRecord> List<T> findAll() {
        return this.findAll(null);
    }

    public <T> List<T> findAll(Class<T> type, Supplier<ConditionEnum>... conditions) {
        QueryMeta queryMeta = SqlBuilder.buildFindAllSql(this, conditions);
        try (Connection con = getSql2o().open()) {
            log.debug(EXECUTE_SQL_PREFIX + " => {}", queryMeta.getSql());
            log.debug(PARAMETER_PREFIX + " => {}", Arrays.toString(queryMeta.getParams()));
            this.cleanParam();
            Query query = con.createQuery(queryMeta.getSql()).withParams(queryMeta.getParams()).throwOnMappingFailure(false);
            if (queryMeta.hasColumnMapping()) {
                queryMeta.getColumnMapping().forEach(query::addColumnMapping);
            }
            return query.executeAndFetch(type);
        }
    }

    public <T extends ActiveRecord> List<T> findAll(Supplier<ConditionEnum>... conditions) {
        return this.findAll((Class<T>) getClass(), conditions);
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
            while (sql.contains(SQL_QM)) {
                sql = sql.replaceFirst("\\?", ":p" + (pos++));
            }
        } else {
            sql = "SELECT * FROM " + getTableName();
        }

        String countSql = "SELECT COUNT(0) FROM (" + sql + ") tmp";
        long   count    = this.count(countSql, params);

        if (null != orderBy) {
            sql += " ORDER BY " + orderBy;
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

            Query query = con.createQuery(queryMeta.getSql()).withParams(queryMeta.getParams()).throwOnMappingFailure(false);
            if (queryMeta.hasColumnMapping()) {
                queryMeta.getColumnMapping().forEach(query::addColumnMapping);
            }
            return query.executeAndFetchFirst(type);
        }
    }

    public <T> T find(Class<T> type, Serializable id) {
        String    sql       = "SELECT * FROM " + getTableName() + " WHERE " + getPk() + " = :p1";
        QueryMeta queryMeta = new QueryMeta();
        SqlBuilder.mapping(queryMeta, this.getClass());
        try (Connection con = getSql2o().open()) {
            this.cleanParam();

            log.debug(EXECUTE_SQL_PREFIX + " => {}", sql);
            log.debug(PARAMETER_PREFIX + " => [{}]", id);

            Query query = con.createQuery(sql).withParams(id).throwOnMappingFailure(false);
            if (queryMeta.hasColumnMapping()) {
                queryMeta.getColumnMapping().forEach(query::addColumnMapping);
            }
            return query.executeAndFetchFirst(type);
        }
    }

    public <T extends ActiveRecord> T find(Serializable id) {
        return find((Class<T>) getClass(), id);
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
        while (sql.contains("?")) {
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
            return table.value();
        }
        return modelType.getSimpleName();
    }

    private String getPk() {
        Class<?> modelType = getClass();
        Table    table     = modelType.getAnnotation(Table.class);
        if (null != table) {
            return table.pk();
        }
        return "id";
    }

    public int delete() {
        QueryMeta queryMeta = SqlBuilder.buildDeleteSql(this);
        int       result    = this.invoke(queryMeta);
        this.cleanParam();
        return result;
    }

    public int delete(Serializable pk) {
        return this.delete(getPk(), pk);
    }

    public int delete(String field, Object value) {
        whereValues.add(WhereParam.builder().key(field).opt("=").value(value).build());
        return this.delete();
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
