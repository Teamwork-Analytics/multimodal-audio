package exceptions;

public class TDLUnavailableException extends Exception {

	public TDLUnavailableException() {
		super("Target Data Line has not been instantiated");
	}

	public TDLUnavailableException(String message) {
		super(message);
	}

}
