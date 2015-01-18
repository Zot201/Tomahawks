package zotmc.tomahawk.transform;

public class MissingMethodException extends RuntimeException {
	
	private static final long serialVersionUID = -8578461050899253705L;
	
	public MissingMethodException(MethodPredicate target) {
		super(target.toString());
	}
	
}
