package eu.riscoss.agent.tasks;

import eu.riscoss.agent.Context;
import eu.riscoss.agent.RiscossRESTClient;

public class ListDomains implements TestTask {
	
	@Override
	public void execute( RiscossRESTClient rest, Context context ) {
		rest.domains().list();
	}
	
}
