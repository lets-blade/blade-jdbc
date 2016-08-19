package blade.jdbc.dialect;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import blade.jdbc.Query;
import blade.jdbc.Util;
import blade.jdbc.annotation.Column;
import blade.jdbc.exception.DBException;

/**
 * Produces ANSI-standard SQL. Extend this class to handle different flavors of sql.
 */
public class DefaultDialect implements Dialect {
	
	private static ConcurrentHashMap<Class<?>, ModelMeta> map = new ConcurrentHashMap<Class<?>, ModelMeta>();
	
	public ModelMeta getModelInfo(Class<?> rowClass) {
		ModelMeta pi = map.get(rowClass);
		if (pi == null) {
			pi = new ModelMeta(rowClass);
			map.put(rowClass, pi);
			
			makeInsertSql(pi);
			makeUpsertSql(pi);
			makeUpdateSql(pi, null);
			makeSelectColumns(pi);
		}
		return pi;
	}
	
	public ModelMeta getModelInfo(Class<?> rowClass, Object row) {
		ModelMeta pi = map.get(rowClass);
		if (pi == null) {
			pi = new ModelMeta(rowClass);
			map.put(rowClass, pi);
			
			makeInsertSql(pi);
			makeUpdateSql(pi, row);
			makeSelectColumns(pi);
		}
		return pi;
	}
	
	@Override
	public String getInsertSql(Query query, Object row) {
		ModelMeta modelMeta = getModelInfo(row.getClass());
		return modelMeta.insertSql;
	}
	
	@Override
	public Object[] getInsertArgs(Query query, Object row) {
		ModelMeta modelMeta = getModelInfo(row.getClass());
		Object [] args = new Object[modelMeta.insertSqlArgCount];
		for (int i = 0; i < modelMeta.insertSqlArgCount; i++) {
			args[i] = modelMeta.getValue(row, modelMeta.insertColumnNames[i]);
		}
		return args;
	}
	
	@Override
	public String getUpdateSql(Query query, Object row) {
		ModelMeta modelMeta = getModelInfo(row.getClass());
		if (modelMeta.primaryKeyName == null) {
			throw new DBException("No primary key specified in the row. Use the @Id annotation.");
		}
		return modelMeta.updateSql;
	}
	
	@Override
	public String getUpdateByPKSql(Query query, Object row) {
		ModelMeta modelMeta = getModelInfo(row.getClass(), row);
		if (modelMeta.primaryKeyName == null) {
			throw new DBException("No primary key specified in the row. Use the @Id annotation.");
		}
		return modelMeta.updateSql;
	}

	@Override
	public Object[] getUpdateArgs(Query query, Object row) {
		ModelMeta modelMeta = getModelInfo(row.getClass());
		
		Object [] args = new Object[modelMeta.updateSqlArgCount];
		for (int i = 0; i < modelMeta.updateSqlArgCount - 1; i++) {
			args[i] = modelMeta.getValue(row, modelMeta.updateColumnNames[i]);
		}
		// add the value for the where clause to the end
		Object pk = modelMeta.getValue(row, modelMeta.primaryKeyName);
		args[modelMeta.updateSqlArgCount - 1] = pk;
		return args;
	}
	
	public void makeUpsertSql(ModelMeta modelMeta){
	}
	
	public void makeUpdateSql(ModelMeta modelMeta, Object row) {
		ArrayList<String> cols = new ArrayList<String>();
		for (Property prop: modelMeta.propertyMap.values()) {
			if (prop.isPrimaryKey || prop.isGenerated) {
				continue;
			}
			if(null != row){
				Object value = modelMeta.getValue(row, prop.name);
				if(null != value){
					cols.add(prop.name);
				}
			} else{
				cols.add(prop.name);
			}
		}
		
		modelMeta.updateColumnNames = cols.toArray(new String [cols.size()]);
		modelMeta.updateSqlArgCount = modelMeta.updateColumnNames.length + 1; // + 1 for the where arg
		
		StringBuilder buf = new StringBuilder();
		buf.append("update ");
		buf.append(modelMeta.table);
		buf.append(" set ");

		for (int i = 0; i < cols.size(); i++) {
			if (i > 0) {
				buf.append(',');
			}
			buf.append(cols.get(i) + " = ?");
		}
		
		buf.append(" where " + modelMeta.primaryKeyName + " = ?");
		
		modelMeta.updateSql = buf.toString();
	}
	
