/**
 * 
 */
package com.cisco.oss.foundation.logging.structured;

import java.lang.annotation.*;

/**
 * @author Yair Ogen
 *
 */

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UserField {
	String description() default "";
	boolean suppressNull() default false;
}
