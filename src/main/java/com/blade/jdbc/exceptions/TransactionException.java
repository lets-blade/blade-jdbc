package com.blade.jdbc.exceptions;

/**
 * Created by biezhi on 2017/3/15.
 */
public class TransactionException extends RuntimeException {

    public TransactionException() {
    }

    public TransactionException(String message) {
        super(message);
    }

    public TransactionException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionException(Throwable cause) {
        super(cause);
    }
}
