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

	public String getName() {
		return getAttribute( json, "name", getID() );
	}
	
}
