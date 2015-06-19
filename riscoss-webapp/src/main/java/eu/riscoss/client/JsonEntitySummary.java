/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/**
 * @author 	Alberto Siena
**/

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
