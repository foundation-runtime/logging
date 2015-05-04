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
package com.cisco.oss.foundation.logging.appender;


/**
 * Responsible for periodically sampling the number of log files with a given
 * base filename and time-based extension, and for deleting the older files if
 * the scavenger's conditions are met.
 * 
 * @author <a href="mailto:simon_park_mail AT yahoo DOT co DOT uk">Simon
 *         Park</a>
 * @author <a href="mailto:berndq AT gmx DOT net">Bernd</a>
 * @version 2.1
 */
interface LogFileScavenger {

  /**
   * Starts the scavenger.
   */
  void begin();

  /**
   * Stops the scavenger.
   */
  void end();

  void scavenge();

  void init(FoundationFileRollingAppender appender,
            AppenderRollingProperties properties);
}
