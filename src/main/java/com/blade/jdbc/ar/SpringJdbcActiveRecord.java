package com.blade.jdbc.ar;

import com.blade.jdbc.ActiveRecord;
import com.blade.jdbc.model.PageRow;
import com.blade.jdbc.model.Paginator;
import com.blade.jdbc.core.*;
import com.blade.jdbc.tx.AtomTx;
import com.blade.jdbc.utils.ClassUtils;
import com.blade.jdbc.utils.NameUtils;
import com.blade.jdbc.utils.Utils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Created by biezhi on 2016/12/29.
 */
public class SpringJdbcActiveRecord implements ActiveRecord {

    /** spring jdbcTemplate 对象 */
    protected JdbcOperations jdbcTemplate;

    /** 名称处理器，为空按默认执行 */
    protected NameHandler nameHandler;

    /** rowMapper，为空按默认执行 */
    protected String         rowMapperClass;

    /** 数据库方言 */
    protected String         dialect;

    private Object[] EMPTY = new Object[]{};

    /**
     * 插入数据
     *
     * @param entity the entity
     * @param criteria the criteria
     * @return long long
     */
    private <T extends Serializable> T insert(Object entity, Take criteria) {
        Class<?> entityClass = SqlAssembleUtils.getEntityClass(entity, criteria);
        NameHandler handler = this.getNameHandler();
        String pkValue = handler.getPKValue(entityClass, this.dialect);
        if (!Utils.blank(pkValue)) {
            String primaryName = handler.getPKName(entityClass);
            if (criteria == null) {
                criteria = Take.create(entityClass);
            }
            criteria.setPKValueName(NameUtils.getCamelName(primaryName), pkValue);
        }
        final BoundSql boundSql = SqlAssembleUtils.buildInsertSql(entity, criteria,
                this.getNameHandler());

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
                PreparedStatement ps = con.prepareStatement(boundSql.getSql(),
                        new String[] { boundSql.getPrimaryKey() });
                int index = 0;
                for (Object param : boundSql.getParams()) {
                    index++;
                    ps.setObject(index, param);
                }
                return ps;
            }
        }, keyHolder);
        return (T) keyHolder.getKey();
    }

    @Override
    public <T extends Serializable> T insert(Object entity) {
        return (T) this.insert(entity, null);
    }

    @Override
    public Long insert(Take criteria) {
        return this.insert(null, criteria);
    }

    @Override
    public void save(Object entity) {
        final BoundSql boundSql = SqlAssembleUtils.buildInsertSql(entity, null,
                this.getNameHandler());
        jdbcTemplate.update(boundSql.getSql(), boundSql.getParams().toArray());
    }

    @Override
    public void save(Take criteria) {
        final BoundSql boundSql = SqlAssembleUtils.buildInsertSql(null, criteria,
                this.getNameHandler());
        jdbcTemplate.update(boundSql.getSql(), boundSql.getParams().toArray());
    }

    @Override
    public int update(Take criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildUpdateSql(null, criteria, this.getNameHandler());
        return jdbcTemplate.update(boundSql.getSql(), boundSql.getParams().toArray());
    }

    @Override
    public int update(Object entity) {
        BoundSql boundSql = SqlAssembleUtils.buildUpdateSql(entity, null, this.getNameHandler());
        return jdbcTemplate.update(boundSql.getSql(), boundSql.getParams().toArray());
    }

    @Override
    public int delete(Take criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildDeleteSql(null, criteria, this.getNameHandler());
        return jdbcTemplate.update(boundSql.getSql(), boundSql.getParams().toArray());
    }

    @Override
    public int delete(Object entity) {
        BoundSql boundSql = SqlAssembleUtils.buildDeleteSql(entity, null, this.getNameHandler());
        return jdbcTemplate.update(boundSql.getSql(), boundSql.getParams().toArray());
    }

    @Override
    public int delete(Class<?> clazz, Serializable id) {
        BoundSql boundSql = SqlAssembleUtils.buildDeleteSql(clazz, id, this.getNameHandler());
        return jdbcTemplate.update(boundSql.getSql(), boundSql.getParams().toArray());
    }

    @Override
    public int deleteAll(Class<?> clazz) {
        String tableName = this.getNameHandler().getTableName(clazz);
        String sql = "TRUNCATE TABLE " + tableName;
        jdbcTemplate.execute(sql);
        return 1;
    }

    @Override
    public <T extends Serializable> List<T> list(Take criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildListSql(null, criteria, this.getNameHandler());
        List<?> list = jdbcTemplate.query(boundSql.getSql(), boundSql.getParams().toArray(),
                this.getRowMapper(criteria.getEntityClass()));
        return (List<T>) list;
    }

    @Override
    public <T extends Serializable> List<T> list(Class<T> type, String sql, Object... args) {
        if(null == args){
            args = EMPTY;
        }
        List<?> list = jdbcTemplate.query(sql, args, this.getRowMapper(type));
        return (List<T>) list;
    }

    @Override
    public <T extends Serializable> List<T> list(T entity) {
        BoundSql boundSql = SqlAssembleUtils.buildListSql(entity, null, this.getNameHandler());
        List<?> list = jdbcTemplate.query(boundSql.getSql(), boundSql.getParams().toArray(),
                this.getRowMapper(entity.getClass()));
        return (List<T>) list;
    }

    @Override
    public List<Map<String, Object>> listMap(String sql, Object... args) {
        if(null != args && args.length > 0){
            return jdbcTemplate.queryForList(sql, args);
        }
        return jdbcTemplate.queryForList(sql);
    }


    @Override
    public <T extends Serializable> List<T> list(T entity, Take criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildListSql(entity, criteria, this.getNameHandler());
        List<?> list = jdbcTemplate.query(boundSql.getSql(), boundSql.getParams().toArray(),
                this.getRowMapper(entity.getClass()));
        return (List<T>) list;
    }

    @Override
    public Map<String, Object> map(String sql, Object... args) {
        if(null != args && args.length > 0){
            return jdbcTemplate.queryForMap(sql, args);
        }
        return jdbcTemplate.queryForMap(sql);
    }

    @Override
    public <T extends Serializable> T one(Class<T> type, String sql, Object... args) {
        if(null != args && args.length > 0){
            return jdbcTemplate.queryForObject(sql, args, type);
        }
        return jdbcTemplate.queryForObject(sql, type);
    }

    @Override
    public int count(Object entity, Take criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildCountSql(entity, criteria, this.getNameHandler());
        return jdbcTemplate.queryForInt(boundSql.getSql(), boundSql.getParams().toArray());
    }

    @Override
    public int count(Object entity) {
        BoundSql boundSql = SqlAssembleUtils.buildCountSql(entity, null, this.getNameHandler());
        return jdbcTemplate.queryForInt(boundSql.getSql(), boundSql.getParams().toArray());
    }

    @Override
    public int count(Take criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildCountSql(null, criteria, this.getNameHandler());
        return jdbcTemplate.queryForInt(boundSql.getSql(), boundSql.getParams().toArray());
    }

    @Override
    public <T extends Serializable> T byId(Class<T> clazz, Serializable id) {
        BoundSql boundSql = SqlAssembleUtils.buildByIdSql(clazz, id, null, this.getNameHandler());

        //采用list方式查询，当记录不存在时返回null而不会抛出异常
        List<T> list = jdbcTemplate.query(boundSql.getSql(), this.getRowMapper(clazz), id);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.iterator().next();
    }

    @Override
    public <T extends Serializable> T byId(Take criteria, Serializable pk) {
        return null;
    }

    /*@Override
    public <T extends Serializable> T get(Take criteria, Long id) {
        BoundSql boundSql = SqlAssembleUtils
                .buildByIdSql(null, id, criteria, this.getNameHandler());

        //采用list方式查询，当记录不存在时返回null而不会抛出异常
        List<T extends Serializable> list = (List<T>) jdbcTemplate.query(boundSql.getSql(),
                this.getRowMapper(criteria.getEntityClass()), id);
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.iterator().next();
    }*/

    @Override
    public <T extends Serializable> T one(T entity) {
        BoundSql boundSql = SqlAssembleUtils.buildQuerySql(entity, null, this.getNameHandler());

        //采用list方式查询，当记录不存在时返回null而不会抛出异常
        List<?> list = jdbcTemplate.query(boundSql.getSql(), boundSql.getParams().toArray(),
                this.getRowMapper(entity.getClass()));
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return (T) list.iterator().next();
    }

    @Override
    public <T extends Serializable> T one(Take criteria) {
        BoundSql boundSql = SqlAssembleUtils.buildQuerySql(null, criteria, this.getNameHandler());
        //采用list方式查询，当记录不存在时返回null而不会抛出异常
        List<?> list = jdbcTemplate.query(boundSql.getSql(), boundSql.getParams().toArray(),
                this.getRowMapper(criteria.getEntityClass()));
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return (T) list.iterator().next();
    }

    @Override
    public int execute(String sql, Object... args) {
        if(null != args && args.length > 0){
        } else {
            jdbcTemplate.execute(sql);
        }
        return 1;
    }

    @Override
    public void run(AtomTx tx) {

    }

    @Override
    public <T> Paginator<T> page(T entity, int page, int limit) {
        return null;
    }

    @Override
    public <T> Paginator<T> page(T entity, int page, int limit, String orderBy) {
        return null;
    }

    @Override
    public <T> Paginator<T> page(T entity, PageRow pageRow) {
        return null;
    }

    @Override
    public <T> Paginator<T> page(Take take) {
        return null;
    }

    /**
     * 获取rowMapper对象
     *
     * @param clazz
     * @return
     */
    protected <T> RowMapper<T> getRowMapper(Class<T> clazz) {

        if (Utils.blank(rowMapperClass)) {
            return BeanPropertyRowMapper.newInstance(clazz);
        } else {
            return (RowMapper<T>) ClassUtils.newInstance(rowMapperClass);
        }
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

    public void setJdbcTemplate(JdbcOperations jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void setNameHandler(NameHandler nameHandler) {
        this.nameHandler = nameHandler;
    }

    public void setRowMapperClass(String rowMapperClass) {
        this.rowMapperClass = rowMapperClass;
    }

    public void setDialect(String dialect) {
        this.dialect = dialect;
    }

}
