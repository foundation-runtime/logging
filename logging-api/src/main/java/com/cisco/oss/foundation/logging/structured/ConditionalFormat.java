package com.cisco.oss.foundation.logging.structured;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConditionalFormat {
	String format();
	FieldCriterion[] criteria() default @FieldCriterion;    
}
