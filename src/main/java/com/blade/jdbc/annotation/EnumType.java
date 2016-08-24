package com.blade.jdbc.annotation;
/**
 * Defines mapping for the enumerated types.
 * The constants of this enumerated type specify how persistent 
 * property or field should be persisted as a enumerated type.
 *
 * @since Java Persistence 1.0
 */
public enum EnumType {
    /** Persist enumerated type property or field as an integer */
    ORDINAL,

    /** Persist enumerated type property or field as a string */
    STRING
}
