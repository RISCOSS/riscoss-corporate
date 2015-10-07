package eu.riscoss.agent.services;

import eu.riscoss.agent.RiscossRESTClient;


public class LayersService extends RESTService {
	
	public LayersService( RiscossRESTClient rest ) {
		super( rest );
	}

	public void create( String name, String parent ) {
		rest.post( "layers/" + rest.get( "domain", "" ) + "/create" ).
		param("name",name).
		param("parent",parent).send();
	}
	
	public void create( String name ) {
		create( name, "$leaf" );
	}

	public void delete( String name ) {
		rest.delete( "layers/" + rest.get( "domain", "" ) + "/delete" ).
			param("name",name).send();
	}

	public String list() {
		return rest.get( "layers/" + rest.get( "domain", "" ) + "/list" ).send();
	}

}
