package com.blade.jdbc.kit;

import com.blade.jdbc.ParamKey;
import com.blade.jdbc.QueryOpts;

public final class QueryKit {
	
	public static String[] getOpts(String key){
		String[] strings = new String[2];
		if(key.indexOf(QueryOpts.GE) != -1){
			int pos = key.indexOf(QueryOpts.GE);
			strings[0] = key.substring(0, pos).trim();
			strings[1] = QueryOpts.GE;
			return strings;
		}
		if(key.indexOf(QueryOpts.LE) != -1){
			int pos = key.indexOf(QueryOpts.LE);
			strings[0] = key.substring(0, pos).trim();
			strings[1] = QueryOpts.LE;
			return strings;
		}
		if(key.indexOf(QueryOpts.EQ) != -1){
			int pos = key.indexOf(QueryOpts.EQ);
			strings[0] = key.substring(0, pos).trim();
			strings[1] = QueryOpts.EQ;
			return strings;
		}
		if(key.indexOf(QueryOpts.NEQ) != -1){
			int pos = key.indexOf(QueryOpts.NEQ);
			strings[0] = key.substring(0, pos).trim();
			strings[1] = QueryOpts.NEQ;
			return strings;
		}
		if(key.indexOf(QueryOpts.GT) != -1){
			int pos = key.indexOf(QueryOpts.GT);
			strings[0] = key.substring(0, pos).trim();
			strings[1] = QueryOpts.GT;
			return strings;
		}
		if(key.indexOf(QueryOpts.LT) != -1){
			int pos = key.indexOf(QueryOpts.LT);
			strings[0] = key.substring(0, pos).trim();
			strings[1] = QueryOpts.LT;
			return strings;
		}
		if(key.indexOf(QueryOpts.IN) != -1){
			int pos = key.indexOf(QueryOpts.IN);
			strings[0] = key.substring(0, pos).trim();
			strings[1] = QueryOpts.IN;
			return strings;
		}
		if(key.indexOf(QueryOpts.NOTIN) != -1){
			int pos = key.indexOf(QueryOpts.NOTIN);
			strings[0] = key.substring(0, pos).trim();
			strings[1] = QueryOpts.NOTIN;
			return strings;
		}
		if(key.indexOf(QueryOpts.LIKE) != -1){
			int pos = key.indexOf(QueryOpts.LIKE);
			strings[0] = key.substring(0, pos).trim();
			strings[1] = QueryOpts.LIKE;
			return strings;
		}
		if(key.indexOf(QueryOpts.BETWEEN) != -1){
			int pos = key.indexOf(QueryOpts.BETWEEN);
			strings[0] = key.substring(0, pos).trim();
			strings[1] = QueryOpts.BETWEEN;
			return strings;
		}
		strings[0] = key.trim();
		strings[1] = "";
		return strings;
	}

	/**
	 * name like ? and id > ? order by ?
	 * 
	 * {
	 * 	name =
	 *  age >
	 * }
	 * 
	 * @param wheres
	 * @return
	 */
	public static ParamKey[] getParams(int index, String wheres) {
		String[] qs = wheres.replaceAll("and", "").replaceAll(",", "").split("\\?");
		ParamKey[] paramKeys = new ParamKey[qs.length];
		for(int i=0, len = qs.length; i<len; i++){
			String[] opts = getOpts(qs[i]);
			paramKeys[i] = new ParamKey(index, opts[0], opts[1]);
			index++;
		}
		return paramKeys;
	}
	
}
