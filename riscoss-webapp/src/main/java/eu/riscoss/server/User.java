package eu.riscoss.server;

public class User {
	
	String token = null;
	String username = "";
	String password = "";
	
	private static final class NotLoggedInUser extends User {
		
		public final boolean isLoggedIn() {
			return false;
		}
		
	}
	
	public static final User noUser = new NotLoggedInUser();
	
	public boolean isLoggedIn() {
		return true;
	}
	
}
