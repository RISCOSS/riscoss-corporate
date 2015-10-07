package eu.riscoss.agent.tasks;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import eu.riscoss.agent.RiscossRESTClient;

public class EnsureLayerStructure implements TestTask {
	
	String domain;
	String[] layers;
	
	Gson gson = new Gson();
	
	public EnsureLayerStructure( String domain, String[] layers ) {
		this.domain = domain;
		this.layers = layers;
	}
	
	@Override
	public void execute( RiscossRESTClient rest ) {	// Ensure that two layers exist in the domain: Project and OSSComponent
		List<Map<String,String>> remoteLayers = 
				gson.fromJson(rest.domain( domain ).layers().list(), new TypeToken<List<Map<String,String>>>() {}.getType() );
		{
			boolean[] found = new boolean[layers.length];
			for( int i = 0; i < found.length; i++ ) found[i] = false;
			for( int i = 0; i < layers.length; i++ ) {
				String layer = layers[i];
				for( Map<String,String> entry : remoteLayers ) {
					if( layer.equals( entry.get( "name" ) ) ) found[i] = true;
				}
			}
			
			for( int i = 0; i < layers.length; i++ ) {
				if( found[i] == false ) {
					if( i == 0 )
						rest.domain( domain ).layers().create( layers[i] );
					else
						rest.domain( domain ).layers().create( layers[i], layers[i-1] );
				}
			}
			
			remoteLayers = 
					gson.fromJson(rest.domain( domain ).layers().list(), new TypeToken<List<Map<String,String>>>() {}.getType() );
			
			for( int i = 0; i < layers.length; i++ ) {
				String layer = layers[i];
				boolean b = false;
				for( Map<String,String> entry : remoteLayers ) {
					if( layer.equals( entry.get( "name" ) ) ) b = true;
				}
				if( b == false ) throw new RuntimeException( "Unable to create layer " + layer );
			}
		}
	}
	
}
