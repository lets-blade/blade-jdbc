package com.blade.jdbc;

import java.io.Serializable;

public class PageRow implements Serializable {

	private static final long serialVersionUID = -1221241722858484846L;
	private int offset;
	private int limit;
	
	public PageRow(int offset, int limit) {
		this.offset = offset;
		this.limit = limit;
	}

	public int getOffset() {
		return offset;
	}

	public int getLimit() {
		return limit;
	}
	
}
