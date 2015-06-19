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
