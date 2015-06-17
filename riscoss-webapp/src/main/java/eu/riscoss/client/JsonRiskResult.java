package eu.riscoss.client;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

import eu.riscoss.shared.ChunkDataType;

public class JsonRiskResult {
	
	JSONObject json;
	
	
	public JsonRiskResult(JSONObject v) {
		this.json = v;
	}

	public String getChunkId() {
		return json.get( "id" ).isString().stringValue();
	}
	
	public String getDatatype() {
		return json.get( "datatype" ).isString().stringValue();
	}
	
	public ChunkDataType getDataType() {
		return ChunkDataType.valueOf( json.get( "datatype" ).isString().stringValue().toUpperCase() );
	}

	public String getDescription() {
		if( json.get( "description" ) == null ) return "";
		if( json.get( "descirption" ).isString() == null ) return "";
		return json.get( "description" ).isString().stringValue();
	}
	
	public String getDistributionString() {
		if( !"distribution".equals( getDatatype() ) ) return "";
		JSONArray array = json.get("value").isArray();
		String ret = "";
		String sep = "";
		for( int i = 0; i < array.size(); i++ ) {
			ret = ret + sep + array.get(i).isString().stringValue();
			sep =";";
		}
		return ret;
	}
	
}
