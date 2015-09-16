package eu.riscoss.shared;

import java.util.ArrayList;

public class JRiskConfiguration {
	
	public String name = null;
	public String type = "layered";
	public ArrayList<String> models = new ArrayList<String>();
	public ArrayList<JRiskConfigurationLayerInfo> layers = new ArrayList<JRiskConfigurationLayerInfo>();
	
}
