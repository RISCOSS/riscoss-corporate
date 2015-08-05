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
		
		public String getType() {
			JSONValue val = json.get( "type" );
			if( val == null ) return "";
			if( val.isString() == null ) return "";
			return val.isString().stringValue();
		}
		
		public String getDataType() {
			JSONValue val = json.get( "datatype" );
			if( val == null ) return "";
			if( val.isString() == null ) return "";
			return val.isString().stringValue();
		}
		
		public String getDate() {
			JSONValue val = json.get( "date" );
			if( val == null ) return "";
			if( val.isString() == null ) return "";
			return val.isString().stringValue();
		}
		
		public String getOrigin() {
			JSONValue val = json.get( "origin" );
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
