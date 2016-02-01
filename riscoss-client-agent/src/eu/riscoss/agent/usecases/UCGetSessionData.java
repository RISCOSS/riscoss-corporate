package eu.riscoss.agent.usecases;

import java.io.File;
import java.util.Date;

import com.google.gson.Gson;

import eu.riscoss.agent.Context;
import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.agent.UseCase;
import eu.riscoss.agent.Workflow;
import eu.riscoss.agent.tasks.EnsureDomainExistence;
import eu.riscoss.agent.tasks.EnsureEntityExistence;
import eu.riscoss.agent.tasks.EnsureLayerStructureNew;
import eu.riscoss.agent.tasks.EnsureParent;
import eu.riscoss.agent.tasks.GenericTask;
import eu.riscoss.agent.tasks.PostRiskData;
import eu.riscoss.agent.tasks.SelectDomain;
import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;
import eu.riscoss.shared.JRASInfo;

public class UCGetSessionData implements UseCase {

	Gson gson = new Gson();
	
	String domain = "Playground";
	
	public static void main( String[] args ) throws Exception {
		new UCGetSessionData().run( new RiscossRESTClient() );
	}
	
	public UCGetSessionData() {}
	
	public UCGetSessionData( String domain ) {
		this.domain = domain;
	}
	
	@Override
	public void run( RiscossRESTClient rest ) throws Exception {
		
		rest.login( "admin", "admin" );
		
		Workflow w = new Workflow( rest );
		
		w.execute( new EnsureDomainExistence( domain ) );
		w.execute( new SelectDomain( domain ) );
		w.execute( new EnsureLayerStructureNew( new String[] { "Project", "OSSComponent" } ) );
		
		w.execute( new EnsureEntityExistence( "Project", "P1" ) );
		w.execute( new EnsureEntityExistence( "OSSComponent", "C1" ) );
		w.execute( new EnsureEntityExistence( "OSSComponent", "C2" ) );
		
		w.execute( new EnsureParent( "C1", "P1" ) );
		w.execute( new EnsureParent( "C2", "P1" ) );
		
		w.execute( new PostRiskData( domain, new RiskData(
				"github:repository-license", "C1", new Date(), RiskDataType.NUMBER, "1" ) ) );
		
		rest.domain( domain ).models().upload( new File( "/Users/albertosiena/models/dev/github_maintenance_risk-1434467716514.xml" ) );
		rest.domain( domain ).rcs().create( "github-rc" );
		rest.domain( domain ).rcs().rc( "github-rc1" ).associate( "OSSComponent", "github_maintenance_risk-1434467716514.xml" );
		
		Gson gson = new Gson();
		JRASInfo ras = gson.fromJson( rest.domain( domain ).analysis().createSession( "C1", "github-rc1" ), JRASInfo.class );
		
		rest.domain( domain ).analysis().session( ras.getId() ).execute();
		
		w.execute( new GenericTask<String>( ras.getId() ) {
			@Override
			public void execute( RiscossRESTClient rest, Context context ) {
//				System.out.println( new Gson().toJson( new String[] { "C1" } ) );
//						rest.domain( domain ).analysis().session( getObject() ).getRESTClient().get( "data" ).send() );
				rest.domain( domain ).analysis().session( getObject() ).get( "/data" ).param( 
						"e", "C1" ).send();
			}} );
		
		rest.logout();
		
	}
	
}
