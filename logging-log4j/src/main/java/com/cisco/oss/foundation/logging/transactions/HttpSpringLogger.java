package com.cisco.oss.foundation.logging.transactions;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.slf4j.Logger;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class for HTTP transactions logging
 *
 * @author abrandwi
 */
public class HttpSpringLogger extends TransactionLogger {

    private enum HttpPropertyKey {Summary, Method, SourceName, SourcePort, URL, ResponseStatusCode, ResponseContentLength, ResponseBody}


    private enum HttpVerbosePropertyKey {RequestHeaders, RequestBody, ResponseHeaders, ResponseBody}


// Those headers will be used when we'll add sourceIdentity to properties
//	private static final String SOURCE_TYPE_HEADER = "Source-Type"; // Cisco header (used by UPM): Should contain the type of machine sent the request (e.g. UPM, TSTVM) - currently won't be sent to PPS
//  private static final String FROM_HEADER 	   = "From"; 		// HTTP header: Should contain the specific machine sent the request (e.g. UPM1) - currently won't be sent to PPS

    // ********* Public methods *********


    public static void start(final Logger logger, final Logger auditor, final HttpServletRequest request) {
        start(logger, auditor, request, null);
    }

    public static void start(final Logger logger, final Logger auditor, final HttpServletRequest request, final String requestBody) {
        if (!TransactionLogger.createLoggingAction(logger, auditor, new HttpSpringLogger())) {
            return;
        }

        HttpSpringLogger httpSpringLogger = (HttpSpringLogger) TransactionLogger.getInstance();
        if (httpSpringLogger == null) {
            return;
        }

        httpSpringLogger.startInstance(request, requestBody);
    }

    public static void success( ResponseEntity response) {
        HttpSpringLogger httpSpringLogger = (HttpSpringLogger) TransactionLogger.getInstance();
        if (httpSpringLogger == null) {
            return;
        }

        httpSpringLogger.successInstance(response);
    }

    public static void failure(final ResponseEntity response) {
        HttpSpringLogger httpSpringLogger = (HttpSpringLogger) TransactionLogger.getInstance();
        if (httpSpringLogger == null) {
            return;
        }

        httpSpringLogger.failureInstance(response);
    }

    // ********* Private methods *********

    protected void startInstance(final HttpServletRequest request, final String requestBody) {
        try {
            addPropertiesStart(request, requestBody);
            writePropertiesToLog(this.auditor, Level.DEBUG);
        } catch (Exception e) {
            logger.error("Failed logging HTTP transaction start: " + e.getMessage(), e);
        }
    }

    protected void successInstance(final ResponseEntity response) {
        try {
            end();
            addPropertiesSuccess(response);
            addPropertiesProcessingTime();
            writePropertiesToLog(this.logger, Level.INFO);
        } catch (Exception e) {
            logger.error("Failed logging HTTP transaction success: " + e.getMessage(), e);
        }
    }

    protected void failureInstance(final ResponseEntity response) {
        try {
            end();
            addPropertiesFailure(response);
            addPropertiesProcessingTime();
            writePropertiesToLog(this.logger, Level.ERROR);
        } catch (Exception e) {
            logger.error("Failed logging HTTP transaction failure: " + e.getMessage(), e);
        }
    }

    protected void addPropertiesStart(final HttpServletRequest request, String requestBody) {
        super.addPropertiesStart("HTTP");
//        this.properties.put(HttpPropertyKey.Summary.name(),"Summary");
        this.properties.put(HttpPropertyKey.SourceName.name(), request.getRemoteHost());
        this.properties.put(HttpPropertyKey.SourcePort.name(), String.valueOf(request.getRemotePort()));
        this.properties.put(HttpPropertyKey.Method.name(), request.getMethod());
        this.properties.put(HttpPropertyKey.URL.name(), getFullURL(request));

        addVerbosePropertiesStart(request, requestBody);
    }

    public static boolean isVerbose() {
        return ConfigurationUtil.INSTANCE.isVerbose();
    }

    protected void addVerbosePropertiesStart(final HttpServletRequest request, String requestBody) {
        if (ConfigurationUtil.INSTANCE.isVerbose()) {
            String requestHeaders = TransactionLogger.getMapAsString(getHeadersAsMap(request), TransactionLogger.secondSeparator);

            if (requestHeaders != null) {
                this.properties.put(HttpVerbosePropertyKey.RequestHeaders.name(), requestHeaders);
            }
            if (requestBody != null) {
                this.properties.put(HttpVerbosePropertyKey.RequestBody.name(), StringUtils.deleteWhitespace(requestBody));
            }
        }
    }

    protected void addPropertiesSuccess(final ResponseEntity response) {
        super.addPropertiesSuccess();

        this.properties.put(HttpPropertyKey.ResponseStatusCode.name(), String.valueOf(response.getStatusCode()));

        if (response.toString() != null) {
            if (!(response.toString() == null)) {
                this.properties.put(HttpPropertyKey.ResponseContentLength.name(), String.valueOf(response.toString().length()));
            } else {
                this.properties.put(HttpPropertyKey.ResponseContentLength.name(), "chunked");
            }
        }

        addVerbosePropertiesSuccess(response);
//        } catch (IOException e) {
//            logger.error("Failed to parse HTTP response" + e.getMessage(), e);
//        }
    }


    protected void addVerbosePropertiesSuccess(ResponseEntity response) {
        if (ConfigurationUtil.INSTANCE.isVerbose()) {
            String responseHeaders = TransactionLogger.getMapAsString(getHeadersAsMap(response), TransactionLogger.secondSeparator);
//            try {
            if (responseHeaders != null) {
                this.properties.put(HttpVerbosePropertyKey.ResponseHeaders.name(), responseHeaders);
            }
            if ((response.toString() != null)) {
                this.properties.put(HttpVerbosePropertyKey.ResponseBody.name(), response.toString());
            }
//            } catch (IOException e) {
//                logger.error("Failed to parse HTTP response" + e.getMessage(), e);
//            }
        }
    }

    protected void addPropertiesFailure(final ResponseEntity response) {
        super.addPropertiesFailure();

        this.properties.put(HttpPropertyKey.ResponseStatusCode.name(), String.valueOf(response.getStatusCode()));
//        try {
        if (response.toString() != null) {
            this.properties.put(HttpPropertyKey.ResponseBody.name(), response.toString());
        }
//        } catch (IOException e) {
//            logger.error("Failed to parse HTTP response" + e.getMessage(), e);
//        }
    }

    protected static String getFullURL(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();

        if (queryString == null) {
            return requestURL.toString();
        } else {
            return requestURL.append('?').append(queryString).toString();
        }
    }

    protected static Map<String, String> getHeadersAsMap(HttpServletRequest request) {
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return null;
        }

        Map<String, String> map = new HashMap<String, String>();

        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }

        return map;
    }

    protected static Map<String, String> getHeadersAsMap(ResponseEntity response) {

        Map<String, List<String>> headers = new HashMap<>(response.getHeaders());
        Map<String, String> map = new HashMap<>();
        for (String header : headers.get(0)) {
            StringBuilder values = new StringBuilder();
            values.append(response.getHeaders()).append(",");

            String key = header;
            String value = values.toString();
            map.put(key, value);
        }

        return map;
    }

}
