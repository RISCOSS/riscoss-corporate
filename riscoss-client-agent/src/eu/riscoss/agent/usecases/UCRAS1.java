package eu.riscoss.agent.usecases;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.agent.UseCase;
import eu.riscoss.agent.tasks.EnsureDomainExistence;
import eu.riscoss.agent.tasks.EnsureLayerStructure;
import eu.riscoss.shared.JArgument;
import eu.riscoss.shared.JEntityData;
import eu.riscoss.shared.JRASInfo;
import eu.riscoss.shared.JRiskAnalysisResult;

public class UCRAS1 implements UseCase {

	Gson gson = new Gson();
	
	@Override
	public void run( RiscossRESTClient rest ) throws Exception {
		
		String domain = "RAS1_Domain";
		
		rest.login( "admin", "admin" );
		
		new EnsureDomainExistence( domain ).execute( rest );
		new EnsureLayerStructure( domain, new String[] { "Project", "OSSComponent" } ).execute( rest );
		
		{	// Create 3 entities: 1 project and 2 components
			JEntityData data = null;
			
			data = gson.fromJson( rest.domain( domain ).entity( "c1" ).getData(), JEntityData.class );
			if( data.layer == null ) {
				rest.domain( domain ).entities().create( "c1", "OSSComponent" );
			}
			
			data = gson.fromJson( rest.domain( domain ).entity( "c2" ).getData(), JEntityData.class );
			if( data.layer == null ) {
				rest.domain( domain ).entities().create( "c2", "OSSComponent" );
			}
			
			data = gson.fromJson( rest.domain( domain ).entity( "p1" ).getData(), JEntityData.class );
			
			if( data.layer == null ) {
				rest.domain( domain ).entities().create( "p1", "Project" );
			}
			rest.domain( domain ).entity( "p1" ).setChildren( "c1", "c2" );
		}
		
		{	// Upload models and create risk configuration
			rest.domain( domain ).models().upload( new File( "/Users/albertosiena/models/dev/github_maintenance_risk-1434467716514.xml" ) );
			
//			rest.domain( domain ).rcs().get( "rc1" );
//			rest.domain( domain ).rcs().create( "rc1" );
//			rest.domain( domain ).rcs().rc( "rc1" ).associate( "Project", "i1.xml" );
//			rest.domain( domain ).rcs().rc( "rc1" ).associate( "OSSComponent", "i1.xml" );
//			rest.domain( domain ).rcs().rc( "rc1" ).associate( "OSSComponent", "i1.xml" );
			
			rest.domain( domain ).rcs().create( "github-rc" );
			rest.domain( domain ).rcs().rc( "github-rc1" ).associate( "OSSComponent", "github_maintenance_risk-1434467716514.xml" );
		}
		
		{	// Create and execute a risk analysis session
			Gson gson = new Gson();
			JRASInfo ras = gson.fromJson( rest.domain( domain ).analysis().createSession( "c1", "rc1" ), JRASInfo.class );
			
			String ret = rest.domain( domain ).analysis().session( ras.getId() ).execute();
			
			System.out.println( "" + ret );
			
			JRiskAnalysisResult result = gson.fromJson( ret, JRiskAnalysisResult.class );
			
			for( JArgument arg : result.argumentation.arguments.values() ) {
				print( arg, System.out );
//				print( result.argumentation.argument, System.out );
			}
			
			// TODO create scenario
			// SKIP (The github model gives risks in output with all inputs = 0)
			
			// TODO execute risk mitigation
		}
		
		
		rest.logout();
		
	}
	
	private void print( JArgument argument, PrintStream out ) {
		print( argument, out, "" );
	}

	private void print( JArgument argument, PrintStream out, String prefix ) {
		out.println( prefix + " " + argument.summary + ": " + argument.truth );
		for( JArgument arg : argument.subArgs ) {
			print( arg, out, prefix + "  " );
		}
	}

	<T> T decode( String json ) {
		Type listType = new TypeToken<T>() {}.getType();
		T t = gson.fromJson(json, listType);
		return t;
	}
	
}
