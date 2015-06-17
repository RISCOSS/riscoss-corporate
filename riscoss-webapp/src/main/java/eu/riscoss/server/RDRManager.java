package eu.riscoss.server;

import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.riscoss.db.RiscossDB;

@Path("rdr")
public class RDRManager {
	
	@POST
	@Path("/store")
	@Consumes({"application/json"})
	public void store( @HeaderParam("json") String string ) {
		System.out.println( string );
		JsonArray json = (JsonArray)new JsonParser().parse( string );
		RiscossDB db = DBConnector.openDB();
		try {
			for( int i = 0; i < json.size(); i++ ) {
				JsonObject o = json.get( i ).getAsJsonObject();
				try {
					db.storeRiskData( o.toString() );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
}
