package eu.riscoss.agent.services;

import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.agent.RiscossRESTClient.Sender;

public class RASService extends RESTService {

	public RASService(RiscossRESTClient rest, String rasId ) {
		super(rest);
		this.rest.set( "rasId", rasId );
	}
	
	public String getId() {
		return rest.get( "rasId", "" );
	}
	
	public String basePath() {
		return "analysis/" + getDomain() + "/session/" + getId();
	}
	
	public String execute() {
		return rest.post( basePath() + "/newrun" ).send();
	}

	public String applyMitigationTechnique( String mtName, String json ) {
		return rest.post( basePath() + "/mt/" + mtName + "/apply" ).send( json );
	}

	public String getMitigationTechniqueParams( String mtName ) {
		return rest.get( basePath() + "/mt/" + mtName + "/params" ).send();
	}

	public Sender get( String service ) {
		return rest.get( basePath() + service );
	}
	
}
