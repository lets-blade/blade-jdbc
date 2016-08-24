package com.blade.jdbc.exception;

public class InitException extends RuntimeException{
	
	private static final long serialVersionUID = 6552230459370143659L;

	public InitException() {
        super();
    }

    public InitException(String message) {
        super(message);
    }

    public InitException(String message, Throwable cause) {
        super(message, cause);
    }

    public InitException(Throwable cause) {
        super(cause);   
    }
}
