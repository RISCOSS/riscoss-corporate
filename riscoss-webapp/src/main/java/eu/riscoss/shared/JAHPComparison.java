package eu.riscoss.shared;

import org.codehaus.jackson.annotate.JsonIgnore;

public class JAHPComparison {
	
	public int value = 1;
	
	String[] ids = new String[] { "", "" };
	
	@JsonIgnore
	public String getId1() {
		return ids[0];
	}
	
	@JsonIgnore
	public String getId2() {
		return ids[1];
	}
	
	@JsonIgnore
	public void setId1( String id ) {
		ids[0] = id;
	}
	
	@JsonIgnore
	public void setId2( String id ) {
		ids[1] = id;
	}
	
}