package blade.jdbc.dialect;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import blade.jdbc.annotation.Column;
import blade.jdbc.annotation.EnumType;
import blade.jdbc.serialize.DbSerializable;

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
