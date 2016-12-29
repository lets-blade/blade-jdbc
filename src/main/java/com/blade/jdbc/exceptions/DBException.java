package com.blade.jdbc.exceptions;

import com.blade.jdbc.model.IEnum;

/**
 * 自定义异常类
 */
public class DBException extends RuntimeException {

    /** Exception code */
    protected String resultCode = "UN_KNOWN_EXCEPTION";

    /** Exception message */
    protected String resultMsg  = "Unknown exception";

    /**
     * Constructor
     */
    public DBException() {
        super();
    }

    /**
     * Instantiates a new DBException.
     *
     * @param e the e
     */
    public DBException(IEnum e) {
        super(e.getDesc());
        this.resultCode = e.getCode();
        this.resultMsg = e.getDesc();
    }

    /**
     * Instantiates a new DBException.
     *
     * @param e the e
     */
    public DBException(Throwable e) {
        super(e);
        this.resultMsg = e.getMessage();
    }

    /**
     * Constructor
     *
     * @param message the message
     */
    public DBException(String message) {
        super(message);
        this.resultMsg = message;
    }

    /**
     * Constructor
     *
     * @param code the code
     * @param message the message
     */
    public DBException(String code, String message) {
        super(message);
        this.resultCode = code;
        this.resultMsg = message;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMsg() {
        return resultMsg;
    }

    public void setResultMsg(String resultMsg) {
        this.resultMsg = resultMsg;
    }
}
