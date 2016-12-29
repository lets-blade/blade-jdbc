package com.blade.jdbc.annotation;

/** 
 * Defines the types of primary key generation. 
 *
 * @since Java Persistence 1.0
 */
public enum GenerationType { 

    /**
     * Indicates that the core provider must assign
     * primary keys for the entity using an underlying 
     * database table to ensure uniqueness.
     */
    TABLE, 

    /**
     * Indicates that the core provider must assign
     * primary keys for the entity using database sequence column.
     */
    SEQUENCE, 

    /**
     * Indicates that the core provider must assign
     * primary keys for the entity using database identity column.
     */
    IDENTITY, 

    /**
     * Indicates that the core provider should pick an
     * appropriate strategy for the particular database. The 
     * <code>AUTO</code> generation strategy may expect a database 
     * resource to exist, or it may attempt to create one. A vendor 
     * may provide documentation on how to create such resources 
     * in the event that it does not support schema generation 
     * or cannot create the schema resource at runtime.
     */
    AUTO
}
