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

package com.cisco.oss.foundation.logging.appenders;

import com.cisco.oss.foundation.logging.ApplicationState;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.rolling.RollingRandomAccessFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;

import java.io.*;

/**
 * Created by Yair Ogen on 22/07/2014.
 */
public class FoundationRollingRandomAccessFileManager extends RollingRandomAccessFileManager {

    private static final FoundationRollingRandomAccessFileManagerFactory FACTORY = new FoundationRollingRandomAccessFileManagerFactory();
    private FoundationRollingRandomAccessFileAppender foundationRollingRandomAccessFileAppender = null;

    public FoundationRollingRandomAccessFileManager(final RandomAccessFile raf, final String fileName,
                                                    final String pattern, final OutputStream os, final boolean append,
                                                    final boolean immediateFlush, final int bufferSize, final long size, final long time,
                                                    final TriggeringPolicy policy, final RolloverStrategy strategy,
                                                    final String advertiseURI, final Layout<? extends Serializable> layout) {
        super(raf, fileName, pattern, os, append, immediateFlush, bufferSize, size, time, policy, strategy, advertiseURI, layout);
    }

    public static FoundationRollingRandomAccessFileManager getRollingRandomAccessFileManager(final String fileName,
                                                                                             final String filePattern, final boolean isAppend, final boolean immediateFlush, final int bufferSize,
                                                                                             final TriggeringPolicy policy, final RolloverStrategy strategy, final String advertiseURI,
                                                                                             final Layout<? extends Serializable> layout) {
        return (FoundationRollingRandomAccessFileManager) getManager(fileName, new FactoryData(filePattern, isAppend,
                immediateFlush, bufferSize, policy, strategy, advertiseURI, layout), FACTORY);
    }

    @Override
    protected void createFileAfterRollover() throws IOException {
        super.createFileAfterRollover();
        if (foundationRollingRandomAccessFileAppender != null) {
            ApplicationState.logState(foundationRollingRandomAccessFileAppender);
        }
    }

    void setFoundationRollingRandomAccessFileAppender(FoundationRollingRandomAccessFileAppender foundationRollingRandomAccessFileAppender) {
        this.foundationRollingRandomAccessFileAppender = foundationRollingRandomAccessFileAppender;
    }

    static class DummyOutputStream extends OutputStream {
        @Override
        public void write(final int b) throws IOException {
        }

        @Override
        public void write(final byte[] b, final int off, final int len) throws IOException {
        }
    }

    /**
     * Factory data.
     */
    private static class FactoryData {
        private final String pattern;
        private final boolean append;
        private final boolean immediateFlush;
        private final int bufferSize;
        private final TriggeringPolicy policy;
        private final RolloverStrategy strategy;
        private final String advertiseURI;
        private final Layout<? extends Serializable> layout;

        /**
         * Create the data for the factory.
         *
         * @param pattern        The pattern.
         * @param append         The append flag.
         * @param immediateFlush
         * @param bufferSize
         * @param policy
         * @param strategy
         * @param advertiseURI
         * @param layout
         */
        public FactoryData(final String pattern, final boolean append, final boolean immediateFlush,
                           final int bufferSize, final TriggeringPolicy policy, final RolloverStrategy strategy,
                           final String advertiseURI, final Layout<? extends Serializable> layout) {
            this.pattern = pattern;
            this.append = append;
            this.immediateFlush = immediateFlush;
            this.bufferSize = bufferSize;
            this.policy = policy;
            this.strategy = strategy;
            this.advertiseURI = advertiseURI;
            this.layout = layout;
        }
    }


    private static class FoundationRollingRandomAccessFileManagerFactory implements ManagerFactory<FoundationRollingRandomAccessFileManager, FactoryData> {

        /**
         * Create the RollingRandomAccessFileManager.
         *
         * @param name The name of the entity to manage.
         * @param data The data required to create the entity.
         * @return a RollingFileManager.
         */
        @Override
        public FoundationRollingRandomAccessFileManager createManager(final String name, final FactoryData data) {
            final File file = new File(name);
            final File parent = file.getParentFile();
            if (null != parent && !parent.exists()) {
                parent.mkdirs();
            }

            if (!data.append) {
                file.delete();
            }
            final long size = data.append ? file.length() : 0;
            final long time = file.exists() ? file.lastModified() : System.currentTimeMillis();

            RandomAccessFile raf = null;
            try {
                raf = new RandomAccessFile(name, "rw");
                if (data.append) {
                    final long length = raf.length();
                    LOGGER.trace("RandomAccessFile {} seek to {}", name, length);
                    raf.seek(length);
                } else {
                    LOGGER.trace("RandomAccessFile {} set length to 0", name);
                    raf.setLength(0);
                }
                return new FoundationRollingRandomAccessFileManager(raf, name, data.pattern, new DummyOutputStream(), data.append,
                        data.immediateFlush, data.bufferSize, size, time, data.policy, data.strategy, data.advertiseURI,
                        data.layout);
            } catch (final IOException ex) {
                LOGGER.error("Cannot access RandomAccessFile {}) " + ex);
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (final IOException e) {
                        LOGGER.error("Cannot close RandomAccessFile {}", name, e);
                    }
                }
            }
            return null;
        }
    }
}
