package com.cisco.vss.foundation.logging.stuctured;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DefaultFormat {
	String value();
}
