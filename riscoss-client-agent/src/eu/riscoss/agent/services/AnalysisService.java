package eu.riscoss.agent.services;

import eu.riscoss.agent.RiscossRESTClient;

public class AnalysisService extends RESTService {

	public AnalysisService(RiscossRESTClient rest) {
		super(rest);
	}

	public String createSession( String entity, String rc ) {
		return rest.post( "analysis/" + getDomain() + "/session/create" )
				.param( "rc", rc )
				.param( "target", entity )
				.param( "name", entity + " - " + rc )
				.send();
	}

	public RASService session( String rasId ) {
		return new RASService( this.rest, rasId );
	}
	
}
