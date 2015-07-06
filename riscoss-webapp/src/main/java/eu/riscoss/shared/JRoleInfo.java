package eu.riscoss.shared;

public class JRoleInfo {
	
	String name;
	
	public JRoleInfo() {
		this( "" );
	}
	
	public JRoleInfo( String name ) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
}
