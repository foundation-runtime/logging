package com.cisco.oss.foundation.logging.transactions;

import com.cisco.oss.foundation.flowcontext.FlowContextFactory;
import com.google.common.base.Joiner;
import org.apache.commons.lang.StringUtils;
import org.slf4j.event.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;

import javax.annotation.PostConstruct;
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

    //private enum HttpPropertyKey {Method, SourceName, SourcePort, URL, ResponseStatusCode, ResponseContentLength, ResponseBody}
    private enum HttpVerbosePropertyKey {RequestHeaders, RequestBody, ResponseHeaders, ResponseBody}

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpSpringLogger.class);

    @Autowired
    private Environment environment;

    public HttpSpringLogger(){

    }

    @PostConstruct
    public void init(){
        ConfigurationUtil.setConfigSource(environment);
    }


// Those headers will be used when we'll add sourceIdentity to properties
//	private static final String SOURCE_TYPE_HEADER = "Source-Type"; // Cisco header (used by UPM): Should contain the type of machine sent the request (e.g. UPM, TSTVM) - currently won't be sent to PPS
//  private static final String FROM_HEADER 	   = "From"; 		// HTTP header: Should contain the specific machine sent the request (e.g. UPM1) - currently won't be sent to PPS

    // ********* Public methods *********


    public static void start(final Logger logger, final Logger auditor, final HttpServletRequest request) {

        start(logger, auditor, request, null);
    }

    public static HttpSpringLogger startAsync(final Logger logger, final Logger auditor, final HttpServletRequest request) {
        return startAsync(logger, auditor, request, null);
    }

    public static HttpSpringLogger startAsync(final Logger logger, final Logger auditor, final HttpServletRequest request, final String requestBody) {
        HttpSpringLogger httpSpringLogger = new HttpSpringLogger();
        if(!createLoggingActionAsync(logger, auditor, httpSpringLogger)) {
            return null;
        }

        httpSpringLogger.startInstance(request, requestBody);
        return httpSpringLogger;
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
        success(response, httpSpringLogger);
    }

    public static void successAsync(ResponseEntity response, HttpSpringLogger httpSpringLogger) {
        FlowContextFactory.deserializeNativeFlowContext(TransactionLogger.getFlowContextAsync(httpSpringLogger));
        httpSpringLogger.successInstance(response);
    }

    public static void success(ResponseEntity response, HttpSpringLogger httpSpringLogger) {
        if (httpSpringLogger == null) {
            return;
        }

        httpSpringLogger.successInstance(response);
    }

    public static void failure(final ResponseEntity response) {
        HttpSpringLogger httpSpringLogger = (HttpSpringLogger) TransactionLogger.getInstance();
        failure(response, httpSpringLogger);
    }

    public static void failure(ResponseEntity response, HttpSpringLogger httpSpringLogger) {
        if (httpSpringLogger == null) {
            return;
        }

        httpSpringLogger.failureInstance(response);
    }

    public static void failureAsync(ResponseEntity response, HttpSpringLogger httpSpringLogger) {
        FlowContextFactory.deserializeNativeFlowContext(TransactionLogger.getFlowContextAsync(httpSpringLogger));
        httpSpringLogger.failureInstance(response);
    }

    // ********* Private methods *********

    protected void startInstance(final HttpServletRequest request, final String requestBody) {
        try {
            addPropertiesStart(request, requestBody);
            writePropertiesToLog(this.logger, Level.DEBUG);
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
        putProperty(LoggingKeys.IP_SRC.name(), request.getRemoteHost() );
        putProperty(LoggingKeys.PORT_SRC.name(), String.valueOf(request.getRemotePort()) );
        putProperty(LoggingKeys.HTTP_METHOD.name(), request.getMethod() );
        putProperty(LoggingKeys.URL.name(),  getFullURL(request) );


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

        putProperty(LoggingKeys.HTTP_CODE.name(), String.valueOf(response.getStatusCode()) );//this.properties.put(loggingKeys.getKeyValue(LoggingKeys.HTTP_CODE.name()), String.valueOf(response.getStatusCode()));
        Object body = response.getBody();
        if ( body != null ) {
            if (body instanceof String) {
                putProperty(LoggingKeys.ResponseContentLength.name(), String.valueOf(body.toString().length()));//this.properties.put(loggingKeys.getKeyValue(LoggingKeys.ResponseContentLength.name()), String.valueOf(body.toString().length()));
            } else {
                putProperty(LoggingKeys.ResponseContentLength.name(),"chunked" );//this.properties.put(loggingKeys.getKeyValue(LoggingKeys.ResponseContentLength.name()), "chunked");
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

            Object body = response.getBody();
            if ( (body != null) && (body instanceof String) ) {
                this.properties.put(HttpVerbosePropertyKey.ResponseBody.name(), (String)response.getBody());
            }
//            } catch (IOException e) {
//                logger.error("Failed to parse HTTP response" + e.getMessage(), e);
//            }
        }
    }

    protected void addPropertiesFailure(final ResponseEntity response) {
        super.addPropertiesFailure();

        putProperty(LoggingKeys.HTTP_CODE.name(),String.valueOf(response.getStatusCode()));
//        try {
        if ( (response.getBody() != null) && (response.getBody() instanceof String) ) {
            putProperty(LoggingKeys.MSG.name(),(String)response.getBody());
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

    // Flat the map of list of string to map of strings, with theoriginal values, seperated by comma
    protected static Map<String, String> getHeadersAsMap(ResponseEntity response) {

        Map<String, List<String>> headers = new HashMap<>(response.getHeaders());
        Map<String, String> map = new HashMap<>();

        for ( Map.Entry<String, List<String>> header :headers.entrySet() ) {
            String headerValue = Joiner.on(",").join(header.getValue());
            map.put(header.getKey(), headerValue);
        }

        return map;
    }

}
