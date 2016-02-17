package com.blade.jdbc;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ARKit {

	static int indexOf(final CharSequence cs, final CharSequence searchChar, final int start) {
        return cs.toString().indexOf(searchChar.toString(), start);
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
	
	public static List<String> split(String str) {
		Pattern pattern = Pattern.compile("(\\S+ (=|like|>|>=|<|<=|in|between|not in) \\?)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(str);
		List<String> list = new ArrayList<String>();
		while(matcher.find()){
			list.add(matcher.group());
		}
        return list;
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
		System.out.println(split("name like ? and age > ? and birthday <= ? and uid IN ? order by id desc"));
		System.out.println(Arrays.toString(getField("birthday <= ?")));
		System.out.println(Arrays.toString(getField("uid IN ?")));
	}
}