	public void makeInsertSql(ModelMeta modelMeta) {
		ArrayList<String> cols = new ArrayList<String>();
		
		Collection<Property> properties = modelMeta.propertyMap.values();
		
		for (Property prop: properties) {
			if (prop.isGenerated) {
				continue;
			}
			cols.add(prop.name);
		}
		modelMeta.insertColumnNames = cols.toArray(new String [cols.size()]);
		modelMeta.insertSqlArgCount = modelMeta.insertColumnNames.length;
		
		StringBuilder buf = new StringBuilder();
		buf.append("insert into ");
		buf.append(modelMeta.table);
		buf.append(" (");
		buf.append(Util.join(modelMeta.insertColumnNames)); // comma sep list?
		buf.append(") values (");
		buf.append(Util.getQuestionMarks(modelMeta.insertSqlArgCount));
		buf.append(")");
		
		modelMeta.insertSql = buf.toString();
	}
	
	private void makeSelectColumns(ModelMeta modelMeta) {
		if (modelMeta.propertyMap.isEmpty()) {
			// this applies if the rowClass is a Map
			modelMeta.selectColumns = "*";
		} else {
			ArrayList<String> cols = new ArrayList<String>();
			for (Property prop: modelMeta.propertyMap.values()) {
				cols.add(prop.name);
			}
			modelMeta.selectColumns = Util.join(cols);
		}
	}
	
	@Override
	public String getPKSql(Query query, Class<?> rowClass) {
		ModelMeta modelMeta = getModelInfo(rowClass);
		String table = query.getTable();
		if (table == null) {
			table = modelMeta.table;
		}
		StringBuilder out = new StringBuilder();
		out.append("select ");
		out.append(modelMeta.primaryKeyName);
		out.append(" from ");
		out.append(table);
		String whereAndOrderby = whereAndOrderBy(query);
		if(whereAndOrderby.length() > 0){
			out.append(whereAndOrderby);
		}
		return out.toString();
	}
	
	@Override
	public String getSelectSql(Query query, Class<?> rowClass) {

		// unlike insert and update, this needs to be done dynamically
		// and can't be precalculated because of the where and order by
		
		ModelMeta modelMeta = getModelInfo(rowClass);
		String columns = modelMeta.selectColumns;
		
		String table = query.getTable();
		if (table == null) {
			table = modelMeta.table;
		}
		
		StringBuilder out = new StringBuilder();
		if(Util.blank(query.sql())){
			out.append("select ");
			out.append(columns);
			out.append(" from ");
			out.append(table);
		} else{
			out.append(query.sql());
		}
		String whereAndOrderby = whereAndOrderBy(query);
		if(whereAndOrderby.length() > 0){
			out.append(whereAndOrderby);
		}
		return out.toString();
	}

	@Override
	public String getCountSql(Query query, Class<?> rowClass) {
		
		// unlike insert and update, this needs to be done dynamically
		// and can't be precalculated because of the where and order by
		ModelMeta modelMeta = getModelInfo(rowClass);
		String table = query.getTable();
		if (table == null) {
			table = modelMeta.table;
		}
		
		StringBuilder out = new StringBuilder();
		if(Util.blank(query.sql())){
			out.append("select count(");
			out.append(modelMeta.primaryKeyName);
			out.append(") from ");
			out.append(table);
		} else{
			out.append(query.sql());
		}
		String whereAndOrderby = whereAndOrderBy(query);
		if(whereAndOrderby.length() > 0){
			out.append(whereAndOrderby);
		}
		return out.toString();
	}
	
	private String whereAndOrderBy(Query query){
		String where = query.getWhere();
		String orderBy = query.getOrderBy();
		StringBuilder out = new StringBuilder();
		if (where != null && where.length() > 0) {
			out.append(" where ");
			if(where.startsWith(" and ")){
				out.append(where.replaceFirst(" and ", ""));
			} else{
				out.append(where);
			}
			
		}
		if (orderBy != null && orderBy.length() > 0) {
			out.append(" order by ");
			out.append(orderBy);
		}
		return out.toString();
	}

	@Override
	public String getCreateTableSql(Class<?> clazz) {
		
		StringBuilder buf = new StringBuilder();

		ModelMeta modelMeta = getModelInfo(clazz);
		buf.append("create table ");
		buf.append(modelMeta.table);
		buf.append(" (");
		
		boolean needsComma = false;
		for (Property prop : modelMeta.propertyMap.values()) {
			
			if (needsComma) {
				buf.append(',');
			}
			needsComma = true;

			Column columnAnnot = prop.columnAnnotation;
			if (columnAnnot == null) {
	
				buf.append(prop.name);
				buf.append(" ");
				buf.append(getColType(prop.dataType, 255, 10, 2));
				if (prop.isGenerated) {
					buf.append(" auto_increment");
				}
				
			} else {
				if (columnAnnot.columnDefinition() == null) {
					
					// let the column def override everything
					buf.append(columnAnnot.columnDefinition());
					
				} else {

					buf.append(prop.name);
					buf.append(" ");
					buf.append(getColType(prop.dataType, columnAnnot.length(), columnAnnot.precision(), columnAnnot.scale()));
					if (prop.isGenerated) {
						buf.append(" auto_increment");
					}
					
					if (columnAnnot.unique()) {
						buf.append(" unique");
					}
					
					if (!columnAnnot.nullable()) {
						buf.append(" not null");
					}
				}
			}
		}
		
		if (modelMeta.primaryKeyName != null) {
			buf.append(", primary key (");
			buf.append(modelMeta.primaryKeyName);
			buf.append(")");
		}
		
		buf.append(")");
		
		return buf.toString();
	}


