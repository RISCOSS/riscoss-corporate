package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

public enum KnownRoles {
	
	Guest( DBResource.All.name() ,"r" ), 
	Consumer( DBResource.All.name(),"r" ), 
	Producer( DBResource.All.name(),"r", DBResource.Entities.name(),"rw", DBResource.Layers.name(),"rw" ), 
	Modeler( DBResource.All.name(),"r", DBResource.Models.name(), "rw", DBResource.RiskConfigurations.name(), "rw" ), 
	Administrator( "All","rw" );
	
	List<Pair<DBResource,String>> rights = new ArrayList<>();
	
	KnownRoles( String ... tok ) {
		if( tok == null ) return;
		if( (tok.length & 1) != 0 )
			throw new RuntimeException( "Wrong number of arguments (must be an even number, where n = <path> and n+1 = <access>" );
		for( int i = 0; i < tok.length; i += 2 ) {
			rights.add( new Pair<DBResource, String>( DBResource.valueOf( tok[i] ), tok[i+1] ) );
		}
	}
	
	public Iterable<Pair<DBResource,String>> permissions() {
		return this.rights;
	}
	
}
