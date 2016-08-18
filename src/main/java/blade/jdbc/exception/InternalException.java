package blade.jdbc.exception;

public class InternalException extends RuntimeException {
	
	private static final long serialVersionUID = -1158624794510225077L;

	public InternalException(Throwable cause) {
		super(cause);
	}

	public InternalException(String message) {
		super(message);
	}
}
