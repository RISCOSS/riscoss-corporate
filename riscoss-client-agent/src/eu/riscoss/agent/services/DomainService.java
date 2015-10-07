package eu.riscoss.agent.services;

import eu.riscoss.agent.RiscossRESTClient;

public class DomainService extends RESTService {
	
	public DomainService( RiscossRESTClient rest, String name) {
		super( rest );
		this.rest.set( "domain", name );
	}

	public LayersService layers() {
		return new LayersService( this.rest );
	}

	public LayerService layer( String layer ) {
		return new LayerService( this.rest, layer );
	}
	
	public EntityService entity( String entity ) {
		return new EntityService( this.rest, entity );
	}
	
	public EntitiesService entities() {
		return new EntitiesService( rest );
	}

	public ModelsService models() {
		return new ModelsService( rest );
	}

	public RCSService rcs() {
		return new RCSService( rest );
	}

	public AnalysisService analysis() {
		return new AnalysisService( rest );
	}
	
}
