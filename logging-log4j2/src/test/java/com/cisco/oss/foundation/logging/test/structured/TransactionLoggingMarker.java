/*
 * Copyright 2014 Cisco Systems, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.cisco.oss.foundation.logging.test.structured;

import com.cisco.oss.foundation.logging.structured.*;

import java.net.URI;

@DefaultFormat(TransactionLoggingMarker.DEFAULT_FORMAT)
@ConditionalFormats({ @ConditionalFormat(format = "start message format", criteria = { @FieldCriterion(name = "transactionState", value = "START") }), @ConditionalFormat(format = "end message format", criteria = { @FieldCriterion(name = "transactionState", value = "END") }),
		@ConditionalFormat(format = "error message format", criteria = { @FieldCriterion(name = "transactionState", value = "ERROR") }) })
public class TransactionLoggingMarker extends AbstractFoundationLoggingMarker {

	public enum TransactionState {

		START, END, ERROR;
	}
	
	static{
		
	}
	
	public static final String DEFAULT_FORMAT = "cvjdlkgjdgjkl";//ConfigurationFactory.getConfiguration().getString("")

	private static final long serialVersionUID = 1327086825288276442L;

	private TransactionState transactionState = null;

	@UserField
	private String method = null;

	@UserField
	private URI url = null;

	@UserField
	private String httpResponse = null;

	@UserField
	private String errorMessage = null;

	@UserField
	private String additionalInfo = null;

	public TransactionLoggingMarker() {
	}

	// @Override
	// public String getDefaultLayout()
	// {
	// StringBuilder logLine = new StringBuilder();
	//
	// switch(transactionState)
	// {
	// case START:
	//
	// logLine.append(startTransactionWording);
	// break;
	//
	// case END:
	//
	// logLine.append(endTransactionWording);
	// break;
	//
	// case ERROR:
	//
	// logLine.append(errorTransactionWording);
	// break;
	// }
	// return logLine.toString();
	// }

	public TransactionLoggingMarker setStart() {
		this.transactionState = TransactionState.START;
		return this;
	}

	public TransactionLoggingMarker setEnd(String httpResponse) {
		this.transactionState = TransactionState.END;
		this.httpResponse = setHttpResponse(httpResponse);
		return this;
	}

	public TransactionLoggingMarker setError(String httpResponse, String errorMessage) {
		this.transactionState = TransactionState.ERROR;
		this.httpResponse = setHttpResponse(httpResponse);
		this.errorMessage = setErrorMessage(errorMessage);
		return this;
	}

	private URI setUrl(URI url) {
		if (url == null)
			return URI.create("unknownURL");
		return url;
	}

	private String setMethod(String method) {
		if (method == null || method.equals(""))
			return "unknownMethod";
		return method;
	}

	private String setAdditionalInfo(String additionalInfo) {
		if (additionalInfo == null || additionalInfo.equals(""))
			return "";
		return "[" + additionalInfo + "]";
	}

	private String setHttpResponse(String httpResponse) {
		if (httpResponse == null || httpResponse.equals(""))
			return "unknownHttpResponse";
		return httpResponse;
	}

	private String setErrorMessage(String errorMessage) {
		if (errorMessage == null || errorMessage.equals(""))
			return "unknownErrorMessage";
		return errorMessage;
	}

	public String getMethod() {
		return method;
	}

	public URI getUrl() {
		return url;
	}

	public String getHttpResponse() {
		return httpResponse;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public String getAdditionalInfo() {
		return additionalInfo;
	}

	public TransactionState getTransactionState() {
		return transactionState;
	}
}
