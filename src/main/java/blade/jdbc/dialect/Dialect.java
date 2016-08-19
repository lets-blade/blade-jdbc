package blade.jdbc.dialect;

import java.sql.ResultSet;

import blade.jdbc.Query;

public interface Dialect {

	String getInsertSql(Query query, Object row);
	
	Object[] getInsertArgs(Query query, Object row);

	String getUpdateSql(Query query, Object row);
	
	String getUpdateByPKSql(Query query, Object row);

	Object[] getUpdateArgs(Query query, Object row);

	String getDeleteSql(Query query, Object row);

	Object[] getDeleteArgs(Query query, Object row);

	String getUpsertSql(Query query, Object row);

	Object[] getUpsertArgs(Query query, Object row);

	String getPKSql(Query query, Class<?> rowClass);

	String getSelectSql(Query query, Class<?> rowClass);
	
	String getCountSql(Query query, Class<?> rowClass);

	String getPageSql(Query query, Class<?> rowClass);
	
	String getCreateTableSql(Class<?> clazz);

	ModelInfo getModelInfo(Class<?> rowClass);
	
	void populateGeneratedKey(ResultSet generatedKeys, Object insertRow);

}
