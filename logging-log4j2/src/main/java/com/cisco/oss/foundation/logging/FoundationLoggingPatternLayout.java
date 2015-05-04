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

package com.cisco.oss.foundation.logging;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.RegexReplacement;

import java.nio.charset.Charset;
import java.util.Map;

/**
 * Class extends EnhancedPatternLayout to allow enabling and disabling of stack
 * trace output.
 *
 * @author Yair Ogen
 * @author Jethro Revill
 */
public final class FoundationLoggingPatternLayout extends AbstractStringLayout {

    private PatternLayout patternLayout = null;

    public FoundationLoggingPatternLayout(final Configuration config, final RegexReplacement replace, final String pattern,
                                           final Charset charset, final boolean alwaysWriteExceptions, final boolean noConsoleNoAnsi,
                                           final String header, final String footer) {
        super(charset, toBytes(header, charset), toBytes(footer, charset));
        patternLayout = PatternLayout.createLayout(pattern,config,replace,charset,alwaysWriteExceptions,noConsoleNoAnsi,header,footer);
    }

    private static byte[] toBytes(String str, Charset charset) {
        if (str != null) {
            return str.getBytes(charset != null ? charset : Charset.defaultCharset());
        }
        return null;
    }

    @Override
    public byte[] getHeader() {
        return patternLayout.getHeader();
    }

    @Override
    public byte[] getFooter() {
        return patternLayout.getFooter();
    }

    /**
     * Gets the conversion pattern.
     *
     * @return the conversion pattern.
     */
    public String getConversionPattern() {
        return patternLayout.getConversionPattern();
    }

    /**
     * PatternLayout's content format is specified by:<p/>
     * Key: "structured" Value: "false"<p/>
     * Key: "formatType" Value: "conversion" (format uses the keywords supported by OptionConverter)<p/>
     * Key: "format" Value: provided "conversionPattern" param
     * @return Map of content format keys supporting PatternLayout
     */
    @Override
    public Map<String, String> getContentFormat()
    {
        return patternLayout.getContentFormat();
    }

    /**
     * Formats a logging event to a writer.
     *
     *
     * @param event logging event to be formatted.
     * @return The event formatted as a String.
     */
    @Override
    public String toSerializable(final LogEvent event) {
        return patternLayout.toSerializable(event);
    }

    @Override
    public String toString() {
        return patternLayout.getConversionPattern();
    }


}
