package com.blade.jdbc.core;

import com.blade.jdbc.model.QueryOpts;
import com.blade.jdbc.exceptions.AssistantException;
import com.blade.jdbc.utils.ClassUtils;
import com.blade.jdbc.utils.CollectionUtils;
import com.blade.jdbc.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.BeanInfo;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SqlAssembleUtils {

    /** 日志对象 */
    private static final Logger LOG    = LoggerFactory.getLogger(SqlAssembleUtils.class);

    /**
     * 获取实体类对象
     * 
     * @param entity
     * @param take
     * @return
     */
    public static Class<?> getEntityClass(Object entity, Take take) {
        return entity == null ? take.getEntityClass() : entity.getClass();
    }

    /**
     * 构建insert语句
     *
     * @param entity 实体映射对象
     * @param take the take
     * @param nameHandler 名称转换处理器
     * @return bound sql
     */
    public static BoundSql buildInsertSql(Object entity, Take take, NameHandler nameHandler) {

        Class<?> entityClass = getEntityClass(entity, take);
        List<AutoField> autoFields = (take != null ? take.getAutoFields()
            : new ArrayList<>());

        List<AutoField> entityAutoField = getEntityAutoField(entity, AutoField.UPDATE_FIELD);

        //添加到后面
        autoFields.addAll(entityAutoField);

        String tableName = nameHandler.getTableName(entityClass);
        String pkName = nameHandler.getPKName(entityClass);

        StringBuilder sql = new StringBuilder(QueryOpts.INSERT_INTO);
        List<Object> params = new ArrayList<>();
        sql.append(tableName);

        sql.append("(");
        StringBuilder args = new StringBuilder();
        args.append("(");

        for (AutoField autoField : autoFields) {

            if (autoField.getType() != AutoField.UPDATE_FIELD
                && autoField.getType() != AutoField.PK_VALUE_NAME) {
                continue;
            }
            String columnName = nameHandler.getColumnName(autoField.getName());
            Object value = autoField.getValues()[0];

            sql.append(columnName);
            //如果是主键，且是主键的值名称
            if (pkName.equalsIgnoreCase(columnName)
                && autoField.getType() == AutoField.PK_VALUE_NAME) {
                //参数直接append，传参方式会把值当成字符串造成无法调用序列的问题
                args.append(value);
            } else {
                args.append(" ?");
                params.add(value);
            }
            sql.append(",");
            args.append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        args.deleteCharAt(args.length() - 1);
        args.append(")");
        sql.append(")");
        sql.append(" values ");
        sql.append(args);
        return new BoundSql(sql.toString(), pkName, params);
    }

    /**
     * 构建更新sql
     *
     * @param entity the entity
     * @param take the take
     * @param nameHandler the name handler
     * @return bound sql
     */
    public static BoundSql buildUpdateSql(Object entity, Take take, NameHandler nameHandler) {

        Class<?> entityClass = getEntityClass(entity, take);
        List<AutoField> autoFields = (take != null ? take.getAutoFields()
            : new ArrayList<>());

        List<AutoField> entityAutoField = getEntityAutoField(entity, AutoField.UPDATE_FIELD);

        //添加到后面，防止or等操作被覆盖
        autoFields.addAll(entityAutoField);

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<Object>();
        String tableName = nameHandler.getTableName(entityClass);
        String primaryName = nameHandler.getPKName(entityClass);

        sql.append("update ").append(tableName).append(" set ");

        Object primaryValue = null;

        Iterator<AutoField> iterator = autoFields.iterator();
        while (iterator.hasNext()) {

            AutoField autoField = iterator.next();

            if (AutoField.UPDATE_FIELD != autoField.getType()) {
                continue;
            }

            String columnName = nameHandler.getColumnName(autoField.getName());

            //如果是主键
            if (primaryName.equalsIgnoreCase(columnName)) {

                Object[] values = autoField.getValues();

                if (null == values || values.length == 0 || StringUtils.isBlank(values[0].toString())) {
                    throw new AssistantException("primary key not is null");
                }
                primaryValue = values[0];
            }

            //白名单 黑名单
            if (take != null && !CollectionUtils.isEmpty(take.getIncludeFields())
                && !take.getIncludeFields().contains(autoField.getName())) {
                continue;
            } else if (take != null && !CollectionUtils.isEmpty(take.getExcludeFields())
                       && take.getExcludeFields().contains(autoField.getName())) {
                continue;
            }

            if(!primaryName.equalsIgnoreCase(columnName)){
                sql.append(columnName).append(" ").append(autoField.getFieldOperator()).append(" ");
                if (null == autoField.getValues() || autoField.getValues().length == 0 || autoField.getValues()[0] == null) {
                    sql.append("null");
                } else {
                    sql.append(" ?");
                    params.add(autoField.getValues()[0]);
                }
                sql.append(",");

                //移除掉操作过的元素
                iterator.remove();
            }

        }

        sql.deleteCharAt(sql.length() - 1);
        sql.append(" where ");

        if (primaryValue != null) {
            sql.append(primaryName).append(" = ?");
            params.add(primaryValue);
        } else {
            BoundSql boundSql = SqlAssembleUtils.builderWhereSql(autoFields, nameHandler);
            sql.append(boundSql.getSql());
            params.addAll(boundSql.getParams());
        }
        return new BoundSql(sql.toString(), primaryName, params);
    }

    /**
     * 获取所有的操作属性，entity非null字段将被转换到列表
     *
     * @param entity the entity
     * @param operateType the operate type
     * @return all auto field
     */
    private static List<AutoField> getEntityAutoField(Object entity, int operateType) {

        //汇总的所有操作属性
        List<AutoField> autoFieldList = new ArrayList<>();

        if (entity == null) {
            return autoFieldList;
        }

        //获取属性信息
        BeanInfo beanInfo = ClassUtils.getSelfBeanInfo(entity.getClass());
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

        AutoField autoField;
        for (PropertyDescriptor pd : pds) {

            String fieldName = pd.getName();

            //null值，忽略 (单独指定的可以指定为null)
            Object value = getReadMethodValue(pd.getReadMethod(), entity);
            if (value == null) {
                continue;
            }

            if (value instanceof String && StringUtils.isBlank(value.toString())) {
                continue;
            }

            autoField = new AutoField();
            autoField.setName(fieldName);
            autoField.setSqlOperator("and");
            autoField.setFieldOperator("=");
            autoField.setValues(new Object[] { value });
            autoField.setType(operateType);

            autoFieldList.add(autoField);
        }
        return autoFieldList;
    }

    /**
     * 构建where条件sql
     *
     * @param autoFields the auto fields
     * @param nameHandler the name handler
     * @return bound sql
     */
    private static BoundSql builderWhereSql(List<AutoField> autoFields, NameHandler nameHandler) {

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<Object>();
        Iterator<AutoField> iterator = autoFields.iterator();
        while (iterator.hasNext()) {
            AutoField autoField = iterator.next();
            if (AutoField.WHERE_FIELD != autoField.getType()) {
                continue;
            }
            //操作过，移除
            iterator.remove();
            if (sql.length() > 0) {
                sql.append(" ").append(autoField.getSqlOperator()).append(" ");
            }
            String columnName = nameHandler.getColumnName(autoField.getName());
            Object[] values = autoField.getValues();

            if (QueryOpts.IN.equalsIgnoreCase(StringUtils.trim(autoField.getFieldOperator()))
                || QueryOpts.NOT_IN.equalsIgnoreCase(StringUtils.trim(autoField.getFieldOperator()))) {

                //in，not in的情况
                sql.append(columnName).append(" ").append(autoField.getFieldOperator()).append(" ");
                sql.append("(");
                for (int j = 0; j < values.length; j++) {
                    sql.append(" ?");
                    params.add(values[j]);
                    if (j != values.length - 1) {
                        sql.append(",");
                    }
                }
                sql.append(")");
            } else if (values == null) {
                //null 值
                sql.append(columnName).append(" ").append(autoField.getFieldOperator())
                    .append(" null");
            } else if (values.length == 1) {
                //一个值 =
                sql.append(columnName).append(" ").append(autoField.getFieldOperator())
                    .append(" ?");
                params.add(values[0]);
            } else {
                //多个值，or的情况
                sql.append("(");
                for (int j = 0; j < values.length; j++) {
                    sql.append(columnName).append(" ").append(autoField.getFieldOperator())
                        .append(" ?");
                    params.add(values[j]);
                    if (j != values.length - 1) {
                        sql.append(" or ");
                    }
                }
                sql.append(")");
            }
        }
        return new BoundSql(sql.toString(), null, params);
    }

    /**
     * 构建根据主键删除sql
     *
     * @param clazz
     * @param id
     * @param nameHandler
     * @return
     */
    public static BoundSql buildDeleteSql(Class<?> clazz, Serializable id, NameHandler nameHandler) {

        List<Object> params = new ArrayList<Object>();
        params.add(id);
        String tableName = nameHandler.getTableName(clazz);
        String primaryName = nameHandler.getPKName(clazz);
        String sql = "delete from " + tableName + " where " + primaryName + " = ?";
        return new BoundSql(sql, primaryName, params);
    }

    /**
     * 构建删除sql
     *
     * @param entity the entity
     * @param take the take
     * @param nameHandler the name handler
     * @return bound sql
     */
    public static BoundSql buildDeleteSql(Object entity, Take take, NameHandler nameHandler) {

        Class<?> entityClass = getEntityClass(entity, take);
        List<AutoField> autoFields = (take != null ? take.getAutoFields()
            : new ArrayList<>());

        List<AutoField> entityAutoField = getEntityAutoField(entity, AutoField.WHERE_FIELD);

        autoFields.addAll(entityAutoField);

        String tableName = nameHandler.getTableName(entityClass);
        String primaryName = nameHandler.getPKName(entityClass);

        StringBuilder sql = new StringBuilder("delete from " + tableName + " where ");
        BoundSql boundSql = SqlAssembleUtils.builderWhereSql(autoFields, nameHandler);
        boundSql.setSql(sql.append(boundSql.getSql()).toString());
        boundSql.setPrimaryKey(primaryName);

        return boundSql;
    }

    /**
     * 构建根据id查询sql
     *
     * @param clazz the clazz
     * @param pk the id
     * @param take the take
     * @param nameHandler the name handler
     * @return bound sql
     */
    public static BoundSql buildByIdSql(Class<?> clazz, Serializable pk, Take take,
                                        NameHandler nameHandler) {

        Class<?> entityClass = (clazz == null ? take.getEntityClass() : clazz);
        String tableName = nameHandler.getTableName(entityClass);
        String primaryName = nameHandler.getPKName(entityClass);
        String columns = SqlAssembleUtils.buildColumnSql(entityClass, nameHandler,
                take == null ? null : take.getIncludeFields(), take == null ? null
                        : take.getExcludeFields());
        String sql = "select " + columns + " from " + tableName + " where " + primaryName + " = ?";
        List<Object> params = new ArrayList<>();
        params.add(pk);

        return new BoundSql(sql, primaryName, params);
    }

    /**
     * 按设置的条件构建查询sql
     *
     * @param entity the entity
     * @param take the take
     * @param nameHandler the name handler
     * @return bound sql
     */
    public static BoundSql buildQuerySql(Object entity, Take take, NameHandler nameHandler) {

        Class<?> entityClass = getEntityClass(entity, take);

        List<AutoField> autoFields = (take != null ? take.getAutoFields()
            : new ArrayList<>());

        String tableName = nameHandler.getTableName(entityClass);
        String primaryName = nameHandler.getPKName(entityClass);

        List<AutoField> entityAutoField = getEntityAutoField(entity, AutoField.WHERE_FIELD);
        autoFields.addAll(entityAutoField);

        String columns = SqlAssembleUtils.buildColumnSql(entityClass, nameHandler,
                take == null ? null : take.getIncludeFields(), take == null ? null
                        : take.getExcludeFields());
        StringBuilder querySql = new StringBuilder("select " + columns + " from ");
        querySql.append(tableName);

        List<Object> params = Collections.EMPTY_LIST;
        if ( null != take && take.hasWhere() || autoFields.size() > 0 ) {
            querySql.append(" where ");

            BoundSql boundSql = SqlAssembleUtils.builderWhereSql(autoFields, nameHandler);
            params = boundSql.getParams();
            querySql.append(boundSql.getSql());
        }

        return new BoundSql(querySql.toString(), primaryName, params);
    }

    /**
     * 构建列表查询sql
     *
     * @param entity the entity
     * @param take the take
     * @param nameHandler the name handler
     * @return bound sql
     */
    public static BoundSql buildListSql(Object entity, Take take, NameHandler nameHandler) {

        BoundSql boundSql = SqlAssembleUtils.buildQuerySql(entity, take, nameHandler);

        StringBuilder sb = new StringBuilder(" order by ");
        if (take != null) {
            for (AutoField autoField : take.getOrderByFields()) {
                sb.append(nameHandler.getColumnName(autoField.getName())).append(" ")
                    .append(autoField.getFieldOperator()).append(",");
            }

            if (sb.length() > 10) {
                sb.deleteCharAt(sb.length() - 1);
            }
        }

        if (sb.length() < 11) {
            sb.append(boundSql.getPrimaryKey()).append(" desc");
        }
        boundSql.setSql(boundSql.getSql() + sb.toString());
        return boundSql;
    }

    /**
     * 构建记录数查询sql
     *
     * @param entity the entity
     * @param take the take
     * @param nameHandler the name handler
     * @return bound sql
     */
    public static BoundSql buildCountSql(Object entity, Take take, NameHandler nameHandler) {

        Class<?> entityClass = getEntityClass(entity, take);
        List<AutoField> autoFields = (take != null ? take.getAutoFields()
            : new ArrayList<>());

        List<AutoField> entityAutoField = getEntityAutoField(entity, AutoField.WHERE_FIELD);
        autoFields.addAll(entityAutoField);

        String tableName = nameHandler.getTableName(entityClass);
        StringBuilder countSql = new StringBuilder("select count(0) from ");
        countSql.append(tableName);

        List<Object> params = Collections.EMPTY_LIST;
        if (!CollectionUtils.isEmpty(autoFields)) {
            countSql.append(" where ");
            BoundSql boundSql = builderWhereSql(autoFields, nameHandler);
            countSql.append(boundSql.getSql());
            params = boundSql.getParams();
        }

        return new BoundSql(countSql.toString(), null, params);
    }

    /**
     * 构建查询的列sql
     *
     * @param clazz the clazz
     * @param nameHandler the name handler
     * @param includeField the include field
     * @param excludeField the exclude field
     * @return string string
     */
    public static String buildColumnSql(Class<?> clazz, NameHandler nameHandler,
                                        List<String> includeField, List<String> excludeField) {

        StringBuilder columns = new StringBuilder();

        //获取属性信息
        BeanInfo beanInfo = ClassUtils.getSelfBeanInfo(clazz);
        PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

        for (PropertyDescriptor pd : pds) {

            String fieldName = pd.getName();

            //白名单 黑名单
            if (!CollectionUtils.isEmpty(includeField) && !includeField.contains(fieldName)) {
                continue;
            } else if (!CollectionUtils.isEmpty(excludeField) && excludeField.contains(fieldName)) {
                continue;
            }

            String columnName = nameHandler.getColumnName(fieldName);
            columns.append(columnName);
            columns.append(",");
        }
        columns.deleteCharAt(columns.length() - 1);
        return columns.toString();
    }

    /**
     * 构建排序条件
     *
     * @param sort
     * @param nameHandler
     * @param properties
     */
    public static String buildOrderBy(String sort, NameHandler nameHandler, String... properties) {

        StringBuilder sb = new StringBuilder();
        for (String property : properties) {
            String columnName = nameHandler.getColumnName(property);
            sb.append(columnName);
            sb.append(" ");
            sb.append(sort);
            sb.append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    /**
     * 构建查询oracle xmltype类型的sql
     *
     * @param clazz
     * @param fieldName
     * @param id
     * @param nameHandler
     * @return
     */
    public static BoundSql buildOracleXmlTypeSql(Class<?> clazz, String fieldName, Long id,
                                                 NameHandler nameHandler) {
        String tableName = nameHandler.getTableName(clazz);
        String primaryName = nameHandler.getPKName(clazz);
        String columnName = nameHandler.getColumnName(fieldName);

        String sql_tmp = "select t.%s.getclobval() xmlFile from %s t where t.%s = ?";
        String sql = String.format(sql_tmp, columnName, tableName, primaryName);
        List<Object> params = new ArrayList<Object>();
        params.add(id);
        return new BoundSql(sql, primaryName, params);
    }

    /**
     * 获取属性值
     *
     * @param readMethod
     * @param entity
     * @return
     */
    private static Object getReadMethodValue(Method readMethod, Object entity) {
        if (readMethod == null) {
            return null;
        }
        try {
            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                readMethod.setAccessible(true);
            }
            return readMethod.invoke(entity);
        } catch (Exception e) {
            LOG.error("get property error", e);
            throw new AssistantException(e);
        }
    }
}
