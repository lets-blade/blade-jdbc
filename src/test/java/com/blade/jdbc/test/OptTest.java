package com.blade.jdbc.test;

import com.blade.jdbc.QueryOpts;

public class OptTest {
	
	public static void main(String[] args) {
		String[] tests = {"name=?", "name = ?", "name >?", "name<= ?"};
		
		for(String key : tests){
			if(key.indexOf(QueryOpts.GE) != -1){
				System.out.println(QueryOpts.GE);
			}
			if(key.indexOf(QueryOpts.LE) != -1){
				System.out.println(QueryOpts.LE);
			}
			if(key.indexOf(QueryOpts.EQ) != -1){
				System.out.println(QueryOpts.EQ);
			}
			if(key.indexOf(QueryOpts.NEQ) != -1){
				System.out.println(QueryOpts.NEQ);
			}
			if(key.indexOf(QueryOpts.LIKE) != -1){
				System.out.println(QueryOpts.LIKE);
			}
		}
	}
}
