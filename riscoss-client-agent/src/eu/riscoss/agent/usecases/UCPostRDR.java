package eu.riscoss.agent.usecases;

import java.util.Date;

import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.agent.UseCase;
import eu.riscoss.agent.Workflow;
import eu.riscoss.agent.tasks.EnsureDomainExistence;
import eu.riscoss.agent.tasks.EnsureEntityExistence;
import eu.riscoss.agent.tasks.EnsureLayerStructure;
import eu.riscoss.agent.tasks.PostRiskData;
import eu.riscoss.agent.tasks.SelectDomain;
import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;

public class UCPostRDR implements UseCase {

	public static void main( String[] args ) throws Exception {
		new UCPostRDR().run( new RiscossRESTClient() );
	}
	
	@Override
	public void run( RiscossRESTClient rest ) throws Exception {
		
		String domain = "RDRTest";
		String[] layers = new String[] { "OSSComponent" };
		String[] entities = new String[] { "c1", "c2", "c3", "c4", "comp1 part2" };
		
		rest.login( "admin", "admin" );
		
		Workflow w = new Workflow( rest );
		
		w.execute( new EnsureDomainExistence( domain ) );
		w.execute( new SelectDomain( domain ) );
		w.execute( new EnsureLayerStructure( domain, layers ) );
		
		for( String entity : entities ) {
			w.execute( new EnsureEntityExistence( layers[0],  entity ) );
		}
		
		w.execute( new PostRiskData( domain, new RiskData(
				"indicator1", "c1", new Date(), RiskDataType.NUMBER, "100" ) ) );
		
		rest.logout();
		
	}
	
}
