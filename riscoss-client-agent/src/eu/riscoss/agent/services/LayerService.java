package eu.riscoss.agent.services;

import eu.riscoss.agent.RiscossRESTClient;

public class LayerService extends RESTService {

	public LayerService( RiscossRESTClient rest, String name ) {
		super(rest);
		this.rest.set( "layer", name );
	}

	public EntitiesService entities() {
		return new EntitiesService( this.rest );
	}
	
}
