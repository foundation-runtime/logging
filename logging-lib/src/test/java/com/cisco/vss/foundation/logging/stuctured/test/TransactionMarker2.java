package com.cisco.vss.foundation.logging.stuctured.test;

import com.cisco.vss.foundation.logging.stuctured.*;

public class TransactionMarker2 extends AbstractFoundationLoggingMarker {
	
	public TransactionMarker2(String firstName, int emp_num) {
		super();
		this.firstName = firstName;
		this.emp_num = emp_num;
	}
	
	
	private static final long serialVersionUID = 9161271890930513129L;			

	

	@UserField
	private String firstName;
	@UserField
	private int emp_num;
	


}
