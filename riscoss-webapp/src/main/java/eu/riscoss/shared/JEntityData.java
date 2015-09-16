package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

public class JEntityData {
	
	public String					name = "";
	public String					layer = null;
	public List<String>				parents = new ArrayList<>();
	public List<String>				children = new ArrayList<>();
	public List<String>				rdcs = new ArrayList<>();
	public List<JRiskNativeData>	userdata = new ArrayList<>();
	
}
