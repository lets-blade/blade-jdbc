package com.blade.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.blade.jdbc.annotation.Table;

import blade.kit.Assert;

public class ARKit {

	static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
        return cs.toString().indexOf(searchChar.toString(), start);
    }
	
	public static String getTable(String sql){
		Pattern pattern = Pattern.compile("from (\\S+) \\S+");
		Matcher matcher = pattern.matcher(sql);
		if(matcher.find()){
			return matcher.group(1);
		}
		pattern = Pattern.compile("update (\\S+) \\S+");
		matcher = pattern.matcher(sql);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
	public static int countMatches(final CharSequence str, final CharSequence sub) {
        if (null == str || null == sub) {
            return 0;
        }
        int count = 0;
        int idx = 0;
        while ((idx = indexOf(str, sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
	
	//select * from aaa 
	public static boolean hasFrom(String sql) {
		Pattern pattern = Pattern.compile("(select * from \\S+)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql);
		if(matcher.find()){
			return true;
		}
		return false;
    }
	
	public static String[] getField(String str) {
		String[] arr = new String[2];
		if(null != str){
			Pattern pattern = Pattern.compile("(\\S+) (=|like|>|>=|<|<=|in|between|not in) \\?", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(str);
			if(matcher.find()){
				arr[0] = matcher.group(1);
				arr[1] = matcher.group(2);
				return arr;
			}
		}
        return arr;
    }
	
	public static <T> String tableName(Class<T> type){
		Table table = type.getAnnotation(Table.class);
		Assert.notNull(table, "The POJO @Table is null.");
		return table.value();
	}
	
	public static <T> String pkName(Class<T> type){
		Table table = type.getAnnotation(Table.class);
		Assert.notNull(table, "The POJO @Table is null.");
		return table.PK();
	}
	
	public static List<String> getFields(Class<?> type) {
		if(null != type){
			List<String> fieldList = new ArrayList<String>();
			Field[] fields = type.getDeclaredFields();
			for(Field field : fields){
				if(Modifier.isPrivate(field.getModifiers()) && !field.getName().equals("serialVersionUID")){
					fieldList.add(field.getName());
				}
			}
			return fieldList;
		}
        return null;
    }
	
	public static <T> List<String> getFields(Object t) {
		try {
			if(null != t){
				Class<?> type = t.getClass();
				List<String> fieldList = new ArrayList<String>();
				Field[] fields = type.getDeclaredFields();
				for(Field field : fields){
					field.setAccessible(true);
					if( Modifier.isPrivate(field.getModifiers()) && 
							!field.getName().equals("serialVersionUID") && 
							null != field.get(t)){
						fieldList.add(field.getName());
					}
				}
				return fieldList;
			}
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
        return null;
    }
	
	public static void main(String[] args) {
		System.out.println(getTable("select count(1) from u_ua where b = 2"));
	}
}
