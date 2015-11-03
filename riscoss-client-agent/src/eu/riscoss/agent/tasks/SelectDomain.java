package eu.riscoss.agent.tasks;

import eu.riscoss.agent.Context;
import eu.riscoss.agent.RiscossRESTClient;

public class SelectDomain implements TestTask {
	
	private String domain;

	public SelectDomain( String domain ) {
		this.domain = domain;
	}
	
	@Override
	public void execute( RiscossRESTClient rest, Context context ) {
		context.set( "domain", domain );
	}
	
}
