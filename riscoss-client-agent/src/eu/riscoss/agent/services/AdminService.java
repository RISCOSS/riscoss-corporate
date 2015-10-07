package eu.riscoss.agent.services;

import eu.riscoss.agent.RiscossRESTClient;

public class AdminService extends RESTService {
	
	public AdminService( RiscossRESTClient base ) {
		super( base );
	}

	public void setPredefinedRole( String domain, String role ) {
		rest.put( "admin/" + domain + "/default-role" ).param( "value", role ).send();
	}
	
	public String domains() {
		return rest.get( "admin/domains/list" ).send();
	}
	
	public void setRole( String username, String domain, String rolename ) {
		rest.post( "admin/" + domain + "/users/" + username + "/set" ).param( "role", rolename ).send();
	}

	public void deleteUser( String username ) {
		rest.delete( "admin/users/" + username + "/delete" ).send();
	}

}
