package com.cisco.oss.foundation.logging.transactions;

import com.cisco.oss.foundation.flowcontext.FlowContextFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class for HTTP transactions logging
 * @author abrandwi
 *
 */
public class HttpLogger extends TransactionLogger {



    //private enum HttpPropertyKey {Method, SourceName, SourcePort, URL, ResponseStatusCode, ResponseContentLength, ResponseBody;};
    private enum HttpVerbosePropertyKey {RequestHeaders, RequestBody, ResponseHeaders, ResponseBody;};

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpLogger.class);

    @Autowired
    private Environment environment;

    public HttpLogger(){

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

    public static HttpLogger startAsync(final Logger logger, final Logger auditor, final HttpServletRequest request) {
        return startAsync(logger, auditor, request, null);
    }

    public static void start(final Logger logger, final Logger auditor, final HttpServletRequest request, final String requestBody) {
        if(!createLoggingAction(logger, auditor, new HttpLogger())) {
            return;
        }

        HttpLogger httpLogger = (HttpLogger) getInstance();
        if (httpLogger == null) {
            return;
        }

        httpLogger.startInstance(request, requestBody);
    }

    public static HttpLogger startAsync(final Logger logger, final Logger auditor, final HttpServletRequest request, final String requestBody) {
        HttpLogger httpLogger = new HttpLogger();
        if(!createLoggingActionAsync(logger, auditor, httpLogger)) {
            return null;
        }

        httpLogger.startInstance(request, requestBody);
        return httpLogger;
    }

    public static void success(final Response response) {
        HttpLogger httpLogger = (HttpLogger) getInstance();
        if (httpLogger == null) {
            return;
        }

        httpLogger.successInstance(response);
    }


    public static void successAsync(final HttpResponse response, HttpLogger httpLogger) {
        FlowContextFactory.deserializeNativeFlowContext(httpLogger.getFlowContextAsync(httpLogger));
        httpLogger.successInstance(response);
    }

    public static void failure(final Response response) {
        HttpLogger httpLogger = (HttpLogger) getInstance();
        if (httpLogger == null) {
            return;
        }

        httpLogger.failureInstance(response);
    }

    public static void failureAsync(final HttpResponse response, HttpLogger httpLogger) {
        FlowContextFactory.deserializeNativeFlowContext(httpLogger.getFlowContextAsync(httpLogger));
        httpLogger.failureInstance(response);
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

    protected void successInstance(final Response response) {
        try {
            end();
            addPropertiesSuccess(response);
            addPropertiesProcessingTime();
            writePropertiesToLog(this.auditor, Level.INFO);
        } catch (Exception e) {
            logger.error("Failed logging HTTP transaction success: " + e.getMessage(), e);
        }
    }

    protected void successInstance(final HttpResponse response) {
        try {
            end();
            addPropertiesSuccess(response);
            addPropertiesProcessingTime();
            writePropertiesToLog(this.auditor, Level.INFO);
        } catch (Exception e) {
            logger.error("Failed logging HTTP transaction success: " + e.getMessage(), e);
        }
    }

    protected void failureInstance(final Response response) {
        try {
            end();
            addPropertiesFailure(response);
            addPropertiesProcessingTime();
            writePropertiesToLog(this.auditor, Level.ERROR);
        } catch (Exception e) {
            logger.error("Failed logging HTTP transaction failure: " + e.getMessage(), e);
        }
    }

    protected void failureInstance(final HttpResponse response) {
        try {
            end();
            addPropertiesFailure(response);
            addPropertiesProcessingTime();
            writePropertiesToLog(this.auditor, Level.ERROR);
        } catch (Exception e) {
            logger.error("Failed logging HTTP transaction failure: " + e.getMessage(), e);
        }
    }

    protected void addPropertiesStart(final HttpServletRequest request, String requestBody) {
        super.addPropertiesStart("HTTP");

        this.properties.put(loggingKeys.getKeyValue(LoggingKeys.IP_SRC.name()), request.getRemoteHost());
        this.properties.put(loggingKeys.getKeyValue(LoggingKeys.PORT_SRC.name()), String.valueOf(request.getRemotePort()));
        this.properties.put(loggingKeys.getKeyValue(LoggingKeys.HTTP_METHOD.name()), request.getMethod());
        this.properties.put(loggingKeys.getKeyValue(LoggingKeys.URL.name()), getFullURL(request));

        addVerbosePropertiesStart(request, requestBody);
    }

    public static boolean isVerbose() {
        return ConfigurationUtil.INSTANCE.isVerbose();
    }

    protected void addVerbosePropertiesStart(final HttpServletRequest request, String requestBody) {
        if ( ConfigurationUtil.INSTANCE.isVerbose() ) {
            String requestHeaders = getMapAsString(getHeadersAsMap(request), secondSeparator);

            if (requestHeaders != null) {
                this.properties.put(HttpVerbosePropertyKey.RequestHeaders.name(), requestHeaders);
            }
            if (requestBody != null) {
                this.properties.put(HttpVerbosePropertyKey.RequestBody.name(), StringUtils.deleteWhitespace(requestBody));
            }
        }
    }

    protected void addPropertiesSuccess(final HttpResponse response) {
        super.addPropertiesSuccess();

        this.properties.put(  loggingKeys.getKeyValue(LoggingKeys.HTTP_CODE.name()), String.valueOf(response.getStatus()));
        if ( response.getBody() != null ) {
            this.properties.put(loggingKeys.getKeyValue(LoggingKeys.ResponseContentLength.name()), String.valueOf(response.getBody().length()));
        }

        addVerbosePropertiesSuccess(response);
    }

    protected void addPropertiesSuccess(final Response response) {
        super.addPropertiesSuccess();

        this.properties.put(loggingKeys.getKeyValue(LoggingKeys.HTTP_CODE.name()), String.valueOf(response.getStatus()));
        if ( response.getEntity() != null ) {
            if ( !(response.getEntity() instanceof StreamingOutput) ) {
                this.properties.put(loggingKeys.getKeyValue(LoggingKeys.ResponseContentLength.name()), String.valueOf(response.getEntity().toString().length()));
            } else {
                this.properties.put(loggingKeys.getKeyValue(LoggingKeys.ResponseContentLength.name()), "chunked");
            }
        }

        addVerbosePropertiesSuccess(response);
    }

    protected void addVerbosePropertiesSuccess(Response response) {
        if (ConfigurationUtil.INSTANCE.isVerbose()) {
            String responseHeaders = getMapAsString(getHeadersAsMap(response), secondSeparator);

            if (responseHeaders != null) {
                this.properties.put(HttpVerbosePropertyKey.ResponseHeaders.name(), responseHeaders);
            }
            if ( (response.getEntity() != null) && !(response.getEntity() instanceof StreamingOutput) ) {
                this.properties.put(HttpVerbosePropertyKey.ResponseBody.name(), response.getEntity().toString());
            }
        }
    }

    protected void addVerbosePropertiesSuccess(HttpResponse response) {
        if (ConfigurationUtil.INSTANCE.isVerbose()) {
            String responseHeaders = getMapAsString(response.getHeaders(), secondSeparator);

            if (responseHeaders != null) {
                this.properties.put(HttpVerbosePropertyKey.ResponseHeaders.name(), responseHeaders);
            }
            if ( response.getBody() != null) {
                this.properties.put(HttpVerbosePropertyKey.ResponseBody.name(), response.getBody());
            }
        }
    }

    protected void addPropertiesFailure(final Response response) {
        super.addPropertiesFailure();

        this.properties.put(loggingKeys.getKeyValue(LoggingKeys.HTTP_CODE.name()), String.valueOf(response.getStatus()));

        if (response.getEntity() != null) {
            this.properties.put(loggingKeys.getKeyValue(LoggingKeys.MSG.name()), response.getEntity().toString());
        }
    }

    protected void addPropertiesFailure(final HttpResponse response) {
        super.addPropertiesFailure();

        this.properties.put(loggingKeys.getKeyValue(LoggingKeys.HTTP_CODE.name()), String.valueOf(response.getStatus()));

        if (response.getBody() != null) {
            this.properties.put(loggingKeys.getKeyValue(LoggingKeys.MSG.name()), response.getBody().toString());
        }
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

    protected static Map<String, String> getHeadersAsMap(Response response) {
        MultivaluedMap<String, Object> headers = response.getMetadata();
        if (headers == null) {
            return null;
        }

        Map<String, String> map = new HashMap<String, String>();

        for (Entry<String, List<Object>> header : headers.entrySet()) {
            StringBuilder values = new StringBuilder();
            for (Object singleValue : header.getValue()) {
                values.append(singleValue).append(",");
            }
            values.deleteCharAt(values.length()-1); // Delete last comma

            String key = header.getKey();
            String value = values.toString();
            map.put(key, value);
        }

        return map;
    }
}