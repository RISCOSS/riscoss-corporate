package eu.riscoss.shared;

public class JPageInfo {
	
	String shortName = "No name";
	String pageName = "No name";
	String description = "";
	
	public JPageInfo() {}
	
	public JPageInfo( String shortName, String pageName, String description ) {
		this.shortName = shortName;
		this.pageName = pageName;
		this.description = description;
	}
	
}
