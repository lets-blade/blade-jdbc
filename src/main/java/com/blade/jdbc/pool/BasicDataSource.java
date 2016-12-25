package com.blade.jdbc.pool;

public interface BasicDataSource {
	
	String getUrl();
	
	int activeCount();
	
	int getIdleConnections();
	
	int getMaxConnections();

	long getBorrowTimeout();
	
	long getLockTimeout();
	
}