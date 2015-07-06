package eu.riscoss.shared;

public class JRASInfo {
	
	String id = "";
	String name = "";
	
	public JRASInfo() {}
	
	public JRASInfo( String id, String name ) {
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
