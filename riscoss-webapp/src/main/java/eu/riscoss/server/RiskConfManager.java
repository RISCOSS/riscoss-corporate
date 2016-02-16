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
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.RiscossElements;
import eu.riscoss.db.SearchParams;
import eu.riscoss.shared.JModelNode;
import eu.riscoss.shared.JRCNode;
import eu.riscoss.shared.JRiskConfiguration;
import eu.riscoss.shared.JRiskConfigurationLayerInfo;
import eu.riscoss.shared.RiscossUtil;

@Path("rcs")
public class RiskConfManager {
	
	@GET @Path( "/{domain}/list" )
	@Info("Returns a list of existing risk configurations")
	public String list(
			@PathParam("domain") @Info("The work domain")				String domain,
			@HeaderParam("token") @Info("The authentication token")		String token, 
			@QueryParam("entity") @Info("En entity name; if specified, only the risk configurations" + 
											" targeting this entity will be listed.")
																		String entity 
			) throws Exception {
		
		RiscossDB db = null;
		
		JsonArray a = new JsonArray();
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			if( entity != null ) {
				if( !entity.equals( "" ) ) {
					String layer = db.layerOf( entity );
					List<String> rcs = db.findCandidateRCs( layer );
					for( String rc : rcs ) {
						JsonObject o = new JsonObject();
						o.addProperty( "name", rc );
						a.add( o );
					}
					return a.toString();
				}
			}
			
			for( String rc : db.getRiskConfigurations() ) {
				JsonObject o = new JsonObject();
				o.addProperty( "name", rc );
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
	
	@GET @Path("/{domain}/search") 
	@Info("Returns a list of rcs that match the specified parameters")
	public String search(
			@PathParam("domain") @Info("The selected domain")			String domain, 
			@HeaderParam("token") @Info("The authentication token")		String token,
			@QueryParam("query")										String query
			) throws Exception {
		return searchNew( domain, token, query, "0", "0");
	}

	@GET @Path("/{domain}/search-rcs")
	@Info("Returns a list of rcs that match the specified parameters")
	public String searchNew(
			@PathParam("domain") @Info("The selected domain")											String domain, 
			@HeaderParam("token") @Info("The authentication token")										String token,
			@DefaultValue("") @QueryParam("query") @Info("The actual query (on the rc name)")			String query, 
			@DefaultValue("0") @QueryParam("from") @Info("Index of the first rc (for pagination)")		String strFrom,
			@DefaultValue("0") @QueryParam("max") @Info("Amount of rcs to search")						String strMax
		) throws Exception {
		
		List<JRCNode> result = new ArrayList<JRCNode>();
		
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
			SearchParams params = new SearchParams();
			params.setMax( strMax );
			params.setFrom( strFrom );
			
			List<String> rcs = new ArrayList<String>();
			
			Collection<String> list = db.findRCs(query, params);
			for (String name : list) {
				JRCNode jd = new JRCNode();
				jd.name = name;
				result.add(jd);
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
	
	/**
	 * For a risk configuration name, returns json with list of *models* associated, and layers with their entities,    
	 * @param domain
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	@GET @Path("/{domain}/{rc}/get")
	@Info("For a risk configuration name, returns json with list of associated models")
	public String getContent( 
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("rc") @Info("The name of a risk configuration")		String name 
			) throws Exception {
		
		JsonObject json = new JsonObject();
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			
			json.addProperty( "name", name );
			json.addProperty( "type", "layered" );
			{
				JsonArray array = new JsonArray();
				for( String modelName : db.getModelsFromRiskCfg( name, null ) ) {
					JsonObject o = new JsonObject();
					o.addProperty( "model", modelName );
					array.add( o );
				}
				json.add( "models", array );
			}
			{
				Map<String,ArrayList<String>> map = db.getRCModels( name );
				JsonArray jlayers = new JsonArray();
				for( String layer : db.layerNames() ) {
					JsonObject jlayer = new JsonObject();
					jlayer.addProperty( "name", layer );
					ArrayList<String> models = map.get( layer );
					if( models == null ) models = new ArrayList<String>();
					JsonArray jmodels = new JsonArray();
					for( String model : models ) {
						jmodels.add( new JsonPrimitive( model ) );
					}
					jlayer.add( "models", jmodels );
					jlayers.add( jlayer );
				}
				json.add( "layers", jlayers );
			}
			return json.toString();
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	/**
	 * For a risk configuration name, returns json with list of *models* associated, and layers with their entities
	 * 
	 * This version uses the JRiskConfiguration class, instead of raw JSON
	 * 
	 * @param domain
	 * @param name
	 * @return
	 * @throws Exception 
	 */
	@GET @Path("/{domain}/{rc}/get_new")
	@Info("For a risk configuration name, returns json with list of associated models")
	public String getContentNew( 
			@PathParam("domain") @Info("The work domain")							String domain,
			@HeaderParam("token") @Info("The authentication token")					String token, 
			@PathParam("rc") @Info("The name of the risk configuration")			String name 
			) throws Exception {
		
		JRiskConfiguration jrc = new JRiskConfiguration();
		
		RiscossDB db = null;
		
		try {
			db = DBConnector.openDB( domain, token );
			
			jrc.name = name;
			jrc.type = "layered";
			{
				for( String modelName : db.getModelsFromRiskCfg( name, null ) ) {
					jrc.models.add( modelName );
				}
			}
			{
				Map<String,ArrayList<String>> map = db.getRCModels( name );
				for( String layer : db.layerNames() ) {
					JRiskConfigurationLayerInfo jlayer = new JRiskConfigurationLayerInfo();
					jlayer.name = layer;
					ArrayList<String> models = map.get( layer );
					if( models == null ) models = new ArrayList<String>();
					for( String model : models ) {
						jlayer.models.add( model );
					}
					jrc.layers.add( jlayer );
				}
			}
			return new Gson().toJson( jrc );
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/{rc}/store")
	@Info("Saves a risk configuration")
	public void setContent( 
			@PathParam("domain") @Info("The work domain")							String domain,
			@HeaderParam("token") @Info("The authentication token")					String token, 
			@PathParam("rc") @Info("The name of the risk configuration")			String name, 
			@Info("A JSON Object containing a list of models for each layer")		String riskConfigs 
			) throws Exception {
		
		JsonObject json = (JsonObject)new JsonParser().parse( riskConfigs );
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			//String rc = json.get( "name" ).getAsString();
			try {
				List<String> list = new ArrayList<String>();
				JsonArray array = json.get( "models" ).getAsJsonArray();
				for( int i = 0; i < array.size(); i++ ) {
					list.add( array.get( i ).getAsJsonObject().get( "name" ).getAsString() );
				}
				db.setModelsFromRiskCfg( name, list );
			}
			catch( Exception ex ) {}
			try {
				Map<String,ArrayList<String>> map = new HashMap<String,ArrayList<String>>();
				JsonArray jlayers = json.get( "layers" ).getAsJsonArray();
				for( int i = 0; i < jlayers.size(); i++ ) {
					JsonObject jlayer = jlayers.get( i ).getAsJsonObject();
					String layer = jlayer.get( "name" ).getAsString();
					JsonArray jmodels = jlayer.get( "models" ).getAsJsonArray();
					ArrayList<String> models = new ArrayList<String>();
					for( int m = 0; m < jmodels.size(); m++ ) {
						models.add( jmodels.get( m ).getAsString() );
					}
					map.put( layer, models );
				}
				db.setRCModels( name, map );
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/{rc}/store_new")
	@Info("Saves a risk configuration (this version uses an automatically serialized json object)")
	public void setContentNew( 
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("rc") @Info("The name of the risk configuration")	String name, 
			String riskConfigs 
			) throws Exception {
		
		JRiskConfiguration rc = new Gson().fromJson( riskConfigs, JRiskConfiguration.class ); // (JsonObject)new JsonParser().parse( riskConfigs );
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			
			try {
				db.setModelsFromRiskCfg( name, rc.models );
			}
			catch( Exception ex ) {}
			try {
				Map<String,ArrayList<String>> map = new HashMap<String,ArrayList<String>>();
				for( JRiskConfigurationLayerInfo rcli : rc.layers ) {
					map.put( rcli.name, rcli.models );
				}
				db.setRCModels( name, map );
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/create")
	@Info("Creates a new risk configuration")
	public String createNew( 
			@PathParam("domain") @Info("The work domain")							String domain,
			@HeaderParam("token") @Info("The authentication token")					String token, 
			@QueryParam("name") @Info("The name 0f the risk configuration")			String name 
			) throws Exception {
		RiscossDB db = null;
		//attention:filename sanitation is not directly notified to the user
		name = RiscossUtil.sanitize(name);
		try {
			db = DBConnector.openDB( domain, token );
			db.createRiskConfiguration( name );
			JsonObject o = new JsonObject();
			o.addProperty( "name", name );
			return o.toString();
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@DELETE @Path("/{domain}/{rc}/delete")
	@Info("Deletes a risk configuration")
	public String delete( 
			@PathParam("domain") @Info("The work domain")							String domain,
			@HeaderParam("token") @Info("The authentication token")					String token, 
			@PathParam("rc") @Info("The name of the risk configuration")			String name 
			) throws Exception {
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			db.removeRiskConfiguration( name );
			JsonObject o = new JsonObject();
			o.addProperty( "name", name );
			return o.toString();
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/{rc}/description")
	public String getDescription(
			@PathParam("domain") @Info("The selected domain") 			String domain,
			@PathParam("rc") @Info("The selected rc")					String rc,
			@HeaderParam("token") @Info("The authentication token") 	String token
			) throws Exception {
		RiscossDB db = null;
		try {
			db = DBConnector.openDB(domain, token);
			return db.getProperty( RiscossElements.RISKCONF, rc, "description", "" );
		} catch (Exception e) {
			throw e;
		} finally {
			DBConnector.closeDB(db);
		}
	}
	
	@POST @Path("/{domain}/{rc}/description")
	public void setDescription(
			@PathParam("domain") @Info("The selected domain") 			String domain,
			@PathParam("rc") @Info("The selected rc")					String rc,
			@HeaderParam("token") @Info("The authentication token") 	String token,
			@Info("The description string to be set")					String description
			) throws Exception {
		RiscossDB db = null;
		try {
			db = DBConnector.openDB(domain, token);
			db.setProperty( RiscossElements.RISKCONF, rc, "description", description );
		} catch (Exception e) {
			throw e;
		} finally {
			DBConnector.closeDB(db);
		}
	}
	
}