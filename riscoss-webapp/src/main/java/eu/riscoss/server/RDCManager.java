package eu.riscoss.server;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eu.riscoss.rdc.RDC;
import eu.riscoss.rdc.RDCFactory;
import eu.riscoss.rdc.RDCParameter;


@Path("rdcs")
public class RDCManager {
	
	@GET
	@Path("/list")
	public String listRDCs() {
		
		JsonObject o = new JsonObject();
		
		for( RDC rdc : RDCFactory.get().listRDCs() ) {
			JsonObject jrdc = new JsonObject();
			JsonArray params = new JsonArray();
			for( RDCParameter par : rdc.getParameterList() ) {
				JsonObject jpar = new JsonObject();
				jpar.addProperty( "name", par.getName() );
				jpar.addProperty( "desc", par.getDescription() );
				jpar.addProperty( "def", par.getDefaultValue() );
				jpar.addProperty( "ex", par.getExample() );
				params.add( jpar );
			}
			jrdc.add( "params", params );
			o.add( rdc.getName(), jrdc );
		}
		
		System.out.println( "Returning RDC list: " + o.toString() );
		
		return o.toString();
	}
	
}