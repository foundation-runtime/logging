package com.cisco.oss.foundation.logging.transactions;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class HttpResponse {

  private String body;
  private int status;
  private Map<String, String> headers;

  public HttpResponse(Response response) {
    this.body = (String) response.getEntity();
    this.status = response.getStatus();
    this.headers = convertMultiMapHeaders(response.getMetadata());
  }

  public HttpResponse(String body, int status, Map<String, String> headers) {
    this.body = body;
    this.status = status;
    this.headers = headers;
  }

  private Map<String, String> convertMultiMapHeaders(MultivaluedMap<String,Object> multivaluedMap) {
    Map<String, String> headersResult = new HashMap<>();

    for (Entry<String, List<Object>> item : multivaluedMap.entrySet()) {
      if (item.getValue() != null && item.getValue().get(0) != null) {
        headersResult.put(item.getKey(), (String) item.getValue().get(0) );
      }
    }

    //multivaluedMap.forEach((key, val) -> headersResult.put(key, (String) ((List)val).get(0) ) );

    return headersResult ;
  }

  public String getBody() {
    return body;
  }
  public void setBody(String body) {
    this.body = body;
  }
  public int getStatus() {
    return status;
  }
  public void setStatus(int status) {
    this.status = status;
  }
  public Map<String, String> getHeaders() {
    return headers;
  }
  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }


}