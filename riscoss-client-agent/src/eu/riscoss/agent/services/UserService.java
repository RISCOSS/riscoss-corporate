package eu.riscoss.agent.services;

import eu.riscoss.agent.RiscossRESTClient;

public class UserService extends RESTService {
	
	public UserService( RiscossRESTClient rest ) {
		super( rest );
	}

	public void register( String username, String pwd ) {
		rest.post( "auth/register" )
			.header( "username", username )
			.header( "password", pwd )
			.send();
	}
	
	public String getUsername() {
		return rest.get( "auth/username" ).send();
	}
	
	public String listDomains( String username ) {
		return rest.get( "admin/domains/public" ).param( "username", username ).send();
	}

}
