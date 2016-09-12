package com.blade.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sql2o.Connection;
import org.sql2o.Query;
import org.sql2o.Sql2o;

import com.blade.jdbc.annotation.Table;
import com.blade.jdbc.dialect.DefaultDialect;
import com.blade.jdbc.dialect.Dialect;
import com.blade.jdbc.exception.DBException;
import com.blade.jdbc.kit.QueryKit;
import com.blade.jdbc.tx.AtomTx;

@SuppressWarnings("unchecked")
public class Model extends HashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(Model.class);

	private Class<? extends Model> clazz;
	
	private Sql2o sql2o;
	
	private Connection connection;
	
	private Query query;

	private Dialect dialect = new DefaultDialect();
	
	private Map<ParamKey, Object> params = new TreeMap<ParamKey, Object>();
	
	private PageRow pageRow;
	
	private String sql;
	
	private String order;
	
	public Model() {
		this.clazz = this.getClass();
		this.sql2o = Base.database();
	}
	
	public Model db(String name){
		this.sql2o = Base.database();
		return this;
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
	
	public Model sql(String sql){
		this.sql = sql;
		return this;
	}
	
	public Model sql(String sql, Object...values){
		this.sql = sql;
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
		
		if(!this.isEmpty()){
			LOGGER.debug("Parameters\t=> {}", Arrays.toString(this.values().toArray()));
		}
		
		if(null == this.connection){
			this.connection = sql2o.open();
		}
		
		this.query = connection.createQuery(sql);
		Collection<Object> vlaues = this.values();
		Object[] paramValues = vlaues.toArray(new Object[vlaues.size()]);
		this.query.withParams(paramValues);
		K k = (K) this.query.executeUpdate().getKey();
		this.clear();
		return k;
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
	
	public int[] saveBatch(){
		if(null != query){
			int[] result = query.executeBatch().getBatchResult();
	        query.getConnection().commit();
	        this.clear();
	        return result;
		}
		return null;
	}
	
	public int update() {
		String sql = dialect.getUpdateSql(this);
		LOGGER.debug("Preparing\t=> {}", sql);
		
		Object[] args = this.params.values().toArray();
		
		if(!this.isEmpty()){
			if(null == args){
				args = this.values().toArray();
			} else{
				List<Object> argList = new ArrayList<Object>(this.values());
				argList.addAll(this.params.values());
				args = argList.toArray();
			}
		}
		
		if(null != args){
			LOGGER.debug("Parameters\t=> {}", Arrays.toString(args));
		}
		
		if(null == this.connection){
			this.connection = sql2o.open();
		}
		Query query = this.connection.createQuery(sql);
		List<Object> vlaues = new ArrayList<Object>(this.values());
		if (!this.params.isEmpty()) {
			vlaues.addAll(this.params.values());
		}
		
		Object[] paramValues = vlaues.toArray(new Object[vlaues.size()]);
		query.withParams(paramValues);
		int result = query.executeUpdate().getResult();
		this.clear();
		return result;
	}
	
	public void execute() {
		
	}

	public int delete(){
		String sql = dialect.getDeleteSql(this);
		LOGGER.debug("Preparing\t=> {}", sql);
		
		if(!this.params.isEmpty()){
			LOGGER.debug("Parameters\t=> {}", Arrays.toString(this.params.values().toArray()));
		}
		
		if(null == this.connection){
			this.connection = sql2o.open();
		}
		Query query = this.connection.createQuery(sql);
		
		List<Object> vlaues = new ArrayList<Object>(this.values());
		if (!this.params.isEmpty()) {
			vlaues.addAll(this.params.values());
		}
		
		Object[] paramValues = vlaues.toArray(new Object[vlaues.size()]);
		query.withParams(paramValues);
		int result = query.executeUpdate().getResult();
		this.clear();
		return result;
	}
	
	public void tx(AtomTx atomTx){
		this.connection = sql2o.beginTransaction();
		try {
			atomTx.execute();
			this.connection.commit();
			this.clear();
		} catch (DBException e) {
			LOGGER.error(e.getMessage(), e);
			this.connection.rollback();
		}finally {
			this.close(this.connection);
		}
	}
	
	public Model order(String order){
		this.order = order;
		return this;
	}
	
	public <T extends Model> List<T> all() {
		return this.list(null);
	}

	public <T extends Model> List<T> list() {
		return this.list(this.sql);
	}
	
	private <T extends Model> List<T> list(String sql) {
		
		String querySql = dialect.getQuerySql(sql, this);
		
		LOGGER.debug("Preparing\t=> {}", querySql);
		
		Query query = sql2o.open().createQuery(querySql);
		
		if (!this.params.isEmpty()) {
			Object[] paramValues = this.params.values().toArray();
			query.withParams(paramValues);
			LOGGER.debug("Parameters\t=> {}", Arrays.toString(paramValues));
		}
		
		List<T> list = (List<T>) query.executeAndFetchModels(clazz);
		this.clear();
		return list;
	}
	
	public <T extends Model> Paginator<T> page(int page, int limit) {
		// query count
		long total = this.count(false);
		Paginator<T> pager = new Paginator<T>(total, page, limit);
		int offset = (pager.getPageNum() - 1) * limit;
		pageRow = new PageRow(offset, limit);
		List<T> result = this.list(null);
		pager.setList(result);
		pageRow = null;
		return pager;
	}
	
	public <T extends Model> T findById(Serializable pk) {
		int index = params.size() + 1;
		this.params.put(new ParamKey(index, this.pkName()), pk);
		return this.findOne();
	}
	
	public <T extends Model> T findOne() {
		String sql = dialect.getQueryOneSql(this.sql, this);
		LOGGER.debug("Preparing\t=> {}", sql);
		Query query = sql2o.open().createQuery(sql);
		
		if (!this.params.isEmpty()) {
			Object[] paramValues = this.params.values().toArray();
			query.withParams(paramValues);
			LOGGER.debug("Parameters\t=> {}", Arrays.toString(paramValues));
		}
		
		List<T> models = (List<T>) query.executeAndFetchTable().asModel(clazz);
		
		this.clear();
		
		if(null != models && !models.isEmpty()){
			return models.get(0);
		}
		return null;
	}
	
	public int count(){
		return this.count(true);
	}
	
	public int count(boolean clear){
		String sql = dialect.getQueryCountSql(this.sql, this);
		LOGGER.debug("Preparing\t=> {}", sql);
		Query query = sql2o.open().createQuery(sql);
		
		if (!this.params.isEmpty()) {
			Object[] paramValues = this.params.values().toArray();
			query.withParams(paramValues);
			LOGGER.debug("Parameters\t=> {}", Arrays.toString(paramValues));
		}
		int count = query.executeScalar(Integer.class);
		if(clear){
			this.clear();
		}
		return count;
	}
	
	private void close(Connection connection){
		if(null != connection){
			connection.close();
		}
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
	
	public PageRow getPageRow() {
		return pageRow;
	}
	
	public String getOrder(){
		return this.order;
	}
	
	@Override
	public void clear() {
		super.clear();
		this.params.clear();
	}
	
}
