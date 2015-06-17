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
