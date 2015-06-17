package eu.riscoss.server;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eu.riscoss.db.RiscossDB;

@Path("layers")
public class LayersManager {
	
	public LayersManager() {
	}
	
	@GET
	@Path("/list")
	public String list() {
		
		JsonArray a = new JsonArray();
		
		RiscossDB db = DBConnector.openDB();
		try {
			for( String layer : db.layerNames() ) {
				JsonObject o = new JsonObject();
				o.addProperty( "name", layer );
				a.add( o );
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
		
		return a.toString();
	}
	
	@POST
	@Path("new")
	public void createNew(
			@QueryParam("name") String name,
			@QueryParam("parent") String parentName
			) {
		name = name.trim();
		parentName = parentName.trim();
		
		RiscossDB db = DBConnector.openDB();
		try
		{
			db.addLayer( name, parentName );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@DELETE
	@Path("delete")
	public void deleteLayer( @QueryParam("name") String name ) {
		RiscossDB db = DBConnector.openDB();
		try {
			db.removeLayer( name );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
}