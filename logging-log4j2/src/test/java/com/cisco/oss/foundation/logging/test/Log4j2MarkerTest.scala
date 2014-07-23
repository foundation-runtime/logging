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

package com.cisco.oss.foundation.logging.test

import com.cisco.oss.foundation.flowcontext.FlowContextFactory
import com.cisco.oss.foundation.logging.test.structured.TransactionMarker
import org.junit.Test
import org.slf4j.LoggerFactory

/**
  * Created by Yair Ogen on 17/07/2014.
  */
class Log4j2MarkerTest {

   val LOGGER = LoggerFactory.getLogger(classOf[Log4j2MarkerTest])



   @Test def simpleMarkerTest(){

     Thread.sleep(1000)
     val transactionMarker: TransactionMarker = new TransactionMarker("mySessionIdFromTest")

     transactionMarker setSourceId ("test source id")
     transactionMarker setSourceType ("test source type")

     LOGGER.info(transactionMarker, "simple test")

     val transactionMarker2: TransactionMarker = new TransactionMarker("mySessionIdFromTest")
     transactionMarker2 setSourceId ("sourceId1")
     transactionMarker2 setSourceType ("sourceType1")
     LOGGER.info(transactionMarker2, "simple test")
   }

 }
