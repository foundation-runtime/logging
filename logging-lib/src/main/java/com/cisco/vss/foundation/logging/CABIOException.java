package com.cisco.vss.foundation.logging;

/**
 * Indicates that an IO issue has arisen during operation of the
 * CAB logging library.
 * @author Jethro Revill
 */
public final class CABIOException extends RuntimeException {

    /**
     * Serialization UID.
     */
    private static final long serialVersionUID = 7109117737252296489L;

    /**
     * Constructs a new exception with the specified detail message.
     *
     * @param message the message
     */
    CABIOException(final String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     *
     * @param message the message
     * @param cause the cause of the exception
     */
    CABIOException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new exception with the specified cause.
     *
     * @param cause the cause of the exception
     */
    CABIOException(final Throwable cause) {
        super(cause);
    }
}
