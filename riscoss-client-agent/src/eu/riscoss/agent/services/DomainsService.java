package eu.riscoss.agent.services;

import eu.riscoss.agent.RiscossRESTClient;

public class DomainsService extends RESTService {

	public DomainsService(RiscossRESTClient rest) {
		super(rest);
	}

	public String list() {
		return rest.get( "admin/domains/public" ).send();
	}

	public void create( String domain ) {
		rest.post( "admin/domains/create" ).param( "name", domain ).send();
	}
	
}
