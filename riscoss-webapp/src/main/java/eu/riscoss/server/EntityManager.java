package eu.riscoss.server;

import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.db.RiscossDB;
import eu.riscoss.rdc.RDC;
import eu.riscoss.rdc.RDCFactory;
import eu.riscoss.rdc.RDCParameter;

@Path("entities")
public class EntityManager {
	
	@GET
	@Path("/list")
	public String list() {
		
		JsonArray a = new JsonArray();
		
		RiscossDB db = DBConnector.openDB();
		try {
			for( String name : db.entities() ) {
				JsonObject o = new JsonObject();
				o.addProperty( "name", name );
				a.add( o );
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
		
		return a.toString();
 
	}
	
	@GET
	@Path("/list/{layer}")
	public String list( @PathParam("layer") String layer ) {
		
		JsonArray a = new JsonArray();
		
		RiscossDB db = DBConnector.openDB();
		try {
			for( String name : db.entities( layer ) ) {
				JsonObject o = new JsonObject();
				o.addProperty( "name", name );
				a.add( o );
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
		
		return a.toString();
 
	}
	
	@GET
	@Path("/rdcs/list")
	public String listRDCs( @QueryParam("entity") String entityName ) {
		JsonObject o = new JsonObject();
		RiscossDB db = DBConnector.openDB();
		try {
			for( RDC rdc : RDCFactory.get().listRDCs() ) {
				String rdcName = rdc.getName();
				boolean enabled = db.isRDCEnabled( entityName, rdcName );
				if( enabled ) {
					JsonObject jrdc = new JsonObject();
					JsonObject params = new JsonObject();
					for( RDCParameter par : rdc.getParameterList() ) {
						params.addProperty( par.getName(), 
								db.getRDCParmeter(entityName, rdcName, par.getName(), "" ) );
					}
					jrdc.addProperty( "enabled", enabled );
					jrdc.add( "params", params );
					o.add( rdc.getName(), jrdc );
				}
			}
			System.out.println( "Returning: " + o.toString() );
			return o.toString();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@PUT
	@Path("/rdcs/save")
	public void setRDCs( 
			@QueryParam("entity") String entityName,
			@HeaderParam("rdcmap") String rdcmapString ) {
		RiscossDB db = DBConnector.openDB();
		try {
			System.out.println( "Received: " + rdcmapString );
			JsonObject json = (JsonObject)new JsonParser().parse( rdcmapString );
			for( Map.Entry<String, JsonElement> entry : json.entrySet() ) {
				String rdcName = entry.getKey();
				JsonObject o = entry.getValue().getAsJsonObject();
				
//				Do we need to add this check?
				if( RDCFactory.get().getRDC( rdcName ) == null ) continue;
				
				boolean enabled = false;
				
				try {
					enabled = "true".equals( entry.getValue().getAsJsonObject().get( "enabled" ).getAsString() );
				}
				catch( Exception ex ) {}
				
				db.setRDCEnabled( entityName, rdcName, enabled );
				if( enabled ) {
					for( RDCParameter par : RDCFactory.get().getRDC( rdcName ).getParameterList() ) {
						String value = 
								o.get( "params" ).getAsJsonObject().get( par.getName() ).getAsString().toString();
						db.setRDCParmeter( entityName, rdcName, par.getName(), value );
					}
				}
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST
	@Path("/new")
	@Produces("application/json")
	public String createEntity( 
			@QueryParam("name") String name,
			@QueryParam( "layer") String layer,
			@QueryParam("parent") String parent ) {
		RiscossDB db = DBConnector.openDB();
		try {
			JsonObject ret = new JsonObject();
			db.addEntity( name, layer );
			if( parent != null ) {
				if( !"".equals( parent ) ) {
					if( db.existsEntity( parent ) ) {
						db.assignEntity( name, parent );
					}
				}
			}
			ret.addProperty( "name", name );
			ret.addProperty( "layer", "layer" );
			ret.addProperty( "parent", parent );
			System.out.println( ret.toString() );
			return ret.toString(); //Response.ok(ret, MediaType.APPLICATION_JSON).build();
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			return ""; //Response.ok( "", MediaType.APPLICATION_JSON).build();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST
	@Path("/create")
	@Produces("application/json")
	public String createEntity( 
			@HeaderParam("info") String str ) {
		RiscossDB db = DBConnector.openDB();
		try {
			JsonObject json = (JsonObject)new JsonParser().parse( str );
			String name = json.get( "name" ).getAsString();
			String layer = json.get( "layer" ).getAsString();
			db.addEntity( name, layer );
			JsonArray a = json.get("parents" ).getAsJsonArray();
			for( int i = 0; i < a.size(); i++ ) {
				String parent = a.get( i ).getAsString();
				db.assignEntity( name, parent);
			}
			JsonObject ret = new JsonObject();
			ret.addProperty( "name", name );
			ret.addProperty( "layer", "layer" );
			System.out.println( ret.toString() );
			return ret.toString();
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			return "";
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET
	@Path("/rdcs/newrun")
	@Produces("application/json")
	public String runRDCS( @QueryParam("entity") String entityName ) {
		RiscossDB db = DBConnector.openDB();
		try {
			JsonObject json = new JsonObject();
			for( RDC rdc : RDCFactory.get().listRDCs() ) {
				String rdcName = rdc.getName();
				boolean enabled = db.isRDCEnabled( entityName, rdcName );
				if( enabled ) {
					JsonObject o = new JsonObject();
					for( RDCParameter par : rdc.getParameterList() ) {
						rdc.setParameter( par.getName(), 
								db.getRDCParmeter( entityName, rdcName, par.getName(), "" ) );
					}
					Map<String,RiskData> values = rdc.getIndicators( entityName );
					for( String key : values.keySet() ) {
						RiskData rd = values.get( key );
						try {
							db.storeRiskData( rd.toJSON() );
						} catch (Exception e) {
							e.printStackTrace();
						}
						JsonObject jrd = new JsonObject();
						jrd.addProperty( "type", rd.getType().name() );
						jrd.addProperty( "date", rd.getDate().toString() );
						jrd.addProperty( "value", rd.getValue().toString() );
						o.add( key, jrd );
					}
					json.add( rdcName, o );
				}
			}
			System.out.println( "Returning: " + json.toString() );
			return json.toString();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@DELETE
	@Path("/entity/delete")
	public void deleteEntity( @QueryParam("entity") String entity ) {
		RiscossDB db = DBConnector.openDB();
		try {
			db.removeEntity( entity );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET
	@Path("/entity/rd/get")
	public String getRiskData( @QueryParam("entity") String entity ) {
		RiscossDB db = DBConnector.openDB();
		try {
			JsonObject json = new JsonObject();
			JsonArray array = new JsonArray();
			for( String id : db.listRiskData( entity ) ) {
				JsonObject o = (JsonObject)new JsonParser().parse( db.readRiskData( entity, id ) );
				array.add(  o );
			}
			json.add( "list", array );
			return json.toString();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST
	@Path("/entity/rd/set")
	public void setRiskData( @QueryParam("entity") String entity, @HeaderParam("rd") String string ) {
		RiscossDB db = DBConnector.openDB();
		try {
			JsonObject json = (JsonObject)new JsonParser().parse( string );
			JsonArray array = new JsonArray();
			for( String id : db.listRiskData( entity ) ) {
				JsonObject o = (JsonObject)new JsonParser().parse( db.readRiskData( entity, id ) );
				array.add(  o );
			}
			json.add( "list", array );
//			return json.toString();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET
	@Path("/entity/data")
	public String getEntityData( @QueryParam("entity") String entity ) {
		RiscossDB db = DBConnector.openDB();
		try {
			JsonObject json = new JsonObject();
			
			json.addProperty( "name", entity );
			json.addProperty( "layer", db.layerOf( entity ) );
			
			{
				JsonArray a = new JsonArray();
				for( String e : db.getParents( entity ) ) {
					a.add( new JsonPrimitive( e ) );
				}
				json.add( "parents", a );
			}
			
			{
				JsonArray a = new JsonArray();
				for( String e : db.getChildren( entity ) ) {
					a.add( new JsonPrimitive( e ) );
				}
				json.add( "children", a );
			}
			
			{
				JsonArray jlist = new JsonArray();
				for( RDC rdc : RDCFactory.get().listRDCs() ) {
					String rdcName = rdc.getName();
					if( db.isRDCEnabled( entity, rdcName ) ) {
						jlist.add( new JsonPrimitive( rdc.getName() ) );
					}
				}
				json.add( "rdcs", jlist );
			}
			
			JsonArray array = new JsonArray();
			for( String id : db.listUserData( entity ) ) {
				
				JsonObject o = (JsonObject)new JsonParser().parse( db.readRiskData( entity, id ) );
				array.add(  o );
				
			}
			json.add( "userdata", array );
			
			System.out.println( "Returning: " + json.toString() );
			return json.toString();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST
	@Path("/entity/parent")
	@Produces("application/json")
	public void setParent( 
			@QueryParam("entity") String entity,
			@HeaderParam("entities") String str ) {
		RiscossDB db = DBConnector.openDB();
		try {
			JsonObject json = (JsonObject)new JsonParser().parse( str );
			JsonArray a = json.get("list" ).getAsJsonArray();
			for( int i = 0; i < a.size(); i++ ) {
				String parent = a.get( i ).getAsString();
				db.assignEntity( entity, parent);
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST
	@Path("/entity/children")
	@Produces("application/json")
	public void setChildren( 
			@QueryParam("entity") String entity,
			@HeaderParam("entities") String str ) {
		RiscossDB db = DBConnector.openDB();
		try {
			JsonObject json = (JsonObject)new JsonParser().parse( str );
			JsonArray a = json.get("list" ).getAsJsonArray();
			for( int i = 0; i < a.size(); i++ ) {
				String child = a.get( i ).getAsString();
				db.assignEntity( child, entity );
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET
	@Path("/entity/parent")
	@Produces("application/json")
	public String getParent( 
			@QueryParam("entity") String entity ) {
		RiscossDB db = DBConnector.openDB();
		try {
			JsonObject json = new JsonObject();
			JsonArray array = new JsonArray();
			for( String e : db.getParents( entity ) ) {
				array.add( new JsonPrimitive( e ) );
			}
			json.add( "entities", array );
			return json.toString();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET
	@Path("/entity/hierarchy")
	@Produces("application/json")
	public String getHierarchyInfo( 
			@QueryParam("entity") String entity ) {
		RiscossDB db = DBConnector.openDB();
		try {
			JsonObject json = new JsonObject();
			JsonArray array = new JsonArray();
			for( String e : db.getParents( entity ) ) {
				array.add( new JsonPrimitive( e ) );
			}
			json.add( "parents", array );
			array = new JsonArray();
			for( String e : db.getChildren( entity ) ) {
				array.add( new JsonPrimitive( e ) );
			}
			json.add( "children", array );
			return json.toString();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET
	@Path("/entity/ras")
	@Produces("application/json")
	public String getRAD( 
			@QueryParam("entity") String entity ) {
		RiscossDB db = DBConnector.openDB();
		try {
			return db.readRASResult( entity );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
}
