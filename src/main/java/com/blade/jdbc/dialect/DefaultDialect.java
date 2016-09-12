package com.blade.jdbc.dialect;

import java.util.Map;
import java.util.Set;

import com.blade.jdbc.Model;
import com.blade.jdbc.PageRow;
import com.blade.jdbc.ParamKey;

public class DefaultDialect implements Dialect {

	/**
	 * insert into person (id, name, age) values(?, ?, ?)
	 */
	@Override
	public String getSaveSql(Model model) {
		StringBuffer sql = new StringBuffer();
		sql.append("insert into ").append(model.table()).append('(');

		Set<String> columns = model.keySet();
		int pos = 0, len = columns.size();
		for (String column : columns) {
			if (pos != (len - 1)) {
				sql.append(column + ',');
			} else {
				sql.append(column);
			}
			pos++;
		}
		sql.append(") values(");
		for (int i = 1; i <= len; i++) {
			if (i < len) {
				sql.append(":p" + i + ", ");
			} else {
				sql.append(":p" + i);
			}
		}
		sql.append(')');
		return sql.toString();
	}

	/**
	 * update person set a=1, b=2 where id = 1
	 */
	@Override
	public String getUpdateSql(Model model) {

		StringBuffer sql = new StringBuffer();
		sql.append("update ").append(model.table()).append(" set ");
		
		Set<String> columns = model.keySet();
		
		int pos = 1, len = columns.size();
		for (String column : columns) {
			if (pos < len) {
				sql.append(column + " = :p" + pos + ",");
			} else {
				sql.append(column + " = :p" + pos + ' ');
			}
			pos++;
		}
		sql.append(this.whereSql(pos, model));
		return sql.toString();
	}
	
	@Override
	public String getDeleteSql(Model model) {
		StringBuffer sql = new StringBuffer();
		sql.append("delete from ").append(model.table()).append(' ');
		sql.append(this.whereSql(1, model));
		return sql.toString();
	}

	@Override
	public String getQuerySql(String sql, Model model) {
		String realSql = this.querySql(sql, model);
		String order = model.getOrder();
		if(null != order){
			realSql += "order by " + order;
		}
		PageRow pageRow = model.getPageRow();
		if(null != pageRow){
			int index = model.params().size() + 1;
			model.params().put(new ParamKey(index, "offset"), pageRow.getOffset());
			model.params().put(new ParamKey(index + 1, "limit"), pageRow.getLimit());
			realSql += " limit :p" + index + ", :p" + (index + 1);
		}
		return realSql;
	}
	
	private String querySql(String sql, Model model){
		StringBuffer sqlBuf = new StringBuffer();
		if(null != sql){
			sqlBuf.append(sql).append(' ');
		} else {
			sqlBuf.append("select * from ").append(model.table()).append(' ');
		}
		sqlBuf.append(this.whereSql(1, model));
		return sqlBuf.toString();
	}
	
	private String whereSql(int index, Model model){
		StringBuffer sql = new StringBuffer();
		Map<ParamKey, Object> where = model.params();
		if (null != where && !where.isEmpty()) {
			sql.append("where ");
			Set<ParamKey> whereKeys = where.keySet();
			int len = whereKeys.size();
			for (ParamKey paramKey : whereKeys) {
				String apped = paramKey.getColumn() + " " + paramKey.getOpt().trim() + " :p" + index;
				if (index > 1 && !"".equals(paramKey.getOpt()) && len > 1) {
					sql.append(" and " + apped).append(' ');
				} else {
					sql.append(apped).append(' ');
				}
				index++;
			}
		}
		return sql.toString();
	}

	@Override
	public String getQueryOneSql(String sql, Model model) {
		String realSql = this.querySql(sql, model);
		realSql += " limit 1";
		return realSql.toString();
	}
	
	@Override
	public String getQueryCountSql(String sql, Model model) {
		StringBuffer sqlBuf = new StringBuffer();
		if(null != sql){
			sqlBuf.append("select count(1) from");
	        int pos = sql.indexOf("from") + 4;
	        int w = sql.indexOf("where");
	        int o = sql.indexOf("order by");
	        
	        if(w != -1){
	            sqlBuf.append(sql.substring(pos, w));   
	        } else if(o != -1){
	            sqlBuf.append(sql.substring(pos, 0));   
	        } else{
	            sqlBuf.append(sql.substring(pos));  
	        }
		} else {
			sqlBuf.append("select count(")
			.append(model.pkName())
			.append(") from ")
			.append(model.table());
		}
		sqlBuf.append(' ').append(this.whereSql(1, model));
		return sqlBuf.toString();
	}
	
}
