package eu.riscoss.client;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;

import eu.riscoss.client.JsonRiskDataList.RiskDataItem;

public class JsonEntitySummary {
	
	public class UserData {
		
		JSONArray data;
		
		public UserData( JSONValue val ) {
			if( val != null ) {
				data = val.isArray();
			}
			if( data == null ) {
				data = new JSONArray();
			}
		}

		public int size() {
			return data.size();
		}
		
		public RiskDataItem get( int i ) {
			return new RiskDataItem( data.get( i ).isObject() );
		}
		
	}
	
	
	JSONObject json;
	UserData userData;
	
	
	public JsonEntitySummary( JSONValue val ) {
		if( val != null ) {
			json = val.isObject();
		}
		if( json == null ) json = new JSONObject();
		
		userData = new UserData( json.get( "userdata" ) );
	}
	
	public String getEntityName() {
		return json.get( "name" ).isString().stringValue();
	}

	public String getLayer() {
		return json.get( "layer" ).isString().stringValue();
	}
	
	public UserData getUserData() {
		return userData;
	}

	public JSONArray getRDCs() {
		return json.get( "rdcs" ).isArray();
	}
	
	public String getRDCString() {
		String str = "";
		{
			String sep = "";
			for( int i = 0; i < getRDCs().size(); i++ ) {
				str = str + sep + getRDCs().get( i ).isString().stringValue();
				sep = ", ";
			}
		}
		return str;
	}

	public JSONArray getParentList() {
		if( json.get( "parents" ) == null ) return new JSONArray();
		JSONArray a = json.get( "parents" ).isArray();
		if( a == null ) return new JSONArray();
		return a;
	}

	public JSONArray getChildrenList() {
		if( json.get( "children" ) == null ) return new JSONArray();
		JSONArray a = json.get( "children" ).isArray();
		if( a == null ) return new JSONArray();
		return a;
	}
	
}
