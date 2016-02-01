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

import java.util.List;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.RiscossElements;
import eu.riscoss.shared.JLayerContextualInfo;
import eu.riscoss.shared.RiscossUtil;

@Path("layers")
public class LayersManager {
	
	Gson gson = new Gson();
	
	public LayersManager() {
	}
	
	@GET @Path("/{domain}/list")
	@Info("Returns a list of existing layers")
	public String list( 
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token 
			) throws Exception {
		
		JsonArray a = new JsonArray();
		
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
			for( String layer : db.layerNames() ) {
				JsonObject o = new JsonObject();
				o.addProperty( "name", layer );
				a.add( o );
			}
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
		
		return a.toString();
	}
	
//	//@POST @Path("{domain}/new")
//	@Deprecated
//	public void createNew_old(
//			@PathParam("domain") String domain,
//			@HeaderParam("token") String token, 
//			@QueryParam("name") String name,
//			@QueryParam("parent") String parentName
//			) throws Exception {
//		//attention:filename sanitation is not directly notified to the user
//		name = RiscossUtil.sanitize(name.trim());
//		
//		parentName = parentName.trim();
//		
//		RiscossDB db = null;
//		try {
//			db = DBConnector.openDB( domain, token );
//			db.addLayer( name, parentName );
//		}
//		catch( Exception ex ) {
//			throw ex;
//		}
//		finally {
//			DBConnector.closeDB( db );
//		}
//	}
	
	@POST @Path("{domain}/create")
	@Info("Creates a new layer")
	public void createNew(
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@QueryParam("name") @Info("The name of the layer")				String name,
			@QueryParam("parent") @Info("The name of the layer immediately above the new one, or an empty string if the new layer has to be the topmost one")
																			String parentName
			) throws Exception {
		//attention:filename sanitation is not directly notified to the user
		name = RiscossUtil.sanitize(name.trim());
		
		parentName = parentName.trim();
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			db.addLayer( name, parentName );
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("{domain}/{layer}/edit-parent")
	@Info("Edits the parent of an existing layer")
	public void editParent(
			@PathParam("domain") @Info("The work domain") 					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token,
			@PathParam("layer") @Info("The name of the layer to edit")		String name,
			@QueryParam("newParent") @Info("The name of the new parent") 	String parent
			) throws Exception {
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token);
			
			db.editParent(name, parent);
			
		} catch (Exception ex) {
			throw ex;
		} finally {
			DBConnector.closeDB( db );
		}
	}
	
	@DELETE @Path("{domain}/{layer}/delete")
	@Info("Deltes an existing layer")
	public void deleteLayer( 
			@PathParam("domain") @Info("The work domain")					String domain, 
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("layer") @Info("The name of the layer to delete")	String name
			) throws Exception {
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			if( db.entities( name ).size() > 0 ) {
				throw new Exception( "You can not delete a layer that still contains entities. You must delete all the entities befor being able to delete this layer." );
			}
			db.removeLayer( name );
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
		
	}
	
	@GET @Path( "{domain}/{layer}/ci" )
	@Info("Returns the contextual information associated to a layer")
	public String getContextualInfo( 
			@PathParam("domain") @Info("The work domain")					String domain, 
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("layer") @Info("The name of the layer")				String layer
			) throws Exception {
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			String json = db.getLayerData( layer, "ci" );
			if( json == null ) {
				JLayerContextualInfo info = new JLayerContextualInfo();
				json = gson.toJson( info );
			}
			return json;
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
		
	}
	
	@POST @Path( "{domain}/{layer}/ci" )
	@Info("Associates contextual information to a layer")
	public void setContextualInfo( 
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("layer") @Info("The name of the layer")				String layer, 
			String json 
			) throws Exception {
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			db.setLayerData( layer, "ci", json );
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
		
	}
	
	@POST @Path("{domain}/{layer}/rename")
	@Info("Changes the name of a layer")
	public void editLayer( 
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("layer") @Info("The name of an existing layer")		String name, 
			@QueryParam("newname") @Info("The new name of the layer")		String newName
			) throws Exception {
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			db.renameLayer( name, newName );
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/{layer}/scope")
	@Info("Returns the scope of a layer; a scope is an ordered set that contains the target layer and its sub-layers")
	public String getScope( 
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token,
			@PathParam("layer") @Info("The name of an existing layer")		String layer
			) throws Exception {
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			List<String> scope = db.getScope( layer );
			return gson.toJson( scope );
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/{layer}/description")
	public String getDescription(
			@PathParam("domain") @Info("The selected domain") 			String domain,
			@PathParam("layer") @Info("The name of an existing layer")	String entity,
			@HeaderParam("token") @Info("The authentication token") 	String token
			) throws Exception {
		RiscossDB db = null;
		try {
			db = DBConnector.openDB(domain, token);
			return db.getProperty( RiscossElements.LAYER, entity, "description", "" );
		} catch (Exception e) {
			throw e;
		} finally {
			DBConnector.closeDB(db);
		}
	}
	
	@POST @Path("/{domain}/{layer}/description")
	public void setDescription(
			@PathParam("domain") @Info("The selected domain") 			String domain,
			@PathParam("layer") @Info("The name of an existing layer")	String layer,
			@HeaderParam("token") @Info("The authentication token") 	String token,
			@Info("The description string to be set")					String description
			) throws Exception {
		RiscossDB db = null;
		try {
			db = DBConnector.openDB(domain, token);
			db.setProperty( RiscossElements.LAYER, layer, "description", description );
		} catch (Exception e) {
			throw e;
		} finally {
			DBConnector.closeDB(db);
		}
	}
	
}