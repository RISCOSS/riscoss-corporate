package eu.riscoss.agent.tasks;

import eu.riscoss.agent.Context;
import eu.riscoss.agent.RiscossRESTClient;

public class SearchEntities implements TestTask {
	
	private String layer;
	private String partialName;
	private String from;
	private String max;
	private String h;

	/*
	 		@DefaultValue("") @PathParam("layer") String layer, 
			@DefaultValue("") @QueryParam("query") String query, 
			@DefaultValue("0") @QueryParam("from") String strFrom,
			@DefaultValue("0") @QueryParam("max") String strMax, 
			@DefaultValue("f") @QueryParam("h") String strHierarchy

	 */
	public SearchEntities( String layer, String partialName, String from, String max, String h ) {
		this.layer = layer;
		this.partialName = partialName;
		this.from = from;
		this.max = max;
		this.h = h;
	}
	
	@Override
	public void execute( RiscossRESTClient rest, Context context ) {
		context.set( "entities", rest.domain( context.get( "domain", "" ) ).entities().search(
				layer, partialName, from, max, h ) );
	}
	
	
	
}
