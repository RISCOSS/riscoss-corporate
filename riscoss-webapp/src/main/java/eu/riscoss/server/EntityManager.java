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

import eu.riscoss.client.Log;
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
@Info("Management of entities")
public class EntityManager {

	@GET @Path("/{domain}/list")
	@Info("Returns a list of the entities sotred in the given domain")
	public String list(
			@PathParam("domain") @Info("The selected domain")			String domain,
			@HeaderParam("token") @Info("The authentication token")		String token 
			) throws Exception {

		JsonArray a = new JsonArray();

		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
			for (String name : db.entities()) {
				JsonObject o = new JsonObject();
				o.addProperty("name", name);
				o.addProperty("layer", db.layerOf(name));
				a.add(o);
			}
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
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
	 * @throws Exception 
	 */
	@GET @Path("/{domain}/{layer}/list")
	@Info("Returns the list of entities in a specific layer")
	public String list(
			@PathParam("domain") @Info("The selected domain")			String domain,
			@PathParam("layer") @Info("The selected layer")				String layer, 
			@HeaderParam("token") @Info("The authentication token")		String token
			) throws Exception {

		//TODO: check if layer exists!
		JsonArray a = new JsonArray();

		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB(domain, token);
			for (String name : db.entities(layer)) {
				JsonObject o = new JsonObject();
				o.addProperty("name", name);
				o.addProperty("layer", layer);
				a.add(o);
			}
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}

		return a.toString();

	}
	
	@GET @Path("/{domain}/search") 
	@Info("Returns a list of entities that match the specified parameters across all layers")
	public String search(
			@PathParam("domain") @Info("The selected domain")			String domain, 
			@HeaderParam("token") @Info("The authentication token")		String token,
			@QueryParam("query")										String query
			) throws Exception {
		return searchNew( domain, token, "", query, "0", "0", "false", "" );
	}

	@GET @Path("/{domain}/{layer}/search")
	@Info("Returns a list of entities that match the specified parameters in a specified layer")
	public String searchNew(
			@PathParam("domain") @Info("The selected domain")			String domain, 
			@HeaderParam("token") @Info("The authentication token")		String token,
			@DefaultValue("") @PathParam("layer")						String layer, 
			@DefaultValue("") @QueryParam("query")						String query, 
			@DefaultValue("0") @QueryParam("from")						String strFrom,
			@DefaultValue("0") @QueryParam("max")						String strMax, 
			@DefaultValue("f") @QueryParam("h")							String strHierarchy,
			@DefaultValue("") @QueryParam("f")							String flags
		) throws Exception {
		
		List<JEntityNode> result = new ArrayList<JEntityNode>();
		
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
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
			
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
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
	@Info("Creates a new entity and associates it to the given layer")
	@Produces("application/json")
	//TODO: remove parent. Extra call for adding parents.
	public String createEntity(
			@PathParam("domain") @Info("The selected domain") String domain,
			@QueryParam("name") String name, 
			@QueryParam("layer") String layer, 
			@QueryParam("parent") String parent,
			@HeaderParam("token") @Info("The authentication token") String token ) {

		// attention:filename sanitation is not directly notified to the user
		name = RiscossUtil.sanitize(name);

		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
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
			return "";
		} finally {
			DBConnector.closeDB(db);
		}
	}

	@DELETE @Path("/{domain}/{entity}/delete")
	@Info("Deleted the specified entity")
	public void deleteEntity(
			@PathParam("domain") @Info("The selected domain")			String domain,
			@PathParam("entity")										String entity,
			@HeaderParam("token") @Info("The authentication token")		String token
			) throws Exception {
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
			db.removeEntity(entity);
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
	}

	@GET @Path("/{domain}/{entity}/data")
	@Info("Returns detailed information about the specified entity")
	public String getEntityData(
			@PathParam("domain") @Info("The selected domain")			String domain,
			@PathParam("entity")										String entity,
			@HeaderParam("token") @Info("The authentication token")		String token
			) throws Exception {
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
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
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
	}

