package eu.riscoss.agent.usecases;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.agent.UseCase;
import eu.riscoss.agent.Workflow;

public class UCTreeList implements UseCase {
	
	public static void main( String[] args ) {

		
		try {
			new UCTreeList().run( new RiscossRESTClient( "http://127.0.0.1:8888" ) );
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		
	
	}

	@Override
	public void run( RiscossRESTClient rest ) throws Exception {
		
		rest.login( "admin", "admin" );
		
		String string = rest.domain( "Cycles_Domain" ).entities().search( "" );
		
		System.out.println( string );
		
		List<JsonObject> list  = new Gson().fromJson( string, new TypeToken<List<JsonObject>>() {}.getType() );
		
		for( JsonObject o : list ) {
			print( o, "" );
		}
		
	}
	
	void print( JsonObject o, String prefix ) {
		
		System.out.println( prefix + o.get( "name" ) );
		
		JsonArray children = o.get( "children" ).getAsJsonArray();
		
		for( int i = 0; i < children.size(); i++ ) {
			print( children.get( i ).getAsJsonObject(), prefix + "  " );
		}
		
	}
	
}
