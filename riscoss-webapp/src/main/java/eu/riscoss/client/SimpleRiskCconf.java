package eu.riscoss.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

public class SimpleRiskCconf {
	
	JSONObject json;
	
	public SimpleRiskCconf( JSONValue response ) {
		
		if( response == null ) response = new JSONObject();
		
		this.json = response.isObject();
		
		if( this.json == null ) {
			this.json = new JSONObject();
		}
		
	}

	public String getName() {
		return json.get( "name" ).isString().stringValue();
	}

	public int getLayerCount() {
		try {
			return json.get( "layers" ).isArray().size();
		}
		catch( Exception ex ) {
			return 0;
		}
	}

	public String getLayer( int index ) {
		try {
			return json.get( "layers" ).isArray().get( index ).isObject().get( "name" ).isString().stringValue();
		}
		catch( Exception ex ) {
			return null;
		}
	}
	
	public void setModelList( String layer, List<String> models ) {
		try {
			JSONArray jmodels = new JSONArray();
			for( int i = 0; i < models.size(); i++ ) {
				jmodels.set( jmodels.size(), new JSONString( models.get( i ) ) );
			}
			JSONArray array = json.get( "layers" ).isArray();
			for( int i = 0; i < array.size(); i++ ) {
				JSONObject o = array.get( i ).isObject();
				if( layer.equals( o.get( "name" ).isString().stringValue() ) ) {
					o.put( "models", jmodels );
					return;
				}
			}
		}
		catch( Exception ex ) {
			
		}
	}
	
	public ArrayList<String> getModelList( String layer ) {
		
		ArrayList<String> list = new ArrayList<String>();
		
		JSONArray array = json.get( "layers" ).isArray();
		for( int i = 0; i < array.size(); i++ ) {
			JSONObject o = array.get( i ).isObject();
			if( layer.equals( o.get( "name" ).isString().stringValue() ) ) {
				JSONArray jmodels = o.get( "models" ).isArray();
				for( int m = 0; m < jmodels.size(); m++ ) {
					list.add( jmodels.get( m ).isString().stringValue() );
				}
				return list;
			}
		}
		
		
		return list;
		
	}

	public JSONObject getJson() {
		return this.json;
	}
}
