package eu.riscoss.shared;

public class RASInfo {
	
	String id = "";
	String name = "";
	
	public RASInfo() {}
	
	public RASInfo( String id, String name ) {
		this.id = id;
		this.name = name;
	}

	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
	
}
