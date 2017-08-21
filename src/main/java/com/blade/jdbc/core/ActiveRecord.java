package com.blade.jdbc.core;

import com.blade.jdbc.Base;
import com.blade.jdbc.annotation.Table;
import com.blade.jdbc.annotation.Transient;
import com.blade.jdbc.page.Page;
import com.blade.jdbc.page.PageRow;
import com.blade.jdbc.utils.Unchecked;
import lombok.Setter;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ActiveRecord implements Serializable {

    @Transient
    @Setter
    protected Sql2o sql2o;

    @Transient
    Set<WhereParam> whereValues = new LinkedHashSet<>();

    @Transient
    private Set<String> saveOrUpdateProperties = new TreeSet<>();

    @Transient
    private List<String> whereKeys = new ArrayList<>();
    private List<Object> whereVals = new ArrayList<>();

    public ActiveRecord() {
    }

    public Sql2o getSql2o() {
        if (null != sql2o) {
            return sql2o;
        }
        return Base.sql2o;
    }

    public <S, T extends ActiveRecord> T where(LambdaExpression<T, S> action) {
        action.apply((T) this);

        SerializedLambda lambda = getSerializedLambda(action);

        System.out.println("lambdaClassName:" + lambda.getImplClass());
        System.out.println("lambdaMethodName:" + lambda.getImplMethodName());

        return null;
    }

    // getting the SerializedLambda
    public <S, T extends ActiveRecord> SerializedLambda getSerializedLambda(LambdaExpression<T, S> action) {
        for (Class<?> clazz = action.getClass(); clazz != null; clazz = clazz.getSuperclass()) {
            try {
                Method replaceMethod = clazz.getDeclaredMethod("writeReplace");
                replaceMethod.setAccessible(true);
                Object serializedForm = replaceMethod.invoke(action);

                if (serializedForm instanceof SerializedLambda) {
                    return (SerializedLambda) serializedForm;
                }
            } catch (NoSuchMethodError e) {
                // fall through the loop and try the next class
            } catch (Throwable t) {
                throw new RuntimeException("Error while extracting serialized lambda", t);
            }
        }
        throw new RuntimeException("writeReplace method not found");
    }

    // getting the synthetic static lambda method
    public static Method getLambdaMethod(SerializedLambda lambda) throws Exception {
        String   implClassName = lambda.getImplClass().replace('/', '.');
        Class<?> implClass     = Class.forName(implClassName);

        String lambdaName = lambda.getImplMethodName();

        for (Method m : implClass.getDeclaredMethods()) {
            if (m.getName().equals(lambdaName)) {
                return m;
            }
        }

        throw new Exception("Lambda Method not found");
    }

    public <T extends ActiveRecord> T is(Object value) {

        return (T) this;
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
            sql = sql.replaceFirst("\\?", ":p" + pos);
        }
        invoke(new QueryMeta(sql, params));
    }

    private int invoke(QueryMeta queryMeta) {
        if (null == Base.connectionThreadLocal.get()) {
            try (Connection con = getSql2o().open()) {
                return con.createQuery(queryMeta.getSql()).withParams(queryMeta.getParams()).executeUpdate().getResult();
            }
        } else {
            Connection con = getConn();
            return con.createQuery(queryMeta.getSql()).withParams(queryMeta.getParams()).executeUpdate().getResult();
        }
    }

    private Connection getConn() {
        return null != Base.connectionThreadLocal.get() ? Base.connectionThreadLocal.get() : getSql2o().open();
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
        PageRow pageRow = Base.pageLocal.get();
        String  limit   = SqlBuilder.appendLimit(pageRow);
        if (null != limit) {
            sql += limit;
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
        QueryMeta queryMeta = SqlBuilder.buildFindAllSql(this, conditions);
        Class<T>  type      = (Class<T>) getClass();
        try (Connection con = getSql2o().open()) {
            Query query = con.createQuery(queryMeta.getSql()).withParams(queryMeta.getParams());
            if (queryMeta.hasColumnMapping()) {
                queryMeta.getColumnMapping().forEach(query::addColumnMapping);
            }
            this.cleanParam();
            return query.executeAndFetch(type);
        }
    }

    public <T extends ActiveRecord> Page<T> page(int page, int limit) {
        return this.page(new PageRow(page, limit));
    }

    public <T extends ActiveRecord> Page<T> page(PageRow pageRow) {
        return page(pageRow, null, null);
    }

    public <T extends ActiveRecord> Page<T> page(PageRow pageRow, String sql, Object... params) {
        Base.pageLocal.set(pageRow);
        int page  = pageRow.getPage();
        int limit = pageRow.getLimit();
        if (null != sql) {
            int pos = 1;
            while (sql.indexOf("?") != -1) {
                sql = sql.replaceFirst("\\?", ":p" + pos);
            }
        }

        String countSql = "select count(*) from (" + sql + ") tmp";
        long   count    = this.count(countSql, params);

        List<T> list = this.queryAll(sql, params);

        Page<T> pageBean = new Page<>();
        pageBean.setTotalRow(count);
        pageBean.setRows(list);
        pageBean.setPage(page);
        pageBean.setTotalPages(count / limit + (count % limit != 0 ? 1 : 0));
        if (pageBean.getTotalPages() > page) {
            pageBean.setNextPage(page + 1);
        } else {
            pageBean.setNextPage(page);
        }
        if (page > 1) {
            if (page > pageBean.getTotalPages()) {
                pageBean.setPrevPage(1);
            } else {
                pageBean.setPrevPage(page - 1);
            }
        } else {
            pageBean.setPrevPage(1);
        }
        Base.pageLocal.remove();
        return pageBean;
    }

    public <T extends ActiveRecord> T find() {
        QueryMeta queryMeta = SqlBuilder.buildFindSql(this);
        Class<T>  type      = (Class<T>) getClass();
        try (Connection con = getSql2o().open()) {
            this.cleanParam();
            Query query = con.createQuery(queryMeta.getSql())
                    .withParams(queryMeta.getParams());
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
            sql = sql.replaceFirst("\\?", ":p" + pos);
        }
        try (Connection con = getSql2o().open()) {
            this.cleanParam();
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
