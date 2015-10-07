package eu.riscoss.agent.usecases;

import java.io.File;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.agent.UseCase;
import eu.riscoss.agent.tasks.EnsureDomainExistence;
import eu.riscoss.agent.tasks.EnsureLayerStructure;
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
		new EnsureLayerStructure( domain, new String[] { "Project", "OSSComponent" } );
		
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
			rest.domain( domain ).models().upload( new File( "/Users/albertosiena/models/dev/i1.xml" ) );
			
			rest.domain( domain ).rcs().get( "rc1" );
			rest.domain( domain ).rcs().create( "rc1" );
			rest.domain( domain ).rcs().rc( "rc1" ).associate( "Project", "i1.xml" );
			rest.domain( domain ).rcs().rc( "rc1" ).associate( "OSSComponent", "i1.xml" );
			rest.domain( domain ).rcs().rc( "rc1" ).associate( "OSSComponent", "i1.xml" );
		}
		
		{	// Create and execute a risk analysis session
			Gson gson = new Gson();
			JRASInfo ras = gson.fromJson( rest.domain( domain ).analysis().createSession( "p1", "rc1" ), JRASInfo.class );
			
			String ret = rest.domain( domain ).analysis().session( ras.getId() ).execute();
			
			JRiskAnalysisResult result = gson.fromJson( ret, JRiskAnalysisResult.class );
			
			System.out.println( "" + ret );
			
			// TODO create scenario
			
			// TODO execute risk mitigation
		}
		
		
		rest.logout();
		
	}
	
	<T> T decode( String json ) {
		Type listType = new TypeToken<T>() {}.getType();
		T t = gson.fromJson(json, listType);
		return t;
	}
	
}
