package com.blade.jdbc.dialect;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blade.jdbc.annotation.Column;
import com.blade.jdbc.annotation.EnumType;
import com.blade.jdbc.annotation.Enumerated;
import com.blade.jdbc.annotation.GeneratedValue;
import com.blade.jdbc.annotation.Id;
import com.blade.jdbc.annotation.Table;
import com.blade.jdbc.annotation.Transient;
import com.blade.jdbc.exception.DBException;
import com.blade.jdbc.serialize.DbSerializer;

/**
 * Provides means of reading and writing properties in a row.
 */
public class ModelMeta implements ModelInfo {

	private static final Logger LOGGER = LoggerFactory.getLogger(ModelMeta.class);
	
	/*
	 * annotations recognized: @ Id, @ GeneratedValue @ Transient @ Table @
	 * Column @ DbSerializer @ Enumerated
	 */
	LinkedHashMap<String, Property> propertyMap = new LinkedHashMap<String, Property>();
	String table;
	String primaryKeyName;
	String generatedColumnName;

	String insertSql;
	int insertSqlArgCount;
	String[] insertColumnNames;

	String upsertSql;
	int upsertSqlArgCount;
	String[] upsertColumnNames;
	
	String updateSql;
	String[] updateColumnNames;
	int updateSqlArgCount;

	String selectColumns;
	
	private boolean cached = true;

	public ModelMeta(Class<?> clazz) {
		try {
			if (Map.class.isAssignableFrom(clazz)) {
				// leave properties empty
			} else {
				populateProperties(clazz);
			}
			Table annot = (Table) clazz.getAnnotation(Table.class);
			if(null == annot){
				annot = (Table) clazz.getSuperclass().getAnnotation(Table.class);
			}
			if (annot != null) {
				table = annot.name();
				cached = annot.cached();
			} else {
				table = clazz.getSimpleName();
			}
		} catch (Throwable t) {
			throw new DBException(t);
		}
	}
	
	private void populateProperties(Class<?> clazz)
			throws IntrospectionException, InstantiationException, IllegalAccessException {
		
		List<Field> fields = new ArrayList<Field>();
		fields.addAll(Arrays.asList(clazz.getFields()));
		if(null != clazz.getSuperclass() && clazz.getSuperclass() != Object.class){
			fields.addAll(Arrays.asList(clazz.getSuperclass().getFields()));
		}
		
		for (Field field : fields) {
			int modifiers = field.getModifiers();
			if (Modifier.isStatic(modifiers) || Modifier.isFinal(modifiers)) {
				continue;
			}
			if (field.getAnnotation(Transient.class) != null) {
				continue;
			}
			
			if (Modifier.isPrivate(modifiers) || Modifier.isPublic(modifiers)) {
				Property prop = new Property();
				prop.name = field.getName();
				prop.field = field;
				prop.dataType = field.getType();
				applyAnnotations(prop, field);
				propertyMap.put(prop.name, prop);
			}
		}

		BeanInfo beanInfo = Introspector.getBeanInfo(clazz, Object.class);
		PropertyDescriptor[] descriptors = beanInfo.getPropertyDescriptors();
		for (PropertyDescriptor descriptor : descriptors) {

			Method readMethod = descriptor.getReadMethod();
			if (readMethod == null) {
				continue;
			}
			if (readMethod.getAnnotation(Transient.class) != null) {
				continue;
			}

			Property prop = new Property();
			prop.name = descriptor.getName();
			prop.readMethod = readMethod;
			prop.writeMethod = descriptor.getWriteMethod();
			prop.dataType = descriptor.getPropertyType();

			applyAnnotations(prop, prop.readMethod);

			propertyMap.put(prop.name, prop);
		}
	}

	/**
	 * Apply the annotations on the field or getter method to the property.
	 * 
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	@SuppressWarnings("unchecked")
	private void applyAnnotations(Property prop, AnnotatedElement ae)
			throws InstantiationException, IllegalAccessException {

		Column col = ae.getAnnotation(Column.class);
		if (col != null) {
			String name = col.name().trim();
			if (name.length() > 0) {
				prop.name = name;
			}
			prop.columnAnnotation = col;
		}

		if (ae.getAnnotation(Id.class) != null) {
			prop.isPrimaryKey = true;
			primaryKeyName = prop.name;
		}

		if (ae.getAnnotation(GeneratedValue.class) != null) {
			generatedColumnName = prop.name;
			prop.isGenerated = true;
		}

		if (prop.dataType.isEnum()) {
			prop.isEnumField = true;
			prop.enumClass = (Class<Enum<?>>) prop.dataType;
			/*
			 * We default to STRING enum type. Can be overriden with @Enumerated
			 * annotation
			 */
			prop.enumType = EnumType.STRING;
			if (ae.getAnnotation(Enumerated.class) != null) {
				prop.enumType = ae.getAnnotation(Enumerated.class).value();
			}
		}

		DbSerializer sc = ae.getAnnotation(DbSerializer.class);
		if (sc != null) {
			prop.serializer = sc.value().newInstance();
		}

	}

	/*
	 * private Method getMethod(Method meth, String propertyName, Property pair)
	 * { if (meth == null) { return null; } if
	 * (meth.getAnnotation(Transient.class) != null) { return null; } if
	 * (meth.getAnnotation(Id.class) != null) { this.primaryKeyName =
	 * propertyName; pair.isPrimaryKey = true; } if
	 * (meth.getAnnotation(GeneratedValue.class) != null) {
	 * this.generatedColumnName = propertyName; pair.isGenerated = true; }
	 * return meth; }
	 */
	public Object getValue(Object row, String name) {

		try {

			Property prop = propertyMap.get(name);
			if (prop == null) {
				throw new DBException("No such field: " + name);
			}

			Object value = null;

			if (prop.readMethod != null) {
				value = prop.readMethod.invoke(row);

			} else if (prop.field != null) {
				value = prop.field.get(row);
			}

			if (value != null) {
				if (prop.serializer != null) {
					value = prop.serializer.serialize(value);

				} else if (prop.isEnumField) {
					// handle enums according to selected enum type
					if (prop.enumType == EnumType.ORDINAL) {
						value = ((Enum<?>) value).ordinal();
					}
					// EnumType.STRING and others (if present in the future)
					else {
						value = value.toString();
					}
				}
			}

			return value;

		} catch (Throwable t) {
			throw new DBException(t);
		}
	}

	public void putValue(Object row, String name, Object value) {
		Property prop = propertyMap.get(name);
		if (prop != null) {
			if (value != null) {
				if (prop.serializer != null) {
					value = prop.serializer.deserialize((String) value, prop.dataType);
				} else if (prop.isEnumField) {
					value = getEnumConst(prop.enumClass, prop.enumType, value);
				}
			}
			if (prop.writeMethod != null) {
				try {
					prop.writeMethod.invoke(row, value);
				} catch (IllegalAccessException e) {
					throw new DBException("Could not write value into row. Property: " + prop.name + " method: "
							+ prop.writeMethod.toString() + " value: " + value, e);
				} catch (IllegalArgumentException e) {
					throw new DBException("Could not write value into row. Property: " + prop.name + " method: "
							+ prop.writeMethod.toString() + " value: " + value, e);
				} catch (InvocationTargetException e) {
					throw new DBException("Could not write value into row. Property: " + prop.name + " method: "
							+ prop.writeMethod.toString() + " value: " + value, e);
				}
				return;
			}

			if (prop.field != null) {
				try {
					prop.field.set(row, value);
				} catch (IllegalArgumentException e) {
					throw new DBException(
							"Could not set value into row. Field: " + prop.field.toString() + " value: " + value, e);
				} catch (IllegalAccessException e) {
					throw new DBException(
							"Could not set value into row. Field: " + prop.field.toString() + " value: " + value, e);
				}
				return;
			}
		}
	}

	/**
	 * Convert a string to an enum const of the appropriate class.
	 */
	private <T extends Enum<T>> Object getEnumConst(Class<Enum<?>> enumClass, EnumType type, Object value) {
		String str = value.toString();
		if (type == EnumType.ORDINAL) {
			Integer ordinalValue = (Integer) value;
			if (ordinalValue < 0 || ordinalValue >= enumClass.getEnumConstants().length) {
				throw new DBException(
						"Invalid ordinal number " + ordinalValue + " for enum class " + enumClass.getCanonicalName());
			}
			return enumClass.getEnumConstants()[ordinalValue];
		} else {
			for (Enum<?> e : enumClass.getEnumConstants()) {
				if (str.equals(e.toString())) {
					return e;
				}
			}
			throw new DBException("Enum value does not exist. value:" + str);
		}
	}

	@Override
	public Property getGeneratedColumnProperty() {
		return propertyMap.get(generatedColumnName);
	}

	public LinkedHashMap<String, Property> getPropertyMap() {
		return propertyMap;
	}

	public String getTable() {
		return table;
	}

	public String getPrimaryKeyName() {
		return primaryKeyName;
	}

	public String getGeneratedColumnName() {
		return generatedColumnName;
	}

	public String getSelectColumns() {
		return selectColumns;
	}

	@Override
	public boolean isCached() {
		return this.cached;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T getPK(Object row) {
		Object pk = this.getValue(row, primaryKeyName);
		return null != pk ? (T)pk : null;
	}

}
