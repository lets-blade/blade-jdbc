package com.blade.jdbc.ar;

import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.Base;
import com.blade.jdbc.pager.PageRow;
import com.blade.jdbc.pager.Paginator;
import com.blade.jdbc.persistence.*;
import com.blade.jdbc.tx.AtomTx;
import com.blade.jdbc.utils.NameUtils;
import com.blade.jdbc.utils.StringUtils;
import com.blade.jdbc.utils.Utils;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

import javax.sql.DataSource;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * ActiveRecord default implement
 */
public class SampleActiveRecord implements ActiveRecord {

    /** sql2o 对象 */
    private Sql2o sql2o;

    /** 名称处理器，为空按默认执行 */
    private NameHandler nameHandler;

    /** 数据库方言 */
    private String          dialect = "mysql";

    private Object[] EMPTY = new Object[]{};

    public SampleActiveRecord(){}

    public SampleActiveRecord(String url, String user, String pass){
        this.sql2o = Base.open(url, user, pass);
    }

    public SampleActiveRecord(DataSource dataSource){
        this.sql2o = Base.open(dataSource);
    }

    public SampleActiveRecord(Sql2o sql2o){
        this.sql2o = sql2o;
    }

    /**
     * 插入数据
     *
     * @param entity the entity
     * @param criteria the criteria
     * @return long long
     */
    private <T extends Serializable> T insert(Object entity, Criteria criteria) {
        Class<?> entityClass = SqlAssembleUtils.getEntityClass(entity, criteria);
        NameHandler handler = this.getNameHandler();
        String pkValue = handler.getPKValue(entityClass, this.dialect);
        if (StringUtils.isNotBlank(pkValue)) {
            String primaryName = handler.getPKName(entityClass);
            if (criteria == null) {
                criteria = Criteria.create(entityClass);
            }
            criteria.setPKValueName(NameUtils.getCamelName(primaryName), pkValue);
        }
        final BoundSql boundSql = SqlAssembleUtils.buildInsertSql(entity, criteria,
            this.getNameHandler());

        try (Connection con = sql2o.open()){
            return (T)con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeUpdate().getKey();
        }
    }

    @Override
    public <T extends Serializable> T insert(Object entity) {
        return this.insert(entity, null);
    }

    @Override
    public <T extends Serializable> T insert(Criteria criteria) {
        return this.insert(null, criteria);
    }

    @Override
    public void save(Object entity) {
        final BoundSql boundSql = SqlAssembleUtils.buildInsertSql(entity, null,
            this.getNameHandler());
        try (Connection con = sql2o.beginTransaction()){
            con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeUpdate().commit(true);
        }
    }

    @Override
    public void save(Criteria criteria) {
        final BoundSql boundSql = SqlAssembleUtils.buildInsertSql(null, criteria,
            this.getNameHandler());
        try (Connection con = sql2o.beginTransaction()){
            con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeUpdate().commit(true);
        }
    }

    @Override
    public int update(Criteria criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildUpdateSql(null, criteria, this.getNameHandler());
        try (Connection con = sql2o.beginTransaction()){
            return con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeUpdate().commit(true).getResult();
        }
    }

    @Override
    public int update(Object entity) {
        BoundSql boundSql = SqlAssembleUtils.buildUpdateSql(entity, null, this.getNameHandler());
        try (Connection con = sql2o.beginTransaction()){
            return con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeUpdate().commit(true).getResult();
        }
    }

    @Override
    public int delete(Criteria criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildDeleteSql(null, criteria, this.getNameHandler());
        try (Connection con = sql2o.beginTransaction()){
            return con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeUpdate().commit(true).getResult();
        }
    }

    @Override
    public int delete(Object entity) {
        BoundSql boundSql = SqlAssembleUtils.buildDeleteSql(entity, null, this.getNameHandler());
        try (Connection con = sql2o.beginTransaction()){
            return con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeUpdate().commit(true).getResult();
        }
    }

    @Override
    public int delete(Class<?> clazz, Long id) {
        BoundSql boundSql = SqlAssembleUtils.buildDeleteSql(clazz, id, this.getNameHandler());
        try (Connection con = sql2o.beginTransaction()){
            return con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeUpdate().commit(true).getResult();
        }
    }

