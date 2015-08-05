package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

public class JLayerContextualInfoElement {

	String				id			= new String();
	String				name 		= new String();
	String				description	= new String();
	String				defval;
	String				type		= new String();
	List<String> 		info		= new ArrayList<>();	
	
	public JLayerContextualInfoElement() {
		
	}
	
	public JLayerContextualInfoElement(String id, String name, String description, String defval, String type) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.defval = defval;
		this.type = type;
	}
	
	public String getId() {
		return this.id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDefval() {
		return this.defval;
	}
	
	public void setDefval(String defval) {
		this.defval = defval;
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
