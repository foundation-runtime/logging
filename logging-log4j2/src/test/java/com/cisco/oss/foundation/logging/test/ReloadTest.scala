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

import java.io.FileOutputStream
import java.util.Properties

import org.junit.Test
import org.slf4j.LoggerFactory

/**
 * Created by Yair Ogen on 22/10/2014.
 */
class ReloadTest {

  val LOGGER = LoggerFactory.getLogger(classOf[ReloadTest])

  @Test
  def testReloadOfConfig(): Unit ={
    LOGGER.trace("shouldn't see this line")

    val url = this.getClass.getResource("/config.properties")
    val props = new Properties()
    props.load(url.openStream())

    props.setProperty("logging.logger.trace-log.prefix.1","com.cisco.oss.foundation.logging.test")
    props.setProperty("logging.logger.trace-log.level","trace")

    LOGGER.info("before file change")
    val stream: FileOutputStream = new FileOutputStream(url.getPath)
    props.store(stream,"")
    stream flush()
    stream close()
    LOGGER.info("after file change")

    Thread sleep(10000)

    LOGGER.info("should be the info line followed by trace")
    LOGGER.trace("should be the first trace line")
  }

}
