package com.cisco.vss.foundation.logging;

import org.junit.Test;

/**
 * 
 */
public final class FoundationIOExceptionTest {

    @Test
    public void cabIOExceptionTest(){
    	new FoundationIOException("dummy");
    	new FoundationIOException("dummy", new IllegalArgumentException());
    	new FoundationIOException(new IllegalArgumentException());
    }
    
}
