package zotmc.tomahawk.util;

public class UnknownFieldException extends RuntimeException {

	public UnknownFieldException() { }

	public UnknownFieldException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownFieldException(String message) {
		super(message);
	}

	public UnknownFieldException(Throwable cause) {
		super(cause);
	}

}
