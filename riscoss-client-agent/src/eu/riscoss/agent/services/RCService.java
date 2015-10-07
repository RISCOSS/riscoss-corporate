package eu.riscoss.agent.services;

import com.google.gson.Gson;

import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.shared.JRiskConfiguration;
import eu.riscoss.shared.JRiskConfigurationLayerInfo;

public class RCService extends RESTService {

	public RCService(RiscossRESTClient rest, String name) {
		super( rest );
		this.rest.set( "rc", name );
	}
	
	public String getName() {
		return rest.get( "rc", "" );
	}
	
	public void associate( String layer, String model ) {
		
		JRiskConfiguration rc = 
				new Gson().fromJson( rest.domain( getDomain() ).rcs().get( getName() ), JRiskConfiguration.class );
		
		for( JRiskConfigurationLayerInfo l : rc.layers ) {
			if( l.name.equals( layer ) ) {
				l.models.add( model );
			}
		}
		
		rc.models.add( model );
		
		rest.post( "rcs/" + getDomain() + "/" + getName() + "/store" ).send( new Gson().toJson( rc ) );
	}
	
}
