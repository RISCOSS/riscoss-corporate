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
