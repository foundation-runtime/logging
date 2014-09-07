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

import com.cisco.oss.foundation.logging.{FoundationLevel, ApplicationState}
import com.cisco.oss.foundation.logging.test.structured.TransactionMarker
import org.junit.{Ignore, Test}
import org.slf4j.LoggerFactory

/**
  * Created by Yair Ogen on 17/07/2014.
  */
class Log4j2MarkerTest {

  val LOGGER = LoggerFactory.getLogger(classOf[Log4j2MarkerTest])
   val AUDITOR = LoggerFactory.getLogger("audit."+classOf[Log4j2MarkerTest].getName)
  val transactionMarker: TransactionMarker = new TransactionMarker("mySessionIdFromTest")

  @Test def simpleMarkerTest(){

     //     Thread.sleep(1000)

     transactionMarker setSourceId ("test source id")
     transactionMarker setSourceType ("test source type")

//     LOGGER.info(transactionMarker, "simple test")
     AUDITOR.info(transactionMarker, "simple test 2")

     val transactionMarker2: TransactionMarker = new TransactionMarker("mySessionIdFromTest")
     transactionMarker2 setSourceId ("sourceId1")
     transactionMarker2 setSourceType ("sourceType1")
     LOGGER.info(transactionMarker2, "simple test")
     LOGGER.info("simple test between markers")
     LOGGER.info(transactionMarker2, "simple test")
//     Thread.sleep(2000)

   }

  @Ignore @Test def loadTest(){
    LOGGER.info("starting test")
    ApplicationState.getInstance().setState(FoundationLevel.INFO,"my persistent message!!!")
    for(i <- 1 to 1250000){
      LOGGER.info("0123456879012345687901234568790123456879012345687901234568790123456879012345687901234568790123456879");
    }
    Thread.sleep(120000)
    LOGGER.info("finished test")
  }

 }
