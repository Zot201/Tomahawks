package zotmc.tomahawk.util;

public class UnknownClassException extends RuntimeException {

	public UnknownClassException() { }

	public UnknownClassException(String message, Throwable cause) {
		super(message, cause);
	}

	public UnknownClassException(String message) {
		super(message);
	}

	public UnknownClassException(Throwable cause) {
		super(cause);
	}

}
