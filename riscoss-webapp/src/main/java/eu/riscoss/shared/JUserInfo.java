package eu.riscoss.shared;

public class JUserInfo {
	
	String username = "";
	
	public JUserInfo() {
		this( "" );
	}
	
	public JUserInfo(String user) {
		this.username = user;
	}
	
	public String getUsername() {
		return this.username;
	}
	
}
