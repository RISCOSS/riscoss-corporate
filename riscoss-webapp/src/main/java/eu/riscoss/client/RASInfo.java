package eu.riscoss.client;

import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;

public class RASInfo {
	
	String id;
	String name;
	
	public RASInfo( JSONValue json ) {
		try {
			id = json.isObject().get( "id" ).isString().stringValue();
			if( json.isObject().get( "name" ) != null )
				name = json.isObject().get( "name" ).isString().stringValue();
			else
				name = id;
		}
		catch( Exception ex ) {
			Window.alert( ex.getMessage() );
		}
	}

	public String getId() {
		return this.id;
	}
	
	public String getName() {
		return this.name;
	}
}
