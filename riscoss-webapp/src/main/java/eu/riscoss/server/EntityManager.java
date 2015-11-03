/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

/**
 * @author 	Alberto Siena
 **/

package eu.riscoss.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.SearchParams;
import eu.riscoss.ram.algo.DownwardEntitySearch;
import eu.riscoss.ram.algo.TraverseCallback;
import eu.riscoss.ram.algo.DownwardEntitySearch.DIRECTION;
import eu.riscoss.rdc.RDC;
import eu.riscoss.rdc.RDCFactory;
import eu.riscoss.rdc.RDCParameter;
import eu.riscoss.shared.JEntityData;
import eu.riscoss.shared.JEntityNode;
import eu.riscoss.shared.JRiskNativeData;
import eu.riscoss.shared.RiscossUtil;

@Path("entities")
public class EntityManager {

	@GET @Path("/{domain}/list")
	public String list(@DefaultValue("Playground") @PathParam("domain") String domain,
			@DefaultValue("") @HeaderParam("token") String token ) {

		JsonArray a = new JsonArray();

		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			for (String name : db.entities()) {
				JsonObject o = new JsonObject();
				o.addProperty("name", name);
				o.addProperty("layer", db.layerOf(name));
				a.add(o);
			}
		} finally {
			DBConnector.closeDB(db);
		}

