package com.blade.jdbc.dialect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.blade.jdbc.annotation.Column;
import com.blade.jdbc.annotation.EnumType;
import com.blade.jdbc.serialize.DbSerializable;

public class Property {
	public String name;
	public Method readMethod;
	public Method writeMethod;
	public Field field;
	public Class<?> dataType;
	public boolean isGenerated;
	public boolean isPrimaryKey;
	public boolean isEnumField;
	public Class<Enum<?>> enumClass;
	public EnumType enumType;
	public Column columnAnnotation;
	public DbSerializable serializer;
}
