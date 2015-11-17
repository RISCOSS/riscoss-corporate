package eu.riscoss.agent.services;

import eu.riscoss.agent.RiscossRESTClient;

public class RDRService extends RESTService {

	public RDRService( RiscossRESTClient rest ) {
		super( rest );
	}
	
	public String store(String json){
		return rest.post( "rdr/" + getDomain() + "/store" ).header("Content-Type", "application/json").send(json);
	}
	
}
