package eu.riscoss.agent.tasks;

import eu.riscoss.agent.Context;
import eu.riscoss.agent.RiscossRESTClient;

public interface TestTask {
	
	void execute( RiscossRESTClient rest, Context context );
	
}
