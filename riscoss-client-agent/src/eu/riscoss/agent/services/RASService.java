package eu.riscoss.agent.services;

import eu.riscoss.agent.RiscossRESTClient;

public class RASService extends RESTService {

	public RASService(RiscossRESTClient rest, String rasId ) {
		super(rest);
		this.rest.set( "rasId", rasId );
	}
	
	public String getId() {
		return rest.get( "rasId", "" );
	}

	public String execute() {
		return rest.post( "analysis/" + getDomain() + "/session/" + getId() + "/newrun" ).send();
	}
	
}
