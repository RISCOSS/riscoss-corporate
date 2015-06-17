package eu.riscoss.client.riskanalysis;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;


public class JsonRiskAnalysis {
	
	JSONObject json;
	
	public JsonRiskAnalysis(JSONValue response) {
		this.json = response.isObject();
		if( json == null ) json = new JSONObject();
	}

	private String getAttribute( JSONObject o, String key, String def ) {
		try {
			return o.get( key ).isString().stringValue();
		}catch( Exception ex ) {
			return def;
		}
	}
	
	public String getID() {
		return getAttribute( json, "id", "" );
	}
	
	public String getRC() {
		return getAttribute( json, "rc", "" );
	}
	
	public String getTarget() {
		return getAttribute( json, "target", "" );
	}
	
	public String getDate() {
		return getAttribute( json, "timestamp", "" );
	}
	
}
