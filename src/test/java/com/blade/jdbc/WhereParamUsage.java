package com.blade.jdbc;

import java.util.Arrays;

import com.blade.jdbc.QueryParam;

public class WhereParamUsage {

	public static void main(String[] args) {
		QueryParam w = QueryParam.me();
		w.gt("age", 18)
		.like("title", "zh%")
		.eq("name", "wang")
		.in("uid", 11,12,13)
		.orderby("id desc")
		.page(1, 20);
		
		System.out.println(w.asSql());
		System.out.println(Arrays.toString(w.args()));
	}
}
