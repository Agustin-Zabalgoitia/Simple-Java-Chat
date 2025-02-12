package general;

public class Constants {
	/**
	 * This class serves only as a repository for constants used both, in the client, and in the server
	 */
	public static String SERVER_SEPARATOR_SYMBOL = "|";
	public static String SERVER_FIRST_SYMBOL = "@";
	public static String USERNAME_SEPARATOR = ":";
	public static String LIST_SEPARATOR = ",";
	public static String NEW_LINE = "\n";
	public static String USER_LIST_MESSAGE = SERVER_FIRST_SYMBOL + "USERLIST" + SERVER_SEPARATOR_SYMBOL;
	public static String ACCEPTED_NICKNAME_MESSAGE = SERVER_FIRST_SYMBOL + "ACCEPTEDNICKNAME" + SERVER_SEPARATOR_SYMBOL;
	
	//Suppress default constructor for non-instantiability
	private Constants() {
		throw new AssertionError();
	}
}
