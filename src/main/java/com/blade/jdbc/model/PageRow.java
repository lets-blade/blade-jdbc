package com.blade.jdbc.model;

import java.io.Serializable;

public class PageRow implements Serializable {

	private static final long serialVersionUID = -1221241722858484846L;
	private int page = 1;
	private int limit = 20;
	private String orderBy;
	
	public PageRow(int page, int limit) {
		this(page, limit, null);
	}

	public PageRow(int page, int limit, String orderBy) {
		if(page < 0){
			page = 1;
		}
		if(limit < 0){
			limit = 10;
		}
		this.page = page;
		this.limit = limit;
		this.orderBy = orderBy;
	}

	public PageRow setOrderBy(String orderBy) {
		this.orderBy = orderBy;
		return this;
	}

	public int getPage() {
		return page;
	}

	public int getOffSet(){
		return (page - 1) * limit;
	}

	public int getLimit() {
		return limit;
	}

	public String getOrderBy() {
		return orderBy;
	}
}
