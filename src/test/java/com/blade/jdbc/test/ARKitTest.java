package com.blade.jdbc.test;

import com.blade.jdbc.ARKit;

public class ARKitTest {

	public static void main(String[] args) {
		System.out.println(ARKit.getTable("select * from  user_t "));
		System.out.println(ARKit.getTable("update  user_t "));
		System.out.println(ARKit.getTable(" insert  into  user_t"));
	}
}
