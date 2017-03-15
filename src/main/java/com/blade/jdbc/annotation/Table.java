package com.blade.jdbc.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * This annotation specifies the primary table for the annotated 
 * entity. Additional tables may be specified using {@link SecondaryTable}
 * or {@link SecondaryTables} annotation. 
 *
 * <p> If no <code>Table</code> annotation is specified for an entity 
 * class, the default values apply.
 *
 * <pre>
 *    Example:
 *
 *    &#064;Entity
 *    &#064;Table(name="CUST", schema="RECORDS")
 *    public class Customer { ... }
 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Target(TYPE) 
@Retention(RUNTIME)
public @interface Table {

    /**
     * (Optional) The name of the table.
     * <p> Defaults to the entity name.
     */
    String name() default "";
    
    String pk() default "id";

}