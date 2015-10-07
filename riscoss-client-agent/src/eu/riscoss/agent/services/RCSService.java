package eu.riscoss.agent.services;

import eu.riscoss.agent.RiscossRESTClient;

public class RCSService extends RESTService {

	public RCSService(RiscossRESTClient rest) {
		super(rest);
	}

	public String get( String name ) {
		return rest.get( "rcs/" + getDomain() + "/" + name + "/get_new" ).send();
	}

	public void create( String name ) {
		rest.post( "rcs/" + getDomain() + "/create" )
		.param( "name", name )
		.send();
	}

	public RCService rc( String name ) {
		return new RCService( rest, name );
	}
	
}
