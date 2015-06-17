package eu.riscoss.client;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

public class JsonModelList {
	
	JSONValue response;
	
	public JsonModelList( JSONValue response ) {
		this.response = response;
	}
	
	public int getModelCount() {
		return response.isArray().size();
	}
	
	public ModelInfo getModelInfo( int index ) {
		JSONObject o = (JSONObject)response.isArray().get( index );
		return new ModelInfo( 
				o.get( "name" ).isString().stringValue() );
	}
}
