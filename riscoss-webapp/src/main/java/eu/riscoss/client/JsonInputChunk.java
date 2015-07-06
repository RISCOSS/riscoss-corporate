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

import eu.riscoss.shared.EChunkDataType;

public class JsonInputChunk {
	
	private JSONObject json;

	public JsonInputChunk( JSONObject o ) {
		this.json = o;
		if( this.json == null )
			this.json = new JSONObject();
	}

	public EChunkDataType getType() {
		
		JSONValue val = json.get( "type" );
		if( val == null ) return EChunkDataType.NaN;
		if( val.isString() == null ) return EChunkDataType.NaN;
		
		String string = val.isString().stringValue();
		
		EChunkDataType type = EChunkDataType.valueOf( string );
		if( type == null ) type = EChunkDataType.NaN;
		
		return type;
	}

	public String[] getDistributionValues() {
		String value = null;
		try {
			value = json.get( "value" ).isString().stringValue();
		}
		catch( Exception ex ) {
			return null;
		}
		return value.split( "[;]" );
	}
	
}
