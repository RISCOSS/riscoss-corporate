package eu.riscoss.client;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

public class JsonRiskDataList {
	
	public static class RiskDataItem {

		private JSONObject json;

		public RiskDataItem(JSONObject object) {
			this.json = object;
			if( this.json == null )
				this.json = new JSONObject();
		}
		
		public String getId() {
			JSONValue val = json.get( "id" );
			if( val == null ) return "";
			if( val.isString() == null ) return "";
			return val.isString().stringValue();
		}
		
		public String getValue() {
			JSONValue val = json.get( "value" );
			if( val == null ) return "";
			if( val.isString() == null ) return "";
			return val.isString().stringValue();
		}
		
	}
	
	private JSONObject json;
	private JSONArray array;
	
	public JsonRiskDataList( JSONValue value ) {
		this.json = value.isObject();
		if( json == null ) json = new JSONObject();
		if( json.get( "list" ) != null )
			array = json.get( "list" ).isArray();
		if( array == null )
			array = new JSONArray();
	}

	public int size() {
		return array.size();
	}
	
	public RiskDataItem get( int index ) {
		if( index >= size() ) return null;
		return new RiskDataItem( array.get( index ).isObject() );
	}
}
