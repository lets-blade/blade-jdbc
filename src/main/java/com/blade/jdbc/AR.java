package com.blade.jdbc;

import java.io.Serializable;

public final class AR {

	private AR() {
	}
	
	public static <T> ARC<T> find(Class<T> type) {
		ARC<T> arc = new ARC<T>(type, OptType.QUERY, null);
		return arc.search();
	}

	public static <T> ARC<T> find(String sql, Class<T> type) {
		ARC<T> arc = find(type);
		return arc.search(sql);
	}

	public static <T> ARC<T> delete(Class<T> type) {
		ARC<T> arc = new ARC<T>(type, OptType.DELETE, null);
		return arc;
	}

	public static <T> ARC<T> insert(Class<T> type) {
		ARC<T> arc = new ARC<T>(type, OptType.INSERT, null);
		return arc;
	}
	
	@SuppressWarnings("unchecked")
	public static <T> ARC<T> insertEntity(T pojo) {
		Class<T> type = (Class<T>) pojo.getClass();
		ARC<T> arc = new ARC<T>(type, OptType.INSERT, null);
		return arc.bind(pojo);
	}
	
	public static <T> ARC<T> update(Class<T> type) {
		ARC<T> arc = new ARC<T>(type, OptType.UPDATE, null);
		return arc;
	}

	public static <T> ARC<T> updateById(Class<T> type, Serializable pk) {
		ARC<T> arc = new ARC<T>(type, OptType.UPDATE, pk);
		return arc;
	}

	public static <T> T findById(Class<T> type, Serializable pk) {
		ARC<T> arc = new ARC<T>(type, OptType.QUERY, null);
		return arc.findByPk(pk);
	}

	public static Object[] in(Object... args) {
		return args;
	}
	
}