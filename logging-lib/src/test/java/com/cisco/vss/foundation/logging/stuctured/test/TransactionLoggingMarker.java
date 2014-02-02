package com.cisco.vss.foundation.logging.stuctured.test;

import java.net.URI;
import com.cisco.vss.foundation.logging.stuctured.*;

@DefaultFormat(TransactionLoggingMarker.DEFAULT_FORMAT)
@ConditionalFormats({ @ConditionalFormat(format = "start message format", criteria = { @FieldCriterion(name = "transactionState", value = "START") }), @ConditionalFormat(format = "end message format", criteria = { @FieldCriterion(name = "transactionState", value = "END") }),
		@ConditionalFormat(format = "error message format", criteria = { @FieldCriterion(name = "transactionState", value = "ERROR") }) })
public class TransactionLoggingMarker extends AbstractCabLoggingMarker {

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
