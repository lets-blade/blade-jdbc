package com.blade.jdbc;

public class ParamKey implements Comparable<ParamKey> {
	
	private int index;
	private String column;
	private String opt;
	
	public ParamKey(int index, String column) {
		this.index = index;
		this.column = column;
		this.opt = " = ";
	}
	
	public ParamKey(int index, String column, String opt) {
		this.index = index;
		this.column = column;
		this.opt = opt;
	}

	public String getColumn() {
		return column;
	}

	public String getOpt() {
		return opt;
	}

	@Override
	public int compareTo(ParamKey o) {
		if(this.index > o.index)
			return 1;
		if(this.index < o.index)
			return -1;
		return 0;
	}
	
}
