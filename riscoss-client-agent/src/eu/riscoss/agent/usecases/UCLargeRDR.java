package eu.riscoss.agent.usecases;

import java.util.List;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.agent.UseCase;
import eu.riscoss.agent.Workflow;
import eu.riscoss.agent.tasks.EnsureDomainExistence;
import eu.riscoss.agent.tasks.EnsureEntityExistence;
import eu.riscoss.agent.tasks.EnsureLayerStructureNew;
import eu.riscoss.agent.tasks.EnsureParent;
import eu.riscoss.agent.tasks.ListEntities;
import eu.riscoss.agent.tasks.SearchEntities;
import eu.riscoss.agent.tasks.SelectDomain;

public class UCLargeRDR implements UseCase {

	Gson gson = new Gson();
	
	String domain = "LargeRDR";
	
	public static void main( String[] args ) throws Exception {
		new UCLargeRDR().run( new RiscossRESTClient() );
	}
	
	public UCLargeRDR() {}
	
	public UCLargeRDR( String domain ) {
		this.domain = domain;
	}
	
	@Override
	public void run( RiscossRESTClient rest ) throws Exception {
		
		rest.login( "admin", "admin" );
		
		Workflow w = new Workflow( rest );
		
		w.execute( new EnsureDomainExistence( domain ) );
		w.execute( new SelectDomain( domain ) );
		w.execute( new EnsureLayerStructureNew( new String[] { "OSSComponent" } ) );
		
		w.execute( new ListEntities() );
		
		List<JsonObject> list  = new Gson().fromJson( w.getContext().get( "entities", "" ), new TypeToken<List<JsonObject>>() {}.getType() );
		
		if( list != null ) {
			for( JsonObject o : list ) {
				rest.domain( domain ).entities().delete( o.get( "name" ).getAsString() );
			}
		}
		
//		Thread.sleep( 1000 );
		
		w.execute( new ListEntities() );
		list  = new Gson().fromJson( w.getContext().get( "entities", "" ), new TypeToken<List<JsonObject>>() {}.getType() );
		if( list != null ) {
			if( list.size() > 0 ) {
				for( JsonObject o : list ) {
					System.out.println( o.get( "name" ).getAsString() + " = " + o.get( "layer" ).getAsString() );
				}
				return;
			}
		}
		
		
		for( int c = 0; c < 500; c++ ) {
			w.execute( new EnsureEntityExistence( "OSSComponent", "c" + c ) );
		}
		
		w.execute( new ListEntities() );
		
		list  = new Gson().fromJson( w.getContext().get( "entities", "" ), new TypeToken<List<JsonObject>>() {}.getType() );
		
		Random r = new Random( 11 ); //System.currentTimeMillis() );
		
		for( int i = 0; i < 500; i++ ) {
			
			System.out.println( i );
			
			int pnum = r.nextInt( list.size() );
			int cnum = r.nextInt( list.size() );
			
			w.execute( new EnsureParent( 
					list.get( cnum ).get("name" ).getAsString(),
					list.get( pnum ).get("name" ).getAsString() ) );
			
		}
		
		w.execute( new SearchEntities( "", "", "", "", "true" ) );
		
		System.out.println( w.getContext().get( "entities", "" ) );
		
		rest.logout();
		
	}
	
}
