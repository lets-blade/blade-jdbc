package blade.jdbc.exception;

import blade.jdbc.Util;

public class DBException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 297141479834130415L;
	
	final String message;

	public DBException() {
		super();
		this.message = null;
	}

	public DBException(Throwable cause) {
		super(cause);
		this.setStackTrace(cause.getStackTrace());
		this.message = null;
	}

	public DBException(String message) {
		super(message);
		this.message = null;
	}

	public DBException(String message, Throwable cause) {
		super(message, cause);
		this.setStackTrace(cause.getStackTrace());
		this.message = null;
	}

	/**
	 *
	 * @param query
	 *            SQL query
	 * @param params
	 *            - array of parameters, can be null
	 * @param cause
	 *            real cause.
	 */
	public DBException(String query, Object[] params, Throwable cause) {
		StringBuilder sb = new StringBuilder(cause.toString()).append(", query: ").append(query);
		if (params != null && params.length > 0) {
			sb.append(", params: ");
			Util.join(sb, params, ", ");
		}
		message = sb.toString();
		setStackTrace(cause.getStackTrace());
		initCause(cause);
	}

	@Override
	public String getMessage() {
		return message == null ? super.getMessage() : message;
	}

}