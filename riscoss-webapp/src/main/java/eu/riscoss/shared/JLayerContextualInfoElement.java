package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

public class JLayerContextualInfoElement {

	String				name 		= new String();
	String				type		= new String();
	List<String> 		info		= new ArrayList<>();	
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return this.type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public List<String> getInfo() {
		return this.info;
	}
	
	public void setInfo(List<String> info) {
		this.info = info;
	}
	
}
