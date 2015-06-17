package eu.riscoss.client;

import java.util.Set;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

public class JsonRDCMap {
	
	JSONObject json;
	
	public JsonRDCMap( JSONValue jsonValue ) {
		this.json = jsonValue.isObject();
		if( this.json == null ) {
			this.json = new JSONObject();
		}
	}

	public Set<String> keySet() {
		return this.json.keySet();
	}
	
	public boolean existsRDC( String rdcName ) {
		if( json.get( rdcName ) == null ) return false;
		if( json.get( rdcName ).isObject() == null ) return false;
		return true;
	}
	
	JSONObject requestRDCObject( String rdcName ) {
		JSONValue v = json.get( rdcName );
		if( v == null ) {
			v = new JSONObject();
			json.put( rdcName, v );
		}
		else if( v.isObject() == null ) {
			v = new JSONObject();
			json.put( rdcName, v );
		}
		return json.get( rdcName ).isObject();
	}
	
	public JSONArray parameters( String key ) {
		JSONObject o = requestRDCObject( key );
//		Window.alert( "" + o );
		if( o.get( "params" ).isArray() == null ) return new JSONArray();
		return o.get( "params" ).isArray();
	}

	public String getParamterName( String rdcName, int i ) {
		JSONArray array = parameters( rdcName );
		JSONObject o = array.get( i ).isObject();
		return o.get( "name" ).isString().stringValue();
	}
	
}
