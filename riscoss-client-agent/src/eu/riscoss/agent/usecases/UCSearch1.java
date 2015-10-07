package eu.riscoss.agent.usecases;

import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.agent.UseCase;
import eu.riscoss.agent.tasks.EnsureDomainExistence;
import eu.riscoss.agent.tasks.EnsureLayerStructure;

public class UCSearch1 implements UseCase {

	@Override
	public void run( RiscossRESTClient rest ) throws Exception {
		
		String domain = "SearchPlayground";
		String[] layers = new String[] { "OSSComponent" };
		String[] entities = new String[] { "c1", "c2", "c3", "c4", "comp1 part2" };
		
		rest.login( "admin", "admin" );
		
		new EnsureDomainExistence( domain ).execute( rest );
		new EnsureLayerStructure( domain, layers ).execute( rest );
		
		for( String entity : entities )
			if( !rest.domain( domain ).entities().exists( entity ) )
				rest.domain( domain ).entities().create( entity, layers[0] );
		
		System.out.println( rest.domain( domain ).entities().search( "c2" ) );
		System.out.println( rest.domain( domain ).entities().search( "c" ) );
		System.out.println( rest.domain( domain ).entities().search( "part2" ) );
		
	}
	
}
