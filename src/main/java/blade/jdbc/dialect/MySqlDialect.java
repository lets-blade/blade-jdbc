package blade.jdbc.dialect;

import blade.jdbc.Query;

public class MySqlDialect extends DefaultDialect {

	@Override
	public String getUpsertSql(Query query, Object row) {
		ModelMeta modelMeta = getModelInfo(row.getClass());
		return modelMeta.upsertSql;
	}

	@Override
	public Object[] getUpsertArgs(Query query, Object row) {

		// same args as insert, but we need to duplicate the values
		Object[] args = super.getInsertArgs(query, row);

		int count = args.length;
		
		Object[] upsertArgs = new Object[count * 2];
		System.arraycopy(args, 0, upsertArgs, 0, count);
		System.arraycopy(args, 0, upsertArgs, count, count);

		return upsertArgs;
	}

	@Override
	public void makeUpsertSql(ModelMeta modelMeta) {
		
		// INSERT INTO table (a,b,c) VALUES (1,2,3) ON DUPLICATE KEY UPDATE
		// c=c+1;

		// mostly the same as the makeInsertSql code
		// it uses the same column names and argcount

		StringBuilder buf = new StringBuilder();
		buf.append(modelMeta.insertSql);
		buf.append(" on duplicate key update ");
		
		boolean first = true;
		for (String colName : modelMeta.insertColumnNames) {
			if (first) {
				first = false;
			} else {
				buf.append(',');
			}
			buf.append(colName);
			buf.append("=?");
		}

		modelMeta.upsertSql = buf.toString();
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
