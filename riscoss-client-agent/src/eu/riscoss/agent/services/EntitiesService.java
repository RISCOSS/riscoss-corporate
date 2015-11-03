package eu.riscoss.agent.services;

import com.google.gson.Gson;

import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.shared.JEntityData;

public class EntitiesService extends RESTService {
	
	Gson gson = new Gson();
	
	public EntitiesService( RiscossRESTClient rest ) {
		super(rest);
	}

	public String list() {
		return rest.get( "entities/" + rest.get( "domain", "" ) + "/list/" + encode( rest.get( "layer", "" ) ) ).send();
	}

	public void create( String entity, String layer ) {
		rest.post( "entities/" + getDomain() + "/create" )
		.param( "name", entity ).param( "layer", layer ).send();
	}
	
	public String search( String query ) {
		return rest.get( "entities/" + rest.get( "domain", "" ) + "/search" ).param( "q", query ).send();
	}
	
	public String search( String layer, String partialName, String from, String max, String h ) {
		return rest.get( "entities/" + rest.get( "domain", "" ) + "/search" ).
				param( "query", partialName ).
				param( "from", from ).
				param( "max", max ).
				param( "h", h ).send();
	}
	
	public boolean exists( String entity ) {
		JEntityData data = gson.fromJson( rest.domain( getDomain() ).entity( entity ).getData(), JEntityData.class );
		return data.layer != null;
	}

	public void delete( String entity ) {
		rest.delete( "entities/" + getDomain() + "/" + entity + "/delete" ).send();
	}
	
}
