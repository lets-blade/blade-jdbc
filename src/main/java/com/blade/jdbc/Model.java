package com.blade.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import com.blade.jdbc.annotation.Table;
import com.blade.jdbc.dialect.DefaultDialect;
import com.blade.jdbc.dialect.Dialect;
import com.blade.jdbc.kit.QueryKit;

@SuppressWarnings("unchecked")
public class Model extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(Model.class);

	private Class<? extends Model> clazz;

	public static Sql2o sql2o;
	
	private Query query;

	private Dialect dialect = new DefaultDialect();
	
	private Map<ParamKey, Object> params = new TreeMap<ParamKey, Object>();
	
	private String order;
	
	public Model() {
		this.clazz = this.getClass();
	}
	
	public Model where(String name, Object value) {
		int index = params.size() + 1;
		if(name.indexOf('?') != -1){
			String[] opt = QueryKit.getOpts(name);
			this.params.put(new ParamKey(index, opt[0], opt[1]), value);
		} else {
			this.params.put(new ParamKey(index, name), value);
		}
		return this;
	}

	public Model where(String name, String opt, Object value) {
		int index = params.size() + 1;
		this.params.put(new ParamKey(index, name, opt), value);
		return this;
	}
	
	public Model where(String wheres, Object... values) {
		int index = params.size() + 1;
		ParamKey[] params = QueryKit.getParams(index, wheres);
		for (int i = 0, len = values.length; i < len; i++) {
			ParamKey paramKey = params[i];
			Object value = values[i];
			this.params.put(paramKey, value);
		}
		return this;
	}

	public Model set(String name, Object value) {
		this.put(name, value);
		return this;
	}
	
	public <T> T get(String name) {
		Object value = super.get(name);
		if(null != value){
			return (T) value;
		}
		return null;
	}
	
	public <K> K save() {
		String sql = dialect.getSaveSql(this);
		LOGGER.debug("Preparing\t=> {}", sql);
		Query query = sql2o.open().createQuery(sql);

		Collection<Object> vlaues = this.values();
		Object[] paramValues = vlaues.toArray(new Object[vlaues.size()]);
		query.withParams(paramValues);
		
		return (K) query.executeUpdate().getKey();
	}
	
	public void addToBatch(){
		if(null == query){
			String sql = dialect.getSaveSql(this);
			query = sql2o.beginTransaction().createQuery(sql);
		}
		Collection<Object> vlaues = this.values();
		Object[] paramValues = vlaues.toArray(new Object[vlaues.size()]);
		query.withParams(paramValues).addToBatch();
		this.clear();
	}
	
	public void saveBatch(){
		if(null != query){
			query.executeBatch();
	        query.getConnection().commit();
		}
	}
	
	public void update() {
		String sql = dialect.getUpdateSql(this);
		LOGGER.debug("Preparing\t=> {}", sql);
		Query query = sql2o.open().createQuery(sql);

		List<Object> vlaues = new ArrayList<Object>(this.values());
		if (!this.params.isEmpty()) {
			vlaues.addAll(this.params.values());
		}
		
		Object[] paramValues = vlaues.toArray(new Object[vlaues.size()]);
		query.withParams(paramValues);
		query.executeUpdate();
	}

	public void delete(){
		String sql = dialect.getDeleteSql(this);
		LOGGER.debug("Preparing\t=> {}", sql);
		Query query = sql2o.open().createQuery(sql);
		
		List<Object> vlaues = new ArrayList<Object>(this.values());
		if (!this.params.isEmpty()) {
			vlaues.addAll(this.params.values());
		}
		
		Object[] paramValues = vlaues.toArray(new Object[vlaues.size()]);
		query.withParams(paramValues);
		
		query.executeUpdate();
	}
	
	public Model order(String order){
		this.order = order;
		return this;
	}
	
	public <T extends Model> List<T> all() {
		return this.list();
	}

	public <T extends Model> List<T> list() {
		return this.list(null);
	}
	
	public <T extends Model> List<T> list(String sql) {
		if(null == sql){
			sql = dialect.getQuerySql(this);
		}
		LOGGER.debug("Preparing\t=> {}", sql);
		Query query = sql2o.open().createQuery(sql);
		if (!this.params.isEmpty()) {
			Object[] paramValues = this.params.values().toArray(new Object[this.params.size()]);
			query.withParams(paramValues);
		}
		return (List<T>) query.executeAndFetchModels(clazz);
	}
	
	public <T extends Model> Paginator<T> page(int page, int limit) {
		
		// query count
		long total = this.count();
		Paginator<T> pager = new Paginator<T>(total, page, limit);
		
		int offset = (pager.getPageNum() - 1) * limit;
		
		String sql = dialect.getQueryPageSql(this);
		
		int index = params.size() + 1;
		this.params.put(new ParamKey(index, "offset"), offset);
		this.params.put(new ParamKey(index + 1, "limit"), limit);
		
		List<T> result = this.list(sql);
		pager.setList(result);
		return pager;
	}
	
	public <T extends Model> T findById(Serializable pk) {
		int index = params.size() + 1;
		this.params.put(new ParamKey(index, this.pkName()), pk);
		return this.findOne();
	}
	
	public <T extends Model> T findOne() {
		String sql = dialect.getQueryOneSql(this);
		LOGGER.debug("Preparing\t=> {}", sql);
		Query query = sql2o.open().createQuery(sql);
		
		if (!this.params.isEmpty()) {
			Object[] paramValues = this.params.values().toArray(new Object[this.params.size()]);
			query.withParams(paramValues);
		}
		return (T) query.executeAndFetchTable().asModel(clazz).get(0);
	}
	
	public int count(){
		String sql = dialect.getQueryCountSql(this);
		LOGGER.debug("Preparing\t=> {}", sql);
		Query query = sql2o.open().createQuery(sql);
		
		if (!this.params.isEmpty()) {
			Object[] paramValues = this.params.values().toArray(new Object[this.params.size()]);
			query.withParams(paramValues);
		}
		return query.executeScalar(Integer.class);
	}
	
	/** meta data **/
	public String table() {
		return clazz.getAnnotation(Table.class).name();
	}

	public String pkName() {
		return clazz.getAnnotation(Table.class).pk();
	}

	public Class<? extends Model> clazz() {
		return this.clazz;
	}

	public Map<ParamKey, Object> params() {
		return this.params;
	}
	
	public String order(){
		return this.order;
	}
}