	protected String getColType(Class<?> dataType, int length, int precision, int scale) {
		String colType;
		
		if (dataType.equals(Integer.class) || dataType.equals(int.class)) {
			colType = "integer";
			
		} else if (dataType.equals(Long.class) || dataType.equals(long.class)) {
			colType = "bigint";
			
		} else if (dataType.equals(Double.class) || dataType.equals(double.class)) {
			colType = "double";
			
		} else if (dataType.equals(Float.class) || dataType.equals(float.class)) {
			colType = "float";
			
		} else if (dataType.equals(BigDecimal.class)) {
			colType = "decimal(" + precision + "," + scale + ")";
			
		} else {
			colType = "varchar(" + length + ")";
		}
		return colType;
	}


	@Override
	public String getDeleteSql(Query query, Object row) {
		
		ModelMeta modelMeta = getModelInfo(row.getClass());
		
		String table = query.getTable();  
		if (table == null) {
			table = modelMeta.table;
			if (table == null) {
				throw new DBException("You must specify a table name");
			}
		}
		
		String primaryKeyName = modelMeta.primaryKeyName;
		
		return "delete from " + table + " where " + primaryKeyName + "=?";
	}


	@Override
	public Object[] getDeleteArgs(Query query, Object row) {
		ModelMeta modelMeta = getModelInfo(row.getClass());
		Object primaryKeyValue = modelMeta.getValue(row, modelMeta.primaryKeyName);
		Object [] args = new Object[1];
		args[0] = primaryKeyValue;
		return args;
	}


	@Override
	public String getUpsertSql(Query query, Object row) {
		String msg =
				"There's no standard upsert implemention. There is one in the MySql driver, though,"
				+ "so if you're using MySql, call Database.setSqlMaker(new MySqlMaker()); Or roll your own.";
		throw new UnsupportedOperationException(msg);
	}


	@Override
	public Object[] getUpsertArgs(Query query, Object row) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void populateGeneratedKey(ResultSet generatedKeys, Object insertRow) {
		ModelInfo modelMeta = getModelInfo(insertRow.getClass());
		
		try {
			Property prop = modelMeta.getGeneratedColumnProperty();
			boolean isInt = prop.dataType.isAssignableFrom(int.class) || prop.dataType.isAssignableFrom(Integer.class); // int or long
			
			Object newKey;
			
			// if there is just one column, it's the generated key
			// postgres returns multiple columns, though, so we have the fetch the value by name
			int colCount = generatedKeys.getMetaData().getColumnCount();
			if (colCount == 1) {
				if (isInt) {
					newKey = generatedKeys.getInt(1);
				} else {
					newKey = generatedKeys.getLong(1);
				}
			} else {
				// colcount > 1, must do by name
				if (isInt) {
					newKey = generatedKeys.getInt(prop.name);
				} else {
					newKey = generatedKeys.getLong(prop.name);
				}
			}
			modelMeta.putValue(insertRow, prop.name, newKey);
		} catch (Throwable t) {
			throw new DBException(t);
		}
	}
	
	@Override
	public String getPageSql(Query query, Class<?> rowClass) {
		
		ModelMeta modelMeta = getModelInfo(rowClass);
		String columns = modelMeta.selectColumns;
		
		String where = query.getWhere();
		String table = query.getTable();
		if (table == null) {
			table = modelMeta.table;
		}
		String orderBy = query.getOrderBy();
		
		StringBuilder out = new StringBuilder();
		out.append("select ");
		out.append(columns);
		out.append(" from ");
		out.append(table);
		if (where != null && where.length() > 0) {
			out.append(" where ");
			if(where.startsWith(" and ")){
				out.append(where.replaceFirst(" and ", ""));
			} else{
				out.append(where);
			}
			
		}
		if (orderBy != null && orderBy.length() > 0) {
			out.append(" order by ");
			out.append(orderBy);
		}
		out.append(" limit ?,?");
        return out.toString();
	}
	
}
