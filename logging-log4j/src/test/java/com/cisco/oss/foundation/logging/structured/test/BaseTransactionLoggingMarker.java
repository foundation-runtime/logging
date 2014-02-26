package com.cisco.oss.foundation.logging.structured.test;

import com.cisco.oss.foundation.logging.structured.*;

public abstract class BaseTransactionLoggingMarker extends AbstractFoundationLoggingMarker {

	private static final long serialVersionUID = 1327086825288276442L;


	@UserField
	private String base = "defaultBase";


	public String getBase() {
		return base;
	}


	public void setBase(String base) {
		this.base = base;
	}
	
}
