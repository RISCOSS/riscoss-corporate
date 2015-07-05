package eu.riscoss.shared;

public class RoleInfo {
	
	String name;
	
	public RoleInfo() {
		this( "" );
	}
	
	public RoleInfo( String name ) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
}
