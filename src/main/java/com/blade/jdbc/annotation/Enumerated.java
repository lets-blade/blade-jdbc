package com.blade.jdbc.annotation;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;

import static com.blade.jdbc.annotation.EnumType.ORDINAL;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Specifies that a persistent property or field should be 
 * persisted as a enumerated type. It may be used in conjunction 
 * with the {@link Basic} annotation.
 *
 * <pre>
 *   Example:
 *
 *   public enum EmployeeStatus {FULL_TIME, PART_TIME, CONTRACT}
 *
 *   public enum SalaryRate {JUNIOR, SENIOR, MANAGER, EXECUTIVE}
 *
 *   &#064;Entity public class Employee {
 *       public EmployeeStatus getStatus() {...}
 *       &#064;Enumerated(STRING)
 *       public SalaryRate getPayScale() {...}
 *       ...
 *   }
 * </pre>
 *
 * @since Java Persistence 1.0
 */
@Target({METHOD, FIELD}) 
@Retention(RUNTIME)
public @interface Enumerated {

    /** (Optional) The type used in mapping an enum type. */
    EnumType value() default ORDINAL;
}
