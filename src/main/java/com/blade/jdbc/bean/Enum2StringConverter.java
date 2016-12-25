package com.blade.jdbc.bean;

import com.blade.jdbc.enums.IEnum;

/**
 * enum to string
 */
public class Enum2StringConverter implements TypeConverter {

    @Override
    public Object convert(Class<?> sourceClass, Class<?> targetClass, Object value) {

        if (value == null) {
            return null;
        }
        if (targetClass.equals(String.class) && IEnum.class.isAssignableFrom(sourceClass)) {
            return ((IEnum) value).getCode();
        }
        return value;
    }
}
