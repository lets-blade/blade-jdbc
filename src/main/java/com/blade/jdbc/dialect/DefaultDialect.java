package com.blade.jdbc.dialect;

import java.util.Map;
import java.util.Set;

import com.blade.jdbc.Model;
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
				sql.append(":p" + i + ",");
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
				sql.append(column + " = :p" + pos);
			}
			pos++;
		}
		sql.append(this.whereSql(pos + 1,model));
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
	public String getQuerySql(Model model) {
		String sql = this.querySql(model);
		String order = model.order();
		if(null != order){
			sql += " order by " + order;
		}
		return sql;
	}
	
	private String querySql(Model model){
		StringBuffer sql = new StringBuffer();
		sql.append("select * from ").append(model.table()).append(' ');
		sql.append(this.whereSql(1, model));
		return sql.toString();
	}
	
	private String whereSql(int index, Model model){
		StringBuffer sql = new StringBuffer();
		Map<ParamKey, Object> where = model.params();
		if (null != where && !where.isEmpty()) {
			sql.append("where ");
			Set<ParamKey> whereKeys = where.keySet();
			for (ParamKey paramKey : whereKeys) {
				String apped = paramKey.getColumn() + " " + paramKey.getOpt().trim() + " :p" + index;
				if (index > 1 && !"".equals(paramKey.getOpt())) {
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
	public String getQueryOneSql(Model model) {
		String sql = this.querySql(model);
		sql += " limit 1";
		return sql.toString();
	}
	
	@Override
	public String getQueryCountSql(Model model) {
		StringBuffer sql = new StringBuffer();
		sql.append("select count(")
		.append(model.pkName())
		.append(") from ")
		.append(model.table()).append(' ')
		.append(this.whereSql(1, model));
		return sql.toString();
	}
	
	@Override
	public String getQueryPageSql(Model model) {
		String sql = this.getQuerySql(model);
		Map<ParamKey, Object> where = model.params();
		int whereSize = where.size();
		sql += " limit :p" + (whereSize+1) + ", :p" + (whereSize+2);
		return sql;
	}

}
