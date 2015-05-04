/*
 * Copyright 2015 Cisco Systems, Inc.
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
class Log4j2BasicTest {

  val LOGGER = LoggerFactory.getLogger(classOf[Log4j2BasicTest])

  @Test def simpleTest(){


//    Thread.sleep(250);

    FlowContextFactory.createFlowContext();
    LOGGER.info("simple test")

    FlowContextFactory.createFlowContext();
    LOGGER.info("simple test2")

    FlowContextFactory.createFlowContext();
    LOGGER.info("simple test3")

//    Thread.sleep(2000)

  }






}