		return a.toString();

	}

	/**
	 * returns the list of entities in a specific layer.
	 * @param domain
	 * @param layer
	 * @param token
	 * @return
	 */
	@GET @Path("/{domain}/{layer}/list")
	public String list(@DefaultValue("Playground") @PathParam("domain") String domain,
			@PathParam("layer") String layer, @DefaultValue("") @HeaderParam("token") String token) {

		//TODO: check if layer exists!
		JsonArray a = new JsonArray();

		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			for (String name : db.entities(layer)) {
				JsonObject o = new JsonObject();
				o.addProperty("name", name);
				o.addProperty("layer", layer);
				a.add(o);
			}
		} finally {
			DBConnector.closeDB(db);
		}

		return a.toString();

	}
	
	@GET @Path("/{domain}/search") 
	public String search(
			@PathParam("domain") String domain, 
			@HeaderParam("token") String token,
			@DefaultValue("") @QueryParam("query") String query ) {
		return searchNew( domain, token, "", query, "0", "0", "false", "" );
	}
	
	@GET @Path("/{domain}/{layer}/search_old") 
	public String search(@DefaultValue("Playground") @PathParam("domain") String domain, @HeaderParam("token") String token,
			@DefaultValue("") @PathParam("layer") String layer, 
			@DefaultValue("") @QueryParam("query") String query, 
			@DefaultValue("0") @QueryParam("from") String strFrom,
			@DefaultValue("0") @QueryParam("max") String strMax,    //not used for now in client
			@DefaultValue("false") @QueryParam("h") String strHierarchy
		) {
		
		JsonArray a = new JsonArray();
		
		RiscossDB db = DBConnector.openDB( domain, token );
		try {
			SearchParams params = new SearchParams();
			params.setMax( strMax );
			params.setFrom( strFrom );
			params.setOptLoadHierarchy( strHierarchy );
			
			Collection<String> list = db.findEntities( layer, query, params );
			for (String name : list) {
				JsonObject o = new JsonObject();
				o.addProperty("name", name);
				o.addProperty("layer", db.layerOf(name));
				a.add(o);
			}
		} finally {
			DBConnector.closeDB(db);
		}
		return a.toString();
	}

	@GET @Path("/{domain}/{layer}/search") 
	public String searchNew(@DefaultValue("Playground") @PathParam("domain") String domain, @HeaderParam("token") String token,
			@DefaultValue("") @PathParam("layer") String layer, 
			@DefaultValue("") @QueryParam("query") String query, 
			@DefaultValue("0") @QueryParam("from") String strFrom,
			@DefaultValue("0") @QueryParam("max") String strMax, 
			@DefaultValue("f") @QueryParam("h") String strHierarchy,
			@DefaultValue("") @QueryParam("f") String flags
		) {
		
		List<JEntityNode> result = new ArrayList<JEntityNode>();
		
		RiscossDB db = DBConnector.openDB( domain, token );
		try {
			SearchParams params = new SearchParams();
			params.setMax( strMax );
			params.setFrom( strFrom );
			params.setOptLoadHierarchy( flags.indexOf( "h" ) != -1 );
			params.setOptLoadHierarchy( strHierarchy );
			
			List<String> layers = new ArrayList<String>();
			
			if( flags.indexOf( "s" ) != -1 ) {
				layers = db.getScope( layer );
			}
			else {
				layers.add( layer );
			}
			
			for( String l : layers ) {
				
				Collection<String> list = db.findEntities( l, query, params );
				for (String name : list) {
					JEntityNode jd = new JEntityNode();
					jd.name = name;
					jd.layer = db.layerOf( name );
					result.add( jd );
					if( params.loadHierarchy = true ) {
						loadDescendants( jd, db );
					}
				}
				
			}
			
		} finally {
			DBConnector.closeDB(db);
		}
		return new Gson().toJson( result );
	}
	
	Set<String> set = new HashSet<String>();
	
	private void loadDescendants( JEntityNode jparent, RiscossDB db ) {
		for( String name : db.getChildren( jparent.name ) ) {
			if( set.contains( name ) ) {
				System.err.println( "Cycle detected!!!!!!!!!!!!!" );
				JEntityNode jd = new JEntityNode();
				jd.name = "CYCLE DETECTED! (" + name + ")";
				jd.layer = db.layerOf( name );
				jparent.children.add( jd );
			}
			else {
				JEntityNode jd = new JEntityNode();
				jd.name = name;
				jd.layer = db.layerOf( name );
				jparent.children.add( jd );
				set.add( name );
				loadDescendants( jd, db );
				set.remove( name );
			}
		}
	}

	Map<String,JEntityData> getParents( String entity, RiscossDB db ) {
		
		Map<String,JEntityData> map = new HashMap<String, JEntityData>();
		
		for( String name : db.getParents( entity ) ) {
			JEntityData jd = new JEntityData();
			jd.name = name;
			jd.layer = db.layerOf( name );
			map.put( name, jd );
		}
		
		return map;
	}
	
	Map<String,JEntityData> getRoots( String entity, RiscossDB db ) {
		
		List<String> parents = db.getParents( entity );
		
		if( parents.size() < 1 )
			return new HashMap<String, JEntityData>();
		
		Map<String,JEntityData> map = new HashMap<String, JEntityData>();
		
		return map;
		
	}
	
	@POST @Path("/{domain}/create")
	@Produces("application/json")
	//TODO: remove parent. Extra call for adding parents.
	public String createEntity(@DefaultValue("Playground") @PathParam("domain") String domain,
			@QueryParam("name") String name, @QueryParam("layer") String layer, @QueryParam("parent") String parent,
			@DefaultValue("") @HeaderParam("token") String token ) {

		// attention:filename sanitation is not directly notified to the user
		name = RiscossUtil.sanitize(name);

		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			JsonObject ret = new JsonObject();
			db.addEntity(name, layer);
			if (parent != null) {
				if (!"".equals(parent)) {
					if (db.existsEntity(parent)) {
						db.assignEntity(name, parent);
					}
				}
			}
			ret.addProperty("name", name);
			ret.addProperty("layer", layer);
			ret.addProperty("parent", parent);
			System.out.println(ret.toString());
			return ret.toString(); // Response.ok(ret,
									// MediaType.APPLICATION_JSON).build();
		} catch (Exception ex) {
			ex.printStackTrace();
			return ""; // Response.ok( "", MediaType.APPLICATION_JSON).build();
		} finally {
			DBConnector.closeDB(db);
		}
	}

	@DELETE @Path("/{domain}/{entity}/delete")
	public void deleteEntity(@DefaultValue("Playground") @PathParam("domain") String domain,
			@PathParam("entity") String entity,
			@DefaultValue("") @HeaderParam("token") String token ) {
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			db.removeEntity(entity);
		} finally {
			DBConnector.closeDB(db);
		}
	}

	@GET @Path("/{domain}/{entity}/data")
	public String getEntityData(@DefaultValue("Playground") @PathParam("domain") String domain,
			@PathParam("entity") String entity,
			@DefaultValue("") @HeaderParam("token") String token ) {
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			JsonObject json = new JsonObject();

			json.addProperty("name", entity);
			json.addProperty("layer", db.layerOf(entity));

			{
				JsonArray a = new JsonArray();
				for (String e : db.getParents(entity)) {
					a.add(new JsonPrimitive(e));
				}
				json.add("parents", a);
			}

			{
				JsonArray a = new JsonArray();
				for (String e : db.getChildren(entity)) {
					a.add(new JsonPrimitive(e));
				}
				json.add("children", a);
			}

			{
				JsonArray jlist = new JsonArray();
				for (RDC rdc : RDCFactory.get().listRDCs()) {
					String rdcName = rdc.getName();
					if (db.isRDCEnabled(entity, rdcName)) {
						jlist.add(new JsonPrimitive(rdc.getName()));
					}
				}
				json.add("rdcs", jlist);
			}

			JsonArray array = new JsonArray();
			for (String id : db.listUserData(entity)) {

				JsonObject o = (JsonObject) new JsonParser().parse(db.readRiskData(entity, id));
				array.add(o);

			}
			json.add("userdata", array);

			System.out.println("Returning entity data: " + json.toString());
			return json.toString();
		} finally {
			DBConnector.closeDB(db);
		}
	}

	//currently not used!
	@GET @Path("/{domain}/{entity}/data_new")
	public String getEntityData_new(@DefaultValue("Playground") @PathParam("domain") String domain,
			@PathParam("entity") String entity,
			@DefaultValue("") @HeaderParam("token") String token ) {
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			
			Gson gson = new Gson();
			
			JEntityData data = new JEntityData();
			
			data.name = entity;
			data.layer = db.layerOf( entity );

			{
				List<String> list = new ArrayList<>();
				for (String e : db.getParents(entity)) {
					list.add( e );
				}
				data.parents = list;
			}

			{
				List<String> list = new ArrayList<>();
				for (String e : db.getChildren(entity)) {
					list.add( e );
				}
				data.children = list;
			}

			{
				List<String> list = new ArrayList<>();
				for (RDC rdc : RDCFactory.get().listRDCs()) {
					String rdcName = rdc.getName();
					if (db.isRDCEnabled(entity, rdcName)) {
						list.add( rdcName );
					}
				}
				data.rdcs = list;
			}
			
			{
				List<JRiskNativeData> list = new ArrayList<>();
				for (String id : db.listUserData(entity)) {
					JRiskNativeData rd = gson.fromJson( db.readRiskData(entity, id), JRiskNativeData.class );
					list.add( rd );
				}
				data.userdata = list;
			}
			
			String json = gson.toJson( data );
			
			System.out.println("Returning: " + json );
			
			return json;
		} finally {
			DBConnector.closeDB(db);
		}
	}

	@POST @Path("/{domain}/{entity}/parents")
	@Produces("application/json")
	public void setParents(@DefaultValue("Playground") @PathParam("domain") String domain,
			@PathParam("entity") String entity, String parents, //@HeaderParam("json") 
 			@DefaultValue("") @HeaderParam("token") String token ) {
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			JsonObject json = (JsonObject) new JsonParser().parse(parents);
			List<String> old_parents = db.getParents( entity );
			for( String old_parent : old_parents ) {
				db.removeEntity( entity, old_parent );
			}
			JsonArray a = json.get("list").getAsJsonArray();
			for (int i = 0; i < a.size(); i++) {
				String parent = a.get(i).getAsString();
				db.assignEntity(entity, parent);
			}
		} finally {
			DBConnector.closeDB(db);
		}
	}
	
	@GET @Path("/{domain}/{entity}/parents")
	@Produces("application/json")
	/**
	 * Returns a list of parents. 
	 * @param domain
	 * @param entity
	 * @param token
	 * @return
	 */
	public String getParents(@DefaultValue("Playground") @PathParam("domain") String domain,
			@PathParam("entity") String entity,
			@DefaultValue("") @HeaderParam("token") String token ) {
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			JsonObject json = new JsonObject();
			JsonArray array = new JsonArray();
			for (String e : db.getParents(entity)) {
				array.add(new JsonPrimitive(e));
			}
			json.add("entities", array);
			return json.toString();
		} finally {
			DBConnector.closeDB(db);
		}
	}

	@POST @Path("/{domain}/{entity}/children")
	@Produces("application/json")
	public void setChildren(
			@DefaultValue("Playground") @PathParam("domain") String domain,
			@PathParam("entity") String entity, 
			String children, //@HeaderParam("json")
			@DefaultValue("") @HeaderParam("token") String token ) {
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			JsonObject json = (JsonObject) new JsonParser().parse(children);
			List<String> old_children = db.getChildren( entity );
			for( String old_child : old_children ) {
				db.removeEntity( old_child, entity );
			}
			JsonArray a = json.get("list").getAsJsonArray();
			List<String> scope = db.getScope( db.layerOf( entity ) );
			for (int i = 0; i < a.size(); i++) {
				String child = a.get(i).getAsString();
				if( "c30".equals( child ) )
					if( "c47".equals( entity ) )
						System.out.print("");
				if( child.equals( entity ) ) continue;
				if( hasDescendant( child, entity, db) ) continue;
				if( !scope.contains( db.layerOf( child ) ) ) continue;
				
				db.assignEntity( child, entity );
			}
		} finally {
			DBConnector.closeDB(db);
		}
	}

	/**
	 * returns direct parents and children of an entity (not the whole hierarchy)
	 * @param domain
	 * @param entity
	 * @param token
	 * @return
	 */
	@GET	@Path("/{domain}/{entity}/hierarchy")
	@Produces("application/json")
	public String getHierarchyInfo(@DefaultValue("Playground") @PathParam("domain") String domain,
			@PathParam("entity") String entity,
			@DefaultValue("") @HeaderParam("token") String token) {
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			JsonObject json = new JsonObject();
			JsonArray array = new JsonArray();
			for (String e : db.getParents(entity)) {
				array.add(new JsonPrimitive(e));
			}
			json.add("parents", array);
			array = new JsonArray();
			for (String e : db.getChildren(entity)) {
				array.add(new JsonPrimitive(e));
			}
			json.add("children", array);
			return json.toString();
		} finally {
			DBConnector.closeDB(db);
		}
	}
	
	

	@GET	@Path("/{domain}/{entity}/rdcs/list")
	public String listRDCs(@DefaultValue("Playground") @PathParam("domain") String domain,
			@PathParam("entity") String entityName,
			@DefaultValue("") @HeaderParam("token") String token ) {
		JsonObject o = new JsonObject();
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			for (RDC rdc : RDCFactory.get().listRDCs()) {
				String rdcName = rdc.getName();
				boolean enabled = db.isRDCEnabled(entityName, rdcName);
				if (enabled) {
					JsonObject jrdc = new JsonObject();
					JsonObject params = new JsonObject();
					for (RDCParameter par : rdc.getParameterList()) {
						params.addProperty(par.getName(), db.getRDCParmeter(entityName, rdcName, par.getName(), ""));
					}
					jrdc.addProperty("enabled", enabled);
					jrdc.add("params", params);
					o.add(rdc.getName(), jrdc);
				}
			}
			System.out.println("Returning enabled rdcs list: " + o.toString());
			return o.toString();
		} finally {
			DBConnector.closeDB(db);
		}
	}

	@POST	@Path("/{domain}/{entity}/rdcs/store")
	public void setRDCs(@DefaultValue("Playground") @PathParam("domain") String domain,
			@PathParam("entity") String entityName, 
			@DefaultValue("") @HeaderParam("token") String token,
			String rdcmapString) { //@HeaderParam("json")
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			System.out.println("Received: " + rdcmapString);
			JsonObject json = (JsonObject) new JsonParser().parse(rdcmapString);
			for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
				String rdcName = entry.getKey();
				JsonObject o = entry.getValue().getAsJsonObject();

				// Do we need to add this check?
				if (RDCFactory.get().getRDC(rdcName) == null)
					continue;

				boolean enabled = false;

				try {
					enabled = "true".equals(entry.getValue().getAsJsonObject().get("enabled").getAsString());
				} catch (Exception ex) {
				}

				db.setRDCEnabled(entityName, rdcName, enabled);
				if (enabled) {
					for (RDCParameter par : RDCFactory.get().getRDC(rdcName).getParameterList()) {
						String value = o.get("params").getAsJsonObject().get(par.getName()).getAsString().toString();
						db.setRDCParmeter(entityName, rdcName, par.getName(), value);
					}
				}
			}
		} finally {
			DBConnector.closeDB(db);
		}
	}
	
	@GET	@Path("/{domain}/{entity}/rdcs/newrun")
	@Produces("application/json")
	public String runRDCS(@DefaultValue("Playground") @PathParam("domain") String domain,
			@PathParam("entity") String entityName,
			@DefaultValue("") @HeaderParam("token") String token ) {
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			JsonObject json = new JsonObject();
			String msg = "Data successfully stored in the data repository";
			for (RDC rdc : RDCFactory.get().listRDCs()) {
				String rdcName = rdc.getName();
				boolean enabled = db.isRDCEnabled(entityName, rdcName);
				if (enabled) {
					JsonObject o = new JsonObject();
					for (RDCParameter par : rdc.getParameterList()) {
						rdc.setParameter(par.getName(), db.getRDCParmeter(entityName, rdcName, par.getName(), ""));
					}
					try {
						Map<String, RiskData> values = rdc.getIndicators(entityName);
						if (values == null) {
							throw new Exception("The RDC '" + rdcName + "' returned an empty map for the entity '"
									+ entityName + "'");
						}
						for (String key : values.keySet()) {
							RiskData rd = values.get(key);
							try {
								db.storeRiskData(rd.toJSON());
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						o.addProperty("result", "ok");
						json.add(rdcName, o);
					} catch (Exception ex) {
						ex.printStackTrace();
						msg = "Some data were not gathered and/or stored in the RDR";
						o.addProperty("result", "error");
						o.addProperty("error-message", ex.getMessage());
						json.add(rdcName, o);
					}
				}
			}
			json.addProperty("msg", msg);
			System.out.println("Returning newrun: " + json.toString());
			return json.toString();
		} finally {
			DBConnector.closeDB(db);
		}
	}

	
	@GET 	@Path("/{domain}/{entity}/rd")
	public String getRiskData(@DefaultValue("Playground") @PathParam("domain") String domain,
			@PathParam("entity") String entity,
			@DefaultValue("") @HeaderParam("token") String token ) {
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			JsonObject json = new JsonObject();
			JsonArray array = new JsonArray();
			for (String id : db.listRiskData(entity)) {
				JsonObject o = (JsonObject) new JsonParser().parse(db.readRiskData(entity, id));
				array.add(o);
			}
			json.add("list", array);
			return json.toString();
		} finally {
			DBConnector.closeDB(db);
		}
	}

	@GET	@Path("/{domain}/{entity}/ras")
	@Produces("application/json")
	public String getRAD(@DefaultValue("Playground") @PathParam("domain") String domain,
			@PathParam("entity") String entity,
			@DefaultValue("") @HeaderParam("token") String token ) {
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			return db.readRASResult(entity);
		} finally {
			DBConnector.closeDB(db);
		}
	}
	
	@GET @Path("/{domain}/{entity}/candidatechildren")
	@Info("Returns a list of entities that can be set as child of the given entity. Thi is useful to avoid circles.")
	public String getCandidateChildren( 
			@PathParam("domain") @Info("The context domain") String domain,
			@PathParam("entity") @Info("The candidate parent entity") String entity,
			@DefaultValue("") @HeaderParam("token") String token ) {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB(domain, token);
			
			List<String> scope = db.getScope( db.layerOf( entity ) );
			
			List<String> entities = new ArrayList<String>();
			
			for( String layer : scope ) {
				
				for( String candidate : db.entities( layer ) ) {
					
					if( !hasDescendant( candidate, entity, db) ) {
						entities.add( candidate );
					}
					
				}
				
			}
			
			return new Gson().toJson( entities );
			
		} finally {
			DBConnector.closeDB(db);
		}