    @Override
    public int deleteAll(Class<?> clazz) {
        String tableName = this.getNameHandler().getTableName(clazz);
        String sql = "TRUNCATE TABLE " + tableName;
        try (Connection con = sql2o.beginTransaction()){
           return con.createQuery(sql).executeUpdate().commit(true).getResult();
        }
    }

    @Override
    public <T> List<T> list(Criteria criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildListSql(null, criteria, this.getNameHandler());
        try (Connection con = sql2o.open()){
            List<?> list = con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeAndFetch(criteria.getEntityClass());
            return null != list && !list.isEmpty() ? (List<T>)list : null;
        }
    }

    @Override
    public <T> List<T> list(String sql, Class<T> type, Object...args) {
        if(null == args){
            args = EMPTY;
        }
        try (Connection con = sql2o.open()){
            return con.createQuery(sql).withParams(args).executeAndFetch(type);
        }
    }

    @Override
    public <T> List<T> list(T entity) {
        BoundSql boundSql = SqlAssembleUtils.buildListSql(entity, null, this.getNameHandler());
        try (Connection con = sql2o.open()){
            List<?> list = con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeAndFetch(entity.getClass());
            return null != list && !list.isEmpty() ? (List<T>)list : null;
        }
    }

    @Override
    public List<Map<String, Object>> listMap(String sql, Object... args) {
        if(null != args && args.length > 0){
            try (Connection con = sql2o.open()){
                return con.createQuery(sql).withParams(args).executeAndFetchTable().asList();
            }
        }
        try (Connection con = sql2o.open()){
            return con.createQuery(sql).executeAndFetchTable().asList();
        }
    }


    @Override
    public <T> List<T> list(T entity, Criteria criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildListSql(entity, criteria, this.getNameHandler());
        try (Connection con = sql2o.open()){
            List<?> list = con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeAndFetch(entity.getClass());
            return null != list && !list.isEmpty() ? (List<T>)list : null;
        }
    }

    @Override
    public Map<String, Object> map(String sql, Object... args) {
        if(null != args && args.length > 0){
            try (Connection con = sql2o.open()){
                List<Map<String,Object>> list = con.createQuery(sql).withParams(args).executeAndFetchTable().asList();
                return null != list && !list.isEmpty() ? list.get(0) : null;
            }
        }
        try (Connection con = sql2o.open()){
            List<Map<String,Object>> list = con.createQuery(sql).executeAndFetchTable().asList();
            return null != list && !list.isEmpty() ? list.get(0) : null;
        }
    }

    @Override
    public <T> T one(Class<T> type, String sql, Object... args) {
        if(null != args && args.length > 0){
            try (Connection con = sql2o.open()){
                return con.createQuery(sql).withParams(args).executeAndFetchFirst(type);
            }
        }
        try (Connection con = sql2o.open()){
            return con.createQuery(sql).executeAndFetchFirst(type);
        }
    }

    @Override
    public int count(Object entity, Criteria criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildCountSql(entity, criteria, this.getNameHandler());
        try (Connection con = sql2o.open()){
            return con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeScalar(Integer.class);
        }
    }

    @Override
    public int count(Object entity) {
        BoundSql boundSql = SqlAssembleUtils.buildCountSql(entity, null, this.getNameHandler());
        try (Connection con = sql2o.open()){
            return con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeScalar(Integer.class);
        }
    }

    @Override
    public int count(Criteria criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildCountSql(null, criteria, this.getNameHandler());
        try (Connection con = sql2o.open()){
            return con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeScalar(Integer.class);
        }
    }

    @Override
    public <T> T byId(Class<T> clazz, Serializable pk) {
        BoundSql boundSql = SqlAssembleUtils.buildByIdSql(clazz, pk, null, this.getNameHandler());
        try (Connection con = sql2o.open()){
            return con.createQuery(boundSql.getSql()).withParams(pk).executeAndFetchFirst(clazz);
        }
    }

    @Override
    public <T> T byId(Criteria criteria, Serializable pk) {
        BoundSql boundSql = SqlAssembleUtils.buildByIdSql(null, pk, criteria, this.getNameHandler());
        try (Connection con = sql2o.open()){
            Object o = con.createQuery(boundSql.getSql()).withParams(pk).executeAndFetchFirst(criteria.getEntityClass());
            return o != null ? (T) o : null;
        }
    }

