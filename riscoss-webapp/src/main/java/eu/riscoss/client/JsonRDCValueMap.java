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

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class JsonRDCValueMap {
	
	JSONObject root;
	
	public JsonRDCValueMap( JSONValue json ) {
		this.root = json.isObject();
		if( root == null )
			root = new JSONObject();
	}
	
	public boolean isEnabled( String rdcName ) {
		JSONValue v = root.get( rdcName );
		if( v == null ) return false;
		if( v.isObject() == null ) return false;
		JSONObject o = v.isObject();
		if( o.get( "enabled" ) == null ) return false;
		if( o.get( "enabled" ).isBoolean() == null ) return false;
		return ( o.get( "enabled" ).isBoolean().booleanValue() == true );
	}
	
	public void enableRDC( String rdcName, Boolean value ) {
		if( root.get( rdcName ) == null ) {
			root.put( rdcName, new JSONObject() );
		}
		root.get( rdcName ).isObject().put( "enabled", JSONBoolean.getInstance( value ) );
	}

	public JSONObject getJson() {
		return this.root;
	}
	
	public void set( String rdcName, String parName, String value ) {
		if( root.get( rdcName ) == null ) {
			root.put( rdcName, new JSONObject() );
		}
		if( root.get( rdcName ).isObject().get( "params" ) == null ) {
			root.get( rdcName ).isObject().put( "params", new JSONObject() );
		}
		JSONObject params = root.get( rdcName ).isObject().get( "params" ).isObject();
		if( params == null ) {
			params = new JSONObject();
			root.get( rdcName ).isObject().put( "params", params );
		}
		params.put( parName, new JSONString( value ) );
	}

	public String get( String rdcName, String parName, String def ) {
		if( root.get( rdcName ) == null ) {
			root.put( rdcName, new JSONObject() );
		}
		JSONObject params = root.get( rdcName ).isObject().get( "params" ).isObject();
		if( params == null ) {
			params = new JSONObject();
			root.get( rdcName ).isObject().put( "params", params );
		}
		JSONValue v = params.get( parName );
		if( v == null ) return def;
		if( v.isString() == null ) return def;
		return v.isString().stringValue();
	}
}
