package com.cisco.oss.foundation.logging.transactions;

import java.io.StringWriter;
import java.io.Writer;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

public class JsonUtil {

	private static ObjectMapper mapper;
	
	static {
		mapper = new ObjectMapper();
        mapper.setSerializationInclusion(Inclusion.NON_EMPTY);	
	}

    public static String getObjectAsJson(Object object) {

        Writer strWriter = new StringWriter();
        try {
            mapper.writeValue(strWriter, object);
            return strWriter.toString();
        } catch(Exception e) {         
            return null;
        }
    }
}
