package eu.riscoss.client;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;

import eu.riscoss.shared.ChunkDataType;

public class JsonInputChunk {
	
	private JSONObject json;

	public JsonInputChunk( JSONObject o ) {
		this.json = o;
		if( this.json == null )
			this.json = new JSONObject();
	}

	public ChunkDataType getType() {
		
		JSONValue val = json.get( "type" );
		if( val == null ) return ChunkDataType.NaN;
		if( val.isString() == null ) return ChunkDataType.NaN;
		
		String string = val.isString().stringValue();
		
		ChunkDataType type = ChunkDataType.valueOf( string );
		if( type == null ) type = ChunkDataType.NaN;
		
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
