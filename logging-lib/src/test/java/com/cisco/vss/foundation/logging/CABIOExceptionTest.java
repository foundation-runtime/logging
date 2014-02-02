package com.cisco.vss.foundation.logging;

import org.junit.Test;

/**
 * 
 */
public final class CABIOExceptionTest {

    @Test
    public void cabIOExceptionTest(){
    	new CABIOException("dummy");
    	new CABIOException("dummy", new IllegalArgumentException());
    	new CABIOException(new IllegalArgumentException());
    }
    
}
