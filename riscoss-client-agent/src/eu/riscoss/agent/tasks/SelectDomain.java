package eu.riscoss.agent.tasks;

import eu.riscoss.agent.ExecutionContext;
import eu.riscoss.agent.RiscossRESTClient;

public class SelectDomain implements TestTask {
	
	private String domain;

	public SelectDomain( String domain ) {
		this.domain = domain;
	}
	
	@Override
	public void execute( RiscossRESTClient rest ) {
		ExecutionContext.get().set( "domain", domain );
	}
	
}
