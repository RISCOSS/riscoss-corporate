package eu.riscoss.agent.usecases;

import eu.riscoss.agent.Context;
import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.agent.Workflow;
import eu.riscoss.agent.tasks.GenericTask;

public class UCTestDescription extends UCAbstract {
	
	public static void main( String[] args ) throws Exception {
		new UCTestDescription().run( new RiscossRESTClient() );
	}
	
	public UCTestDescription() {
		initLayers( "OSS Component" );
		initEntities( "OSS Component", new String[] { "c1" } );
	}

	@Override
	protected void runUC( Workflow w ) {
		w.execute( new GenericTask<String>( "" ) {
			@Override
			public void execute( RiscossRESTClient rest, Context context ) {
				System.out.println(
						rest.domain( getDomain() ).entity( "c1" ).getDescription() );
			}} );
//		w.execute( new GenericTask<String>( "" ) {
//			@Override
//			public void execute( RiscossRESTClient rest, Context context ) {
//				rest.domain( getDomain() ).entity( "c1" ).setDescription( "TestDescription " + System.currentTimeMillis() );
//			}} );
//		w.execute( new GenericTask<String>( "" ) {
//			@Override
//			public void execute( RiscossRESTClient rest, Context context ) {
//				System.out.println(
//						rest.domain( getDomain() ).entity( "c1" ).getDescription( "<no description>" ) );
//			}} );
	}
	
}
