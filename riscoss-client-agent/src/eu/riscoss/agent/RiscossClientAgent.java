package eu.riscoss.agent;

import eu.riscoss.agent.usecases.UCRAS1;
import eu.riscoss.agent.usecases.UCSearch1;


public class RiscossClientAgent {
	
	public static void main( String[] args ) {
		new RiscossClientAgent().run( "http://127.0.0.1:8888" );
	}
	
	public void run( String base_addr ) {
		
		try {
			new UCRAS1().run( new RiscossRESTClient( base_addr ) );
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		
	}
	
	void runAll( String base_addr ) {
		
		RiscossRESTClient client = new RiscossRESTClient( base_addr );
		
		for( UseCase uc : UseCaseLibrary.get().useCases() ) {
			
			try {
				uc.run( client );
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
}
