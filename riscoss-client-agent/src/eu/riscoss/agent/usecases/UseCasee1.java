package eu.riscoss.agent.usecases;

import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.agent.UseCase;


public class UseCasee1 implements UseCase {
	
	public void run( RiscossRESTClient client ) throws Exception {
		
		client.ensureUserExistence( "asiena", "pippo" );
		
		client.login( "admin", "admin" );
		
		client.admin().setPredefinedRole( "Playground", "Administrator" );
		client.admin().setRole( "asiena", "FBK", "Consumer" );
		client.admin().setRole( "asiena", "UPC", "Guest" );
		
		client.logout();
		
		client.login( "asiena", "pippo" );
		
		System.out.println( client.user().listDomains( "asiena" ) );
		
//		client.domain( "FBK" ).layers().create( "OSSComponent" );
		
		System.out.println( 
				client.domain( "FBK" ).layer( "OSSComponent" ).entities().list() );
		
		client.domain( "FBK" ).layers().delete( "OSS Component" );
		
		client.logout();
		
	}
	
}
