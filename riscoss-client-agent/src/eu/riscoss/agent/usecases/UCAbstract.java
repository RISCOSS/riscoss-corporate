package eu.riscoss.agent.usecases;

import java.util.HashMap;
import java.util.Map;

import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.agent.UseCase;
import eu.riscoss.agent.Workflow;
import eu.riscoss.agent.tasks.EnsureDomainExistence;
import eu.riscoss.agent.tasks.EnsureEntityExistence;
import eu.riscoss.agent.tasks.EnsureLayerStructure;
import eu.riscoss.agent.tasks.SelectDomain;

public abstract class UCAbstract implements UseCase {
	
	String domain = "Playground";
	String[] layers = new String[] { "OSSComponent" };
	Map<String,String[]> entities = new HashMap<>();
	Map<String,String[]> children = new HashMap<>();
	
	
	public UCAbstract() {
		
	}
	
	protected void initDomain( String name ) {
		this.domain = name;
	}
	
//	protected void initLayers( String[] layers ) {
//		this.layers = layers;
//	}
	
	protected void initLayers( String ... layers ) {
		this.layers = layers;
	}
	
	protected void initEntities( String layer, String[] entities ) {
		this.entities.put( layer, entities );
	}
	
	@Override
	public void run( RiscossRESTClient rest ) throws Exception {
		
		rest.login( "admin", "admin" );
		
		Workflow w = new Workflow( rest );
		
		w.execute( new EnsureDomainExistence( domain ) );
		w.execute( new SelectDomain( domain ) );
		w.execute( new EnsureLayerStructure( domain, layers ) );
		
		for( String layer : layers ) {
			String[] entities = this.entities.get( layer );
			if( entities == null ) continue;
			for( String entity : entities ) {
				w.execute( new EnsureEntityExistence( layer,  entity ) );
			}
		}
		
		runUC( w );
		
	}
	
	public String getDomain() {
		return this.domain;
	}
	
	protected abstract void runUC( Workflow w );
	
}
