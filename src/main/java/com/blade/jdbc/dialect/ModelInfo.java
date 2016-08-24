package com.blade.jdbc.dialect;

public interface ModelInfo {
	
	Object getValue(Object row, String name);
	
	void putValue(Object row, String name, Object value);

	Property getGeneratedColumnProperty();
	
	String getTable();
	
	String getPrimaryKeyName();
	
	boolean isCached();
	
	<T> T getPK(Object row);
}
