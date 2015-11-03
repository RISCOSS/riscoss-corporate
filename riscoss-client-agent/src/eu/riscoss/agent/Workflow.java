package eu.riscoss.agent;

import eu.riscoss.agent.tasks.TestTask;

public class Workflow {
	
	Context context = new Context();
	
	RiscossRESTClient rest;
	
	public Workflow( RiscossRESTClient rest ) {
		this.rest = rest;
	}
	
	public void execute( TestTask task ) {
		task.execute( rest, this.context );
	}

	public Context getContext() {
		return this.context;
	}
	
}
