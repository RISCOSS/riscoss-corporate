package eu.riscoss.agent.tasks;

import eu.riscoss.agent.Context;
import eu.riscoss.agent.RiscossRESTClient;

public class ListEntities implements TestTask {

	@Override
	public void execute( RiscossRESTClient rest, Context context ) {
		context.set( "entities", rest.domain( context.get( "domain", "" ) ).entities().list() );
	}
	
	
	
}