    @Override
    public <T> T one(T entity) {
        BoundSql boundSql = SqlAssembleUtils.buildQuerySql(entity, null, this.getNameHandler());
        try (Connection con = sql2o.open()){
            Object o = con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeAndFetchFirst(entity.getClass());
            return o != null ? (T) o : null;
        }
    }

    @Override
    public <T> T one(Criteria criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildQuerySql(null, criteria, this.getNameHandler());
        try (Connection con = sql2o.open()){
            Object o = con.createQuery(boundSql.getSql()).withParams(boundSql.getParams().toArray()).executeAndFetchFirst(criteria.getEntityClass());
            return o != null ? (T) o : null;
        }
    }

    @Override
    public int execute(String sql, Object... args) {
        int pindex = 1;
        while(sql.contains(" ?")){
            sql = sql.replaceFirst(" \\?", " :p" + pindex++);
        }
        if(null != args && args.length > 0){
            try (Connection con = sql2o.beginTransaction()){
                return con.createQuery(sql).withParams(args).executeUpdate().commit(true).getResult();
            }
        }
        try (Connection con = sql2o.beginTransaction()){
            return con.createQuery(sql).executeUpdate().commit(true).getResult();
        }
    }

    @Override
    public void run(AtomTx tx) {
        try (Connection con = sql2o.open()){
            tx.call(con);
        }
    }

    @Override
    public <T> Paginator<T> page(T entity, int page, int limit) {
        return this.page(entity, new PageRow(page, limit));
    }

    @Override
    public <T> Paginator<T> page(Criteria criteria, int page, int limit, String orderBy) {
        return this.page(criteria, new PageRow(page, limit, orderBy));
    }

    @Override
    public <T> Paginator<T> page(T entity, PageRow pageRow) {
        BoundSql boundSql = SqlAssembleUtils.buildQuerySql(entity, null, this.getNameHandler());
        String countSql = Utils.getCountSql(boundSql.getSql());

        Paginator<T> paginator;

        try (Connection con = sql2o.open()){
            int total = con.createQuery(countSql).withParams(boundSql.getParams().toArray()).executeScalar(Integer.class);

            paginator = new Paginator<>(total, pageRow.getPage(), pageRow.getLimit());

            String sql = Utils.getPageSql(boundSql.getSql(), dialect, pageRow);
            List<?> list = con.createQuery(sql).withParams(boundSql.getParams().toArray()).executeAndFetch(entity.getClass());
            if(null != list){
                paginator.setList((List<T>) list);
            }
        }

        return paginator;
    }

    @Override
    public <T> Paginator<T> page(T entity, int page, int limit, String orderBy) {
        return this.page(entity, new PageRow(page, limit, orderBy));
    }

    @Override
    public <T> Paginator<T> page(Criteria criteria, PageRow pageRow) {
        BoundSql boundSql = SqlAssembleUtils.buildQuerySql(null, criteria, this.getNameHandler());
        String countSql = Utils.getCountSql(boundSql.getSql());

        Paginator<T> paginator = null;

        try (Connection con = sql2o.open()){
            int total = con.createQuery(countSql).withParams(boundSql.getParams().toArray()).executeScalar(Integer.class);

            paginator = new Paginator<>(total, pageRow.getPage(), pageRow.getLimit());

            String sql = Utils.getPageSql(boundSql.getSql(), dialect, pageRow);
            List<?> list = con.createQuery(sql).withParams(boundSql.getParams().toArray()).executeAndFetch(criteria.getEntityClass());
            if(null != list){
                paginator.setList((List<T>) list);
            }
        }

        return paginator;
    }

    /**
     * 获取名称处理器
     *
     * @return
     */
    protected NameHandler getNameHandler() {
        if (this.nameHandler == null) {
            this.nameHandler = new DefaultNameHandler();
        }
        return this.nameHandler;
    }

    public Sql2o getSql2o() {
        return sql2o;
    }

    public SampleActiveRecord setSql2o(Sql2o sql2o) {
        this.sql2o = sql2o;
        return this;
    }

    public void setNameHandler(NameHandler nameHandler) {
        this.nameHandler = nameHandler;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }
}
