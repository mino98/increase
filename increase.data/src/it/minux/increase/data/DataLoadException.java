package it.minux.increase.data;

public class DataLoadException 
	extends Exception
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3224643993498719303L;

	public DataLoadException(String message, Exception cause) {
		super(message, cause);
	}
	
	public DataLoadException(String message) {
		super(message);
	}

}