	//currently not used!
	@GET @Path("/{domain}/{entity}/data_new")
	public String getEntityData_new(
			@PathParam("domain") @Info("The selected domain")		String domain,
			@PathParam("entity")									String entity,
			@HeaderParam("token") @Info("The authentication token")	String token
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
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
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
	}
	
	@POST @Path("/{domain}/{entity}/rename")
	@Info("Rename the specified entity")
	public void renameEntity(
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("entity") @Info("The name of an existing entity")	String name, 
			@QueryParam("newname") @Info("The new name of the entity")		String newName
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			db.renameEntity(name, newName);
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
	}

	@POST @Path("/{domain}/{entity}/parents")
	@Produces("application/json")
	@Info("Sets the specified entity as the child for all the given parent entities")
	public void setParents(
			@PathParam("domain") @Info("The selected domain")			String domain,
			@PathParam("entity")										String entity, 
			@HeaderParam("token") @Info("The authentication token")		String token,
			String parents
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
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
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
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
	@Info("Returns a list of parents")
	public String getParents(
			@PathParam("domain") @Info("The selected domain")			String domain,
			@PathParam("entity")										String entity,
			@HeaderParam("token") @Info("The authentication token")		String token
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			JsonObject json = new JsonObject();
			JsonArray array = new JsonArray();
			for (String e : db.getParents(entity)) {
				array.add(new JsonPrimitive(e));
			}
			json.add("entities", array);
			return json.toString();
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
	}
	
	@POST @Path("/{domain}/{entity}/children")
	@Produces("application/json")
	@Info("Sets the given list of entities as children of the specified parent entity")
	public void setChildren(
			@PathParam("domain") @Info("The selected domain")			String domain,
			@PathParam("entity")										String entity, 
			@HeaderParam("token") @Info("The authentication token")		String token,
			String children
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			JsonObject json = (JsonObject) new JsonParser().parse(children);
			List<String> old_children = db.getChildren( entity );
			for( String old_child : old_children ) {
				db.removeEntity( old_child, entity );
			}
			JsonArray a = json.get("list").getAsJsonArray();
			List<String> scope = db.getScope( db.layerOf( entity ) );
			for (int i = 0; i < a.size(); i++) {
				String child = a.get(i).getAsString();
				if( child.equals( entity ) ) continue;
				if( hasDescendant( child, entity, db) ) continue;
				if( !scope.contains( db.layerOf( child ) ) ) continue;
				
				db.assignEntity( child, entity );
			}
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
	}
	
	@GET @Path("/{domain}/{entity}/candidatechild")
	public String isCandidateChild(
			@PathParam("domain") @Info("The selected domain") String domain,
			@PathParam("entity") String entity, 
			@QueryParam("child") String child,
			@HeaderParam("token") @Info("The authentication token") String token ) throws Exception {
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB(domain, token);
			if( child.equals( entity ) ) throw new Exception("Invalid candidate child: an entity can not be child of itself");
			if( hasDescendant( child, entity, db) ) throw new Exception("Invalid candidate child: cycle detected");
			List<String> scope = db.getScope( db.layerOf( entity ) );
			if( !scope.contains( db.layerOf( child ) ) ) throw new Exception("Invalid candidate child: must be in a layer equal or lower to the parent entity");
			return "Ok";
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/{entity}/candidateparent")
	public String isCandidateParent(
			@PathParam("domain") @Info("The selected domain") String domain,
			@PathParam("entity") String entity, 
			@QueryParam("child") String child,
			@HeaderParam("token") @Info("The authentication token") String token ) throws Exception {
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB(domain, token);
			if( child.equals( entity ) ) throw new Exception("Invalid candidate child: an entity can not be parent of itself");
			if( hasAncestor( child, entity, db) ) throw new Exception("Invalid candidate child: cycle detected");
			List<String> scope = db.getScope( db.layerOf( child ) );
			if( !scope.contains( db.layerOf( entity ) ) ) throw new Exception("Invalid candidate child: must be in a layer equal or higher to the parent entity");
			return "Ok";
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	/**
	 * returns direct parents and children of an entity (not the whole hierarchy)
	 * @param domain
	 * @param entity
	 * @param token
	 * @return
	 * @throws Exception 
	 */
	@GET	@Path("/{domain}/{entity}/hierarchy")
	@Produces("application/json")
	public String getHierarchyInfo(
			@PathParam("domain") @Info("The selected domain") String domain,
			@PathParam("entity") String entity,
			@HeaderParam("token") @Info("The authentication token") String token) throws Exception {
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
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
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
	}
	
	

	@GET	@Path("/{domain}/{entity}/rdcs/list")
	public String listRDCs(
			@PathParam("domain") @Info("The selected domain") String domain,
			@PathParam("entity") String entityName,
			@HeaderParam("token") @Info("The authentication token") String token ) throws Exception {
		JsonObject o = new JsonObject();
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
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
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
	}

	@POST	@Path("/{domain}/{entity}/rdcs/store")
	public void setRDCs(
			@PathParam("domain") @Info("The selected domain") String domain,
			@PathParam("entity") String entityName, 
			@HeaderParam("token") @Info("The authentication token") String token,
			String rdcmapString) throws Exception {
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
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
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
	}
	
	@GET	@Path("/{domain}/{entity}/rdcs/newrun")
	@Produces("application/json")
	public String runRDCS(
			@PathParam("domain") @Info("The selected domain") String domain,
			@PathParam("entity") String entityName,
			@HeaderParam("token") @Info("The authentication token") String token ) throws Exception {
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
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
						DBConnector.closeDB( db );
						Map<String, RiskData> values = rdc.getIndicators(entityName);
						db = DBConnector.openDB(domain, token);
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
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
	}

	
	@GET @Path("/{domain}/{entity}/rd")
	public String getRiskData(
			@PathParam("domain") @Info("The selected domain") String domain,
			@PathParam("entity") String entity,
			@HeaderParam("token") @Info("The authentication token") String token ) throws Exception {
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
			JsonObject json = new JsonObject();
			JsonArray array = new JsonArray();
			for (String id : db.listRiskData(entity)) {
				JsonObject o = (JsonObject) new JsonParser().parse(db.readRiskData(entity, id));
				array.add(o);
			}
			json.add("list", array);
			return json.toString();
		} catch (Exception e) {
			throw e;
		} finally {
			DBConnector.closeDB(db);
		}
	}

	@GET	@Path("/{domain}/{entity}/ras")
	@Produces("application/json")
	public String getRAD(
			@PathParam("domain") @Info("The selected domain") String domain,
			@PathParam("entity") String entity,
			@HeaderParam("token") @Info("The authentication token") String token ) throws Exception {
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
			return db.readRASResult(entity);
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
	}
	
	@GET @Path("/{domain}/{entity}/candidatechildren")
	@Info("Returns a list of entities that can be set as child of the given entity. Thi is useful to avoid circles.")
	public String getCandidateChildren( 
			@PathParam("domain") @Info("The selected domain") String domain,
			@PathParam("entity") @Info("The candidate parent entity") String entity,
			@HeaderParam("token") @Info("The authentication token") String token ) throws Exception {
		
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
			
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
//		return "";
	}
	
	@GET @Path("/{domain}/{entity}/candidateparents")
	@Info("Returns a list of entities that can be set as parent of the given entity. Thi is useful to avoid circles.")
	public String getCandidateParents( 
			@PathParam("domain") @Info("The selected domain")				String domain,
			@PathParam("entity") @Info("The candidate child entity")		String entity,
			@HeaderParam("token") @Info("The authentication token")			String token 
			) throws Exception {
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
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
			
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
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
