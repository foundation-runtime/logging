package com.cisco.vss.foundation.logging.structured;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface FieldCriterion {
	String name() default "";
	String value() default "";
	
}