//		return "";
	}
	
	@GET @Path("/{domain}/{entity}/candidateparents")
	@Info("Returns a list of entities that can be set as parent of the given entity. Thi is useful to avoid circles.")
	public String getCandidateParents( 
			@PathParam("domain") @Info("The context domain") String domain,
			@PathParam("entity") @Info("The candidate child entity") String entity,
			@DefaultValue("") @HeaderParam("token") String token ) {
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			
			Collection<String> layers = db.layerNames();
			
			List<String> entities = new ArrayList<String>();
			
			String l = db.layerOf( entity );
			
			for( String layer : layers ) {
				
				for( String candidate : db.entities( layer ) ) {
					
					if( !hasAncestor( candidate, entity, db) ) {
						entities.add( candidate );
					}
					
				}
				
				if( layers.equals( l ) ) {
					break;
				}
				
			}
			
			return new Gson().toJson( entities );
			
		} finally {
			DBConnector.closeDB(db);
		}
//		return "";
	}
	
	static class EntityFinder extends TraverseCallback<String> {
		
		boolean found = false;
		
		public EntityFinder(String storedValue) {
			super(storedValue);
		}
		@Override
		public boolean onEntityFound( String entity ) {
			found = getValue().equals( entity );
			return found;
		}
		
	}
	
	boolean hasDescendant( String entity, String descendant, RiscossDB db ) {
		
		DownwardEntitySearch ett = new DownwardEntitySearch( db );
		
		EntityFinder finder = new EntityFinder( descendant );
		
		ett.analyseEntity( entity, finder );
		
		return finder.found;
		
	}
	
	boolean hasAncestor( String entity, String ancestor, RiscossDB db ) {
		
		DownwardEntitySearch ett = new DownwardEntitySearch( db, DIRECTION.UP );
		
		EntityFinder finder = new EntityFinder( ancestor );
		
		ett.analyseEntity( entity, finder );
		
		return finder.found;
		
	}
	
}
