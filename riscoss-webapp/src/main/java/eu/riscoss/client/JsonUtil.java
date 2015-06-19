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

public class JsonUtil {
	
	static final JSONArray EMPTY_ARRAY = new JSONArray();
	static final JSONObject NO_OBJECT = new JSONObject();
	
	public static JSONArray getArray( JSONObject object, String key ) {
		if( object == null ) return EMPTY_ARRAY;
		JSONValue v = object.get( key );
		if( v == null ) return EMPTY_ARRAY;
		if( v.isArray() == null ) return EMPTY_ARRAY;
		return v.isArray();
	}

	public static JSONObject getObject( JSONValue val ) {
		if( val == null ) return NO_OBJECT;
		JSONObject object = val.isObject();
		if( object == null ) return NO_OBJECT;
		return object;
	}
	
	public static JSONObject getObject( JSONValue val, String key ) {
		if( val == null ) return NO_OBJECT;
		JSONObject object = val.isObject();
		if( object == null ) return NO_OBJECT;
		JSONValue v = object.get( key );
		if( v == null ) return NO_OBJECT;
		if( v.isObject() == null ) return NO_OBJECT;
		return v.isObject();
	}
	
	public static String getValue( JSONValue val, String key, String def ) {
		if( val == null ) return def;
		JSONObject object = val.isObject();
		if( object == null ) return def;
		JSONValue v = object.get( key );
		if( v == null ) return def;
		if( v.isString() == null ) return def;
		return v.isString().stringValue();
	}
	
	private JSONValue value;
	
	public JsonUtil( JSONValue val ) {
		this.value = val;
	}
	
}
