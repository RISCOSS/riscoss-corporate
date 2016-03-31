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

import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.gargoylesoftware.htmlunit.javascript.host.Console;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import eu.riscoss.client.Log;
import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;
import eu.riscoss.db.RecordAbstraction;
import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.RiskAnalysisSession;
import eu.riscoss.db.RiskScenario;
import eu.riscoss.db.SearchParams;
import eu.riscoss.ram.MitigationActivity;
import eu.riscoss.ram.RiskAnalysisManager;
import eu.riscoss.ram.algo.DownwardEntitySearch;
import eu.riscoss.ram.algo.TraverseCallback;
import eu.riscoss.ram.rae.Argument;
import eu.riscoss.ram.rae.Argumentation;
import eu.riscoss.reasoner.Chunk;
import eu.riscoss.reasoner.DataType;
import eu.riscoss.reasoner.Distribution;
import eu.riscoss.reasoner.Evidence;
import eu.riscoss.reasoner.Field;
import eu.riscoss.reasoner.FieldType;
import eu.riscoss.reasoner.ModelSlice;
import eu.riscoss.reasoner.ReasoningLibrary;
import eu.riscoss.reasoner.RiskAnalysisEngine;
import eu.riscoss.server.ma.AHPAnalysis;
import eu.riscoss.shared.EAnalysisOption;
import eu.riscoss.shared.EAnalysisResult;
import eu.riscoss.shared.JArgument;
import eu.riscoss.shared.JArgumentation;
import eu.riscoss.shared.JLayerNode;
import eu.riscoss.shared.JMissingData;
import eu.riscoss.shared.JRASInfo;
import eu.riscoss.shared.JRASNode;
import eu.riscoss.shared.JRiskData;
import eu.riscoss.shared.JValueMap;
import eu.riscoss.shared.JWhatIfData;
import eu.riscoss.shared.JWhatIfData.JWhatIfItem;

@Path("analysis")
public class AnalysisManager {
	
	Gson gson = new Gson();
	
	static class MissingDataItem {
		String id;
		String label;
		String question;
		String description;
		String type;
		String value;
	}
	
	class AnalysisDataSource {
		RiskAnalysisSession ras;
		RiscossDB db;
		public AnalysisDataSource( RiskAnalysisSession ras, RiscossDB db) {
			this.ras = ras;
			this.db = db;
		}
	}
	
	@GET @Path( "/{domain}/session/list")
	@Info("Provides a list of all existing risk sessions")
	public String listRAS(
			@PathParam("domain") @Info("The work domain")				String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@QueryParam("entity") @Info("An entity name. If this field is specified, only the sessions targeting the entity are returned")
																			String entity, 
			@QueryParam("rc") @Info("A risk configuration name. If this field is specified, only the sessions that use this risk configuration are returned")
																			String rc 
			) throws Exception {
		
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
			JsonObject json = new JsonObject();
			JsonArray array = new JsonArray();
			for( RecordAbstraction record : db.listRAS( entity,  rc ) ) {
				array.add( gson.toJsonTree( 
						new JRASInfo( record.getName(), record.getProperty( "name", record.getName() ) ) ) );
			}
			json.add( "list", array );
			return json.toString();
		} 
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
		
	}
	
	@GET @Path("/{domain}/search") 
	@Info("Returns a list of ras that match the specified parameters")
	public String search(
			@PathParam("domain") @Info("The selected domain")			String domain, 
			@HeaderParam("token") @Info("The authentication token")		String token,
			@QueryParam("query")										String query,
			@QueryParam("target")										String target,
			@QueryParam("rc")											String rc
			) throws Exception {
		return searchNew( domain, token, query, target, rc, "0", "0");
	}
	
	@GET @Path("/{domain}/search-ras")
	@Info("Returns a list of ras that match the specified parameters")
	public String searchNew(
			@PathParam("domain") @Info("The selected domain")											String domain, 
			@HeaderParam("token") @Info("The authentication token")										String token,
			@DefaultValue("") @QueryParam("query") @Info("The actual query (on the ras name)")			String query, 
			@DefaultValue("") @QueryParam("target") @Info("Target entity of the query")					String target,
			@DefaultValue("") @QueryParam("rc") @Info("Risk configuration of the query")				String rc,
			@DefaultValue("0") @QueryParam("from") @Info("Index of the first ras (for pagination")		String strFrom,
			@DefaultValue("0") @QueryParam("max") @Info("Amount of ras to search")						String strMax
		) throws Exception {
				
		JsonArray a = new JsonArray();
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
			SearchParams params = new SearchParams();
			params.setMax( strMax );
			params.setFrom( strFrom );
			
			List<RecordAbstraction> list = db.findRAS(query, target, rc, params);
			for( RecordAbstraction record : list ) {
				JRASInfo jras = new JRASInfo( record.getName(), record.getProperty( "name", record.getName() ) );
				RiskAnalysisSession r = db.openRAS( jras.getId() );
				JsonObject json = new JsonObject();
				json.addProperty("id", r.getId());
				json.addProperty("target", r.getTarget());
				json.addProperty("rc", r.getRCName());
				json.addProperty("name", r.getName());
				try {
					Date date = new Date( r.getTimestamp() );
					SimpleDateFormat sdf = new SimpleDateFormat( "dd-MM-yyyy HH.mm.ss" );
					json.addProperty("timestamp", sdf.format( date ));
				} catch ( Exception ex ) {}
				a.add(json);
			}
			
			return a.toString();
			
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB(db);
		}
	}
	
	@POST @Path("/{domain}/session/list-results")
	@Info("Get the results of a given list of risk analysis session")
	public String getSessionListResults(
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token,
			@Info ("The list of entities")									String rasIds) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB(domain, token);
			
			JsonArray a = new JsonArray();
			JsonObject list = (JsonObject) new JsonParser().parse(rasIds);
			
			for (JsonElement id : list.get("list").getAsJsonArray()) {
				RiskAnalysisSession ras = db.openRAS( id.getAsString() );
				JsonObject json = new JsonObject();
				json.addProperty("id", ras.getId());
				json.addProperty("target", ras.getTarget());
				json.addProperty("rc", ras.getRCName());
				json.addProperty("name", ras.getName());
				json.addProperty("res", ras.readResults());
				try {
					Date date = new Date( ras.getTimestamp() );
					SimpleDateFormat sdf = new SimpleDateFormat( "dd-MM-yyyy HH.mm.ss" );
					json.addProperty("timestamp", sdf.format( date ));
				} catch ( Exception ex ) {}
				a.add(json);
			}
			
			return a.toString();
			
		} catch (Exception ex) {
			throw ex;
		} finally {
			DBConnector.closeDB(db);
		}
		
	}
	
	@GET @Path("/{domain}/session/last")
	@Info("Get a list of the last risk session for every entity and rc pair")
	public String getLastRiskSession(
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token") 		String token) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			JsonArray a = new JsonArray();
			HashMap<String, RiskAnalysisSession> raslist = new HashMap<>();
			
			List<String> list = new ArrayList<>();
						
			for( RecordAbstraction record : db.listRAS( "",  "" ) ) {
				JRASInfo jras = new JRASInfo( record.getName(), record.getProperty( "name", record.getName() ) );
				RiskAnalysisSession ras = db.openRAS( jras.getId() );
				String path = ras.getTarget() + "#" + ras.getRCName();
				if (!raslist.containsKey(path)) {
					raslist.put(path, ras);
				}
				else {
					if (raslist.get(path).getTimestamp() < ras.getTimestamp()) {
						raslist.remove(path);
						raslist.put(path, ras);
					}
				}
			}
			
			for (RiskAnalysisSession ras : raslist.values()) {
				JsonObject json = new JsonObject();
				json.addProperty( "id", ras.getId() );
				json.addProperty( "target", ras.getTarget() );
				json.addProperty( "rc", ras.getRCName() );
				json.addProperty( "name", ras.getName() );
				json.addProperty( "res", ras.readResults());
				try {
					Date date = new Date( ras.getTimestamp() );
					SimpleDateFormat sdf = new SimpleDateFormat( "dd-MM-yyyy HH.mm.ss" );
					json.addProperty( "timestamp", sdf.format( date ) );
				}
				catch( Exception ex ) {}
				a.add(json);
			}
			
			return a.toString();
			
		} catch ( Exception ex ) {
			throw ex;
		} finally {
			DBConnector.closeDB( db );
		}
		
	}
	
	@POST @Path("/{domain}/session/{sid}/rename")
	@Info("Rename an existing risk analysis session")
	public void renameSession(
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token,
			@PathParam("sid") @Info("The session id")						String sid,
			@QueryParam("newname") @Info("The session new name")			String newName) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			RiskAnalysisSession ras = db.openRAS( sid );

			ras.setName(newName);
			
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/session/create")
	@Info("Creates a new risk analysis session")
	public String createSession(
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@QueryParam("rc") @Info("The risk configuration to be used")	String rc,
			@QueryParam("target") @Info("The target entity; if working with entity hierarchy, it is typically the root of the hierarchy")
																			String target, 
			@QueryParam("name") @Info("The name of the risk analysis session")
																			String name
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			// Create a new risk analysis session
			RiskAnalysisSession ras = db.createRAS();
			
			// setup layers
			ras.setLayers( db.layerNames() );
			
			// set target
			ras.setTarget( target );
			
			// setup entities
			gatherEntityTree( target, db, ras );
			
			{
				String uniqueName = name;
				int i = 0;
				while( db.existsRAS( uniqueName ) == true ) {
					i++;
					uniqueName = name + " (" + i + ")";
				}
				name = uniqueName;
			}
			
			// setup risk configuration
			ras.setRCName( rc );
			ras.setRCModels( db.getRCModels( rc ) );
			if( name != null )
				ras.setName( name );
			else ras.setName( ras.getId() );
			
			// store models content
			Map<String,ArrayList<String>> map = db.getRCModels( rc );
			for( String layer : db.layerNames() ) {
				ArrayList<String> models = map.get( layer );
				if( models == null ) models = new ArrayList<String>();
				for( String model : models ) {
					ras.storeModelBlob( model, layer, db.getModelBlob( model ) );
				}
			}
			
			cacheRDRData( ras, db );
			
			db.saveRAS( ras );
			
			return gson.toJson( 
					new JRASInfo( ras.getId(), ras.getName() ) );
			
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/session/{sid}/edit-target/{target}")
	@Info("Edits the entity name of a risk session")
	public void editSessionTarget( 
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("sid") @Info("The risk session ID")					String sid ,
			@PathParam("target") @Info("The new entity name")				String name
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			RiskAnalysisSession ras = db.openRAS( sid );
			
			ras.setTarget(name);
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	/* This method goes through the hierarchy of entities in a given risk analysis session,
	 * reads the required data from the rdr, and stores the data in the risk analysis session
	 */
	@GET @Path("/{domain}/session/{sid}/update-data")
	@Info(
			"This method goes through the hierarchy of entities in a given risk analysis session," +
			"reads the required data from the rdr, and stores the data in the risk analysis session")
	public void updateSessionData( 
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("sid") @Info("The risk session ID")					String sid 
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			RiskAnalysisSession ras = db.openRAS( sid );
			
			cacheRDRData( ras, db );
			
			ras.setOption( "rdr-read-time", "" + new Date().getTime() );
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	void cacheRDRData( RiskAnalysisSession ras, RiscossDB db  ) {
		
		new DownwardEntitySearch( db ).analyseEntity( ras.getTarget(), new TraverseCallback<AnalysisDataSource>( new AnalysisDataSource( ras, db ) ) {
			@Override
			public void afterEntityAnalyzed( String entity ) {
				RiskAnalysisSession ras = getValue().ras;
				RiscossDB db = getValue().db;
				String layer = db.layerOf( entity );
				RiskAnalysisEngine rae = ReasoningLibrary.get().createRiskAnalysisEngine();
				for( String model : ras.getModels( layer ) ) {
					String blob = db.getModelBlob( model );
					if( blob != null ) {
						rae.loadModel( blob );
					}
				}
				for( Chunk c : rae.queryModel( ModelSlice.INPUT_DATA ) ) {
					String str = db.readRiskData( entity, c.getId() );
					if( str != null ) {
						ras.saveInput( entity, c.getId(), "rdr", str );
					}
				}
			}
		} );
		
	}
	
	@GET @Path("/{domain}/session/{sid}/missing-data")
	@Info(
			"Given a risk session, this method returns the list of ID of the indicators, " + 
			"which still do not have a value")
	public String getSessionMissingData(
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("sid") @Info("The risk session ID")					String sid
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			db = DBConnector.openDB( domain, token );
			
			// Sia i dati che mancano, sia quelli marcati come "user"
			RiskAnalysisSession ras = db.openRAS( sid );
			
			String target = ras.getTarget();
			
			RiskAnalysisProcess rap = new RiskAnalysisProcess( ras );
			JMissingData md = rap.gatherMissingData( target );
			
			return gson.toJson( md );
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/session/{sid}/missing-data")
	@Info("Set the value of the indicators that still do not have a value")
	public void setSessionMissingData(
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("sid") @Info("The risk session ID")					String sid, 
			String values
	) throws Exception {
		JsonObject json = (JsonObject) new JsonParser().parse(values);
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			JValueMap valueMap = gson.fromJson(json, JValueMap.class);
			RiskAnalysisSession ras = db.openRAS(sid);
			for (String entity : valueMap.map.keySet()) {
				for (JRiskData jrd : valueMap.map.get(entity)) {
					RiskData rd = new RiskData(jrd.id, entity, new Date(), RiskDataType.NUMBER, jrd.value);
					ras.saveInput(entity, rd.getId(), "user", gson.toJson(rd));
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
	
	@GET @Path("/{domain}/session/{sid}/data")
	public String getSessionData(
			@HeaderParam("token") @Info("The authentication token")				String token, 
			@PathParam("domain") @Info("The work domain")						String domain,
			@PathParam("sid") @Info("The risk session ID")						String sid,
			@QueryParam("e") @Info("The list of entities whose data have to be retrieved")
																				String entities
			) throws Exception {
		RiscossDB db = null;
		try {
			JsonArray jentities = (JsonArray)new JsonParser().parse( entities );
			List<String> list = new ArrayList<>();
			for (int i = 0; i < jentities.size(); ++i) {
				list.add(jentities.get(i).getAsString());
			}
			db = DBConnector.openDB( domain, token );
			JWhatIfData data = new JWhatIfData();
			RiskAnalysisSession ras = db.openRAS( sid );
			for( String entity : list ) {
				String layer = ras.getLayer( entity );
				JWhatIfItem item = new JWhatIfItem();
				item.models = ras.getModels( layer );
				{
					RiskAnalysisEngine rae = ReasoningLibrary.get().createRiskAnalysisEngine();
					for( String model : item.models ) {
						String blob = ras.getStoredModelBlob( model );
						rae.loadModel( blob );
						for( Chunk chunk : rae.queryModel( ModelSlice.INPUT_DATA ) ) {
							item.values.put( chunk.getId(), ras.getInput( entity, chunk.getId() ) );
						}
					}
				}
				data.items.put( entity, item );
			}
			return gson.toJson( data );
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	
	@GET @Path("/{domain}/session/{sid}/summary")
	@Info(	"Returns some basic information about a risk session: " + 
			"ID, target entity, risk configuration, name, timestamp" )	
	public String getSessionSummary(
			@PathParam("domain") @Info("The work domain")						String domain,
			@HeaderParam("token") @Info("The authentication token")				String token, 
			@PathParam("sid") @Info("The risk session ID")						String sid
			) throws Exception {
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			RiskAnalysisSession ras = db.openRAS( sid );
			JsonObject json = new JsonObject();
			json.addProperty( "id", ras.getId() );
			json.addProperty( "target", ras.getTarget() );
			json.addProperty( "rc", ras.getRCName() );
			json.addProperty( "name", ras.getName() );
			try {
				Date date = new Date( ras.getTimestamp() );
				SimpleDateFormat sdf = new SimpleDateFormat( "dd-MM-yyyy HH.mm.ss" );
				json.addProperty( "timestamp", sdf.format( date ) );
			}
			catch( Exception ex ) {}
			return json.toString();
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/session/{sid}/report")
	@Info("Get a report xml format string of the selected session")
	public String generateReport(
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token,
			@PathParam("sid") @Info("The session id")						String sid) throws Exception {
		
		RiscossDB db = null;
		
		try {
			db = DBConnector.openDB( domain, token );
			RiskAnalysisSession ras = db.openRAS( sid );

			//Inits XML parser
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
	        Document doc = docBuilder.newDocument();
	        
	        //Parse results data
	        Element rootElement = doc.createElement("riscoss");
	        doc.appendChild(rootElement);
	        Element risksession = doc.createElement("risksession");
	        risksession.setAttribute("label", ras.getName());
	        rootElement.appendChild(risksession);
	        
	        //Risk session main info
	        Element target = doc.createElement("target");
	        target.setTextContent(ras.getTarget());
	        risksession.appendChild(target);
	        
	        Element rc = doc.createElement("rc");
	        rc.setTextContent(ras.getRCName());
	        risksession.appendChild(rc);
	        
	        Element time = doc.createElement("timestamp");
	        Date date = new Date( ras.getTimestamp() );
			SimpleDateFormat sdf = new SimpleDateFormat( "dd-MM-yyyy HH.mm.ss" );
	        time.setTextContent(sdf.format(date));
	        risksession.appendChild(time);
	        
	        //Models data
	        List<String> modelsList = db.getModelsFromRiskCfg( ras.getRCName(), ras.getTarget() );
	        Element models = doc.createElement("models");
	        risksession.appendChild(models);
	        for (String modelName : modelsList) {
	        	Element model = doc.createElement("model");
	        	Element name = doc.createElement("name");
	        	name.setTextContent(modelName);
	        	model.appendChild(name);
	        	models.appendChild(model);
	        }
	        //Results data
	        Element results = doc.createElement("results");
	        JSONObject jsonRes = new JSONObject(ras.readResults());
	        if (!jsonRes.has("hresults")) {
	        	results.setAttribute("type", "sequential");
	        	jsonRes.put("entity", ras.getTarget());
	        	getSequentialResults(jsonRes, results, doc);
	        }
	        else {
	        	results.setAttribute("type", "hierarchycal");
	        	getHierarchycalResults(jsonRes.getJSONObject("hresults"), results, doc);
	        }
	        risksession.appendChild(results);
	        
	        //Input data
	        Element inputs = doc.createElement("inputs");
	        getInputs(jsonRes.getJSONObject("input"), inputs, doc);
	        risksession.appendChild(inputs);
	        
	        //Argumentation
	        Element argumentation = doc.createElement("argumentation");
	        getArguments(jsonRes.getJSONObject("argumentation").getJSONObject("arguments"), argumentation, doc);
	        risksession.appendChild(argumentation);
	        
	        //Get XML string
	        StringWriter sw = new StringWriter();
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	        transformer.transform(new DOMSource(doc), new StreamResult(sw));
	        
	        JsonObject json = new JsonObject();
			json.addProperty( "xml", sw.toString() );
	        
			return json.toString();
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	private void getArguments(JSONObject jsonObject, Element argumentation, Document doc) {
		Iterator<?> keys = jsonObject.keys();
		
		while (keys.hasNext()) {
			String key = (String) keys.next();
			try {
				Element argument = doc.createElement("argument");
				argument.setAttribute("id", key);
				Element summary = doc.createElement("summary");
				summary.setTextContent(jsonObject.getJSONObject(key).getString("summary"));
				Element truth = doc.createElement("truth");
				truth.setTextContent(jsonObject.getJSONObject(key).getString("truth"));
				
				Element subArgs = doc.createElement("subArgs");
				JSONArray r = jsonObject.getJSONObject(key).getJSONArray("subArgs");
				for (int i = 0; i < r.length(); ++i) {
					Element subArg = doc.createElement("argument");
					appendSubArgs(r.getJSONObject(i), subArg, doc);
					subArgs.appendChild(subArg);
				}
				
				argumentation.appendChild(argument);
				argument.appendChild(summary);
				argument.appendChild(truth);
				argument.appendChild(subArgs);
				
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	}

	private void appendSubArgs(JSONObject object, Element subArg, Document doc) {
		try {
			Element summary = doc.createElement("summary");
			summary.setTextContent(object.getString("summary"));
			Element truth = doc.createElement("truth");
			truth.setTextContent(object.getString("truth"));
			
			Element subArgs = doc.createElement("subArgs");
			JSONArray r = object.getJSONArray("subArgs");
			for (int i = 0; i < r.length(); ++i) {
				Element sub = doc.createElement("argument");
				appendSubArgs(r.getJSONObject(i), sub, doc);
				subArgs.appendChild(sub);
			}
			subArg.appendChild(summary);
			subArg.appendChild(truth);
			subArg.appendChild(subArgs);
			
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	private void getInputs(JSONObject jsonRes, Element inputs, Document doc) {
		Element entity = doc.createElement("entity");
		Element data = doc.createElement("data");
		Element children = doc.createElement("children");
		try {
			entity.setTextContent(jsonRes.getString("entity"));
			JSONArray r = jsonRes.getJSONArray("data");
			for (int i = 0; i < r.length(); ++i) {
				Element event = doc.createElement("input");
				event.setAttribute("id", r.getJSONObject(i).getString("id"));
				Element label = doc.createElement("label");
				label.setTextContent(r.getJSONObject(i).getString("label"));
				Element type = doc.createElement("type");
				type.setTextContent(r.getJSONObject(i).getString("type"));
				Element description = doc.createElement("description");
				description.setTextContent(r.getJSONObject(i).getString("description"));
				Element exposure = doc.createElement("value");
				exposure.setTextContent(String.valueOf(r.getJSONObject(i).getString("value")));
				
				event.appendChild(label);
				event.appendChild(type);
				event.appendChild(description);
				event.appendChild(exposure);
				
				data.appendChild(event);
			}
			inputs.appendChild(entity);
			inputs.appendChild(data);
			
			JSONArray childArray = jsonRes.getJSONArray("children");
			for (int i = 0; i < childArray.length(); ++i) {
				Element child = doc.createElement("child");
				getInputs(childArray.getJSONObject(i), child, doc);
				children.appendChild(child);
			}
			inputs.appendChild(children);
			
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void getHierarchycalResults(JSONObject jsonRes, Element results, Document doc) {
		Element entity = doc.createElement("entity");
		Element res = doc.createElement("res");
		Element children = doc.createElement("children");
		try {
			extractRes(jsonRes, results, doc, entity, res);
			
			JSONArray childArray = jsonRes.getJSONArray("children");
			for (int i = 0; i < childArray.length(); ++i) {
				Element child = doc.createElement("child");
				getHierarchycalResults(childArray.getJSONObject(i), child, doc);
				if (child.hasChildNodes()) children.appendChild(child);
			}
			results.appendChild(children);
			
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	private void getSequentialResults(JSONObject jsonRes, Element results, Document doc) {
		Element entity = doc.createElement("entity");
		Element res = doc.createElement("res");
		try {
			extractRes(jsonRes, results, doc, entity, res);
			
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void extractRes(JSONObject jsonRes, Element results, Document doc,
			Element entity, Element res) throws JSONException {
		entity.setTextContent(jsonRes.getString("entity"));
		JSONArray r = jsonRes.getJSONArray("results");
		for (int i = 0; i < r.length(); ++i) {
			Element event = doc.createElement("event");
			event.setAttribute("id", r.getJSONObject(i).getString("id"));
			Element type = doc.createElement("type");
			type.setTextContent(r.getJSONObject(i).getString("type"));
			Element datatype = doc.createElement("datatype");
			datatype.setTextContent(r.getJSONObject(i).getString("datatype"));
			event.appendChild(type);
			switch (r.getJSONObject(i).getString("datatype")) {
				case "evidence":
					Element label = doc.createElement("label");
					label.setTextContent(r.getJSONObject(i).getString("label"));
					Element description = doc.createElement("description");
					description.setTextContent(r.getJSONObject(i).getString("description"));
					Element exposure = doc.createElement("exposure");
					exposure.setTextContent(String.valueOf(r.getJSONObject(i).getJSONObject("e").getDouble("e")));
					event.appendChild(label);
					event.appendChild(description);
					event.appendChild(exposure);
					res.appendChild(event);
					break;
				case "distribution":
					Element values = doc.createElement("values");
					JSONArray s = r.getJSONObject(i).getJSONArray("value");
					for (int j = 0; j < s.length(); ++j) {
						Element value = doc.createElement("value");
						value.setTextContent(s.getString(j));
						values.appendChild(value);
					}
					Element rank = doc.createElement("rank");
					rank.setTextContent(r.getJSONObject(i).getString("rank"));
					event.appendChild(rank);
					event.appendChild(values);
					res.appendChild(event);
					break;
				default:
					break;
			}
		}
		results.appendChild(entity);
		results.appendChild(res);
	}
	
	@GET @Path("/{domain}/session/{sid}/results")
	@Produces("application/json")
	@Info( "Returns the results of a previously executed risk analysis" )
	public String getRAD( 
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("sid") @Info("The risk session ID")					String sid 
			) throws Exception {
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			RiskAnalysisSession ras = db.openRAS( sid );
			return ras.readResults();
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@DELETE @Path("/{domain}/session/{sid}/delete")
	@Info( "Removes a risk analysis session from the DB" )
	public void deleteRiskAnalysis( 
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("sid") @Info("The risk session ID")					String sid 
			) throws Exception {
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			db.destroyRAS( sid );
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/session/{sid}/newrun")
	@Info(	"Executes a risk analysis. This method only applies to a risk analysis session. " + 
			"It loads the models stored in the analysis session, " + 
			"takes the indicator values stored in the analysis session " + 
			"and applies them to the stored entity trees" )
	public String runRiskAnalysis(
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token,
			@PathParam("sid") @Info("The risk analysis sesison ID")			String sid,
			@DefaultValue("RunThrough") @QueryParam("opt") @Info("A flag that tells what to do in case some data are missing. Can be left unset.")
																			String strOpt /* See AnalysisOption.RunThrough */
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			// Create a new risk analysis session
			RiskAnalysisSession ras = db.openRAS( sid );
			
			RiskAnalysisProcess proc = new RiskAnalysisProcess();
			
			// Apply analysis algorithm
			proc.start( ras );
			
			// Save session (in case of in-memory sessions)
			db.saveRAS( ras );
			
			JsonObject res = getAnalysisResults( ras );
			
			String ret = res.toString();
			
			ras.saveResults( ret );
			
			ras.setTimestamp( new Date().getTime() );
			
			return ret;
			
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	private JsonObject getAnalysisResults( RiskAnalysisSession ras, String entityName ) {
		
		JsonObject jo = new JsonObject();
		
		JsonArray ret = new JsonArray();
		
		String layerName = ras.getLayer( entityName );
		
		for( String indicatorId : ras.getResults( layerName, entityName ) ) {
			
			JsonObject o = new JsonObject();
			o.addProperty( "id", indicatorId );
			DataType dt = DataType.valueOf( ras.getResult( layerName, entityName, indicatorId, "datatype", DataType.REAL.name() ) );
			o.addProperty( "datatype", dt.name().toLowerCase() );
			o.addProperty( "type", ras.getResult( layerName, entityName, indicatorId, "type", "" ) );
			o.addProperty( "rank", ras.getResult( layerName, entityName, indicatorId, "rank", "0" ) );
			switch( dt ) {
			case EVIDENCE: {
				JsonObject je = new JsonObject();
				je.addProperty( "e", 
						Double.parseDouble( ras.getResult( layerName, entityName, indicatorId, "e", "0" ) ) );
				o.add( "e", je );
				o.addProperty( "p", ras.getResult( layerName, entityName, indicatorId, "p", "0" ) );
				o.addProperty( "m", ras.getResult( layerName, entityName, indicatorId, "m", "0" ) );
				o.addProperty( "description", ras.getResult( layerName, entityName, indicatorId, "description", "" ) );
				o.addProperty( "label", ras.getResult( layerName, entityName, indicatorId, "label", indicatorId ) );
			}
			break;
			case DISTRIBUTION: {
				String value = ras.getResult( layerName, entityName, indicatorId, "value", "" );
				Distribution d = Distribution.unpack( value );
				JsonArray values = new JsonArray();
				for( int i = 0; i <  d.getValues().size(); i++ ) {
					values.add( new JsonPrimitive( "" + d.getValues().get( i ) ) );
				}
				o.add( "value", values );
			}
			break;
			case INTEGER:
				o.addProperty( "value", ras.getResult( layerName, entityName, indicatorId, "value", "0" ) );
				break;
			case NaN:
				break;
			case REAL:
				o.addProperty( "value", ras.getResult( layerName, entityName, indicatorId, "value", "0" ) );
				break;
			case STRING:
				o.addProperty( "value", ras.getResult( layerName, entityName, indicatorId, "value", "" ) );
				break;
			default:
				break;
			}
			ret.add( o );
		}
		
		jo.addProperty( "entity", entityName );
		jo.add( "results", ret );
		
		JsonArray children = new JsonArray();
		
		for( String child : ras.getChildren( entityName ) ) {
			children.add( getAnalysisResults( ras, child ) );
		}
		
		jo.add( "children", children );
		
		return jo;
		
	}
	
	private JsonObject loadResult( RiskAnalysisSession ras, String indicatorId, String entityName, String layerName ) {
		
		JsonObject o = new JsonObject();
		o.addProperty( "id", indicatorId );
		DataType dt = DataType.valueOf( ras.getResult( layerName, entityName, indicatorId, "datatype", DataType.REAL.name() ) );
		o.addProperty( "datatype", dt.name().toLowerCase() );
		o.addProperty( "type", ras.getResult( layerName, entityName, indicatorId, "type", "" ) );
		o.addProperty( "rank", ras.getResult( layerName, entityName, indicatorId, "rank", "0" ) );
		switch( dt ) {
		case EVIDENCE: {
			JsonObject je = new JsonObject();
			je.addProperty( "e", 
					Double.parseDouble( ras.getResult( layerName, entityName, indicatorId, "e", "0" ) ) );
			o.add( "e", je );
			o.addProperty( "p", ras.getResult( layerName, entityName, indicatorId, "p", "0" ) );
			o.addProperty( "m", ras.getResult( layerName, entityName, indicatorId, "m", "0" ) );
			o.addProperty( "description", ras.getResult( layerName, entityName, indicatorId, "description", "" ) );
			o.addProperty( "label", ras.getResult( layerName, entityName, indicatorId, "label", indicatorId ) );
		}
		break;
		case DISTRIBUTION: {
			String value = ras.getResult( layerName, entityName, indicatorId, "value", "" );
			Distribution d = Distribution.unpack( value );
			JsonArray values = new JsonArray();
			for( int i = 0; i <  d.getValues().size(); i++ ) {
				values.add( new JsonPrimitive( "" + d.getValues().get( i ) ) );
			}
			o.add( "value", values );
		}
		break;
		case INTEGER:
			o.addProperty( "value", ras.getResult( layerName, entityName, indicatorId, "value", "0" ) );
			break;
		case NaN:
			break;
		case REAL:
			o.addProperty( "value", ras.getResult( layerName, entityName, indicatorId, "value", "0" ) );
			break;
		case STRING:
			o.addProperty( "value", ras.getResult( layerName, entityName, indicatorId, "value", "" ) );
			break;
		default:
			break;
		}
		return o;
	}
	
	private JsonObject getAnalysisResults( RiskAnalysisSession ras ) {
		
		JsonObject json = new JsonObject();
		
		JsonObject res = getAnalysisResults( ras, ras.getTarget() );
		
		json.add( "hresults", res );
		
		JsonArray clusters = new JsonArray();
		Set<String> done = new HashSet<String>();
		
		{
			JsonArray ret = new JsonArray();
			
			for( int l = 0; l < ras.getLayerCount(); l++ ) {
				
				String layerName = ras.getLayer( l );
				
				for( String entityName : ras.getEntities( layerName ) ) {
					
					if( done.contains( entityName ) ) continue;
					
					done.add( entityName );
					
					JsonObject jcluster = new JsonObject();
					
					jcluster.addProperty( "entity", entityName );
					
					for( String indicatorId : ras.getResults( layerName, entityName ) ) {
						
						JsonObject o = loadResult( ras, indicatorId, entityName, layerName );
						
						jcluster.add( "results", loadResult( ras, indicatorId, entityName, layerName ) );
						
						ret.add( o );
					}
					
					clusters.add( jcluster );
					
				}
			}
			
			json.add( "results", ret );
//			json.add( "cr", clusters );
		}
		
		
		json.addProperty( "result", EAnalysisResult.Done.name() );
		
		JsonObject info = new JsonObject();
		info.addProperty( "entity", ras.getTarget() );
		json.add( "info", info );
		
		JMissingData inputs = new RiskAnalysisProcess( ras ).fillMissingDataStructureNew( 
				ras.getTarget(), false, true, new String[] { "user" } );
		
		json.add( "input", gson.toJsonTree( inputs ) );
		
		String jsonArg = ras.getEntityAttribute( ras.getTarget(), "argumentation", null );
		if( jsonArg != null ) {
			Argumentation arg = gson.fromJson( jsonArg, Argumentation.class );
			JArgumentation jarg = transform( arg );
			json.add( "argumentation", gson.toJsonTree( jarg ) );
		}
		
		return json;
		
	}
	
	private JArgumentation transform( Argumentation a ) {
		
		JArgumentation argumentation = new JArgumentation();
		
		for( Argument arg : a.getArgument().subArguments() ) {
			JArgument jarg = new JArgument();
			jarg.summary = arg.getSummary();
			jarg.truth = arg.getTruth();
			argumentation.arguments.put( arg.getId(), jarg );
			fill( jarg, arg );
		}
		
//		fill( argumentation.argument, a.getArgument() );
		
		return argumentation;
	}
	
	private void fill( JArgument jarg, Argument arg ) {
		jarg.summary = arg.getSummary();
		jarg.truth = arg.getTruth();
		for( Argument subArg : arg.subArguments() ) {
			JArgument jsub = new JArgument();
			fill( jsub, subArg );
			jarg.subArgs.add( jsub );
		}
	}
	
	void gatherEntityTree( String entity, RiscossDB db, RiskAnalysisSession ras ) {
		
		String layer = db.layerOf( entity );
		
		ras.addEntity( entity, layer );
		
		for( String child : db.getChildren( entity ) ) {
			gatherEntityTree( child, db, ras );
			ras.setParent( child, entity );
		}
		
	}
	
	@POST @Path("/{domain}/new")
	@Info("Executes a risk a simple risk analysis using real data but outside a risk session and with not hierarchy support")
	public String runAnalysisWithRealDataOld( 
			@DefaultValue("Playground") @PathParam("domain") @Info("The selected domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")											String token, 
			@QueryParam("rc") @Info("The risk configuration")												String rc,
			@QueryParam("target") @Info("The target entity")												String target,
			@QueryParam("verbosity") @Info("Extra parameters")												String flags,
			@DefaultValue("RunThrough") @QueryParam("opt") @Info("The policy to be applied in case of missing data")
																											String strOpt /* See AnalysisOption.RunThrough */,
			@HeaderParam("customData") @Info("An assignment to input indicators")							String customData
			) throws Exception {
		
		EAnalysisOption opt = EAnalysisOption.valueOf( strOpt );
		if( opt == null ) opt = EAnalysisOption.RunThrough;
		
		JsonObject custom = (JsonObject)new JsonParser().parse( customData );
		if( custom == null ) custom = new JsonObject();
		
		Properties options = new Properties();
		
		int verbosity = 0;
		
		if( flags != null ) {
			if( "full".equals( flags ) )
				options.put( "verbosity", "full" );
		}
		
		if( "full".equals( options.getProperty( "verbosity", "" ) ) ) {
			verbosity = Integer.MAX_VALUE;
		}
		
		RiskAnalysisEngine rae = ReasoningLibrary.get().createRiskAnalysisEngine();
		
		RiscossDB db = DBConnector.openDB( domain, token );
		
		try {
			
			for( String rc_name : db.getModelsFromRiskCfg( rc, target ) ) {
				String blob = db.getModelBlob( rc_name );
				rae.loadModel( blob );
			}
			
			JsonArray jinputs = new JsonArray();
			
			List<MissingDataItem> missingFields = new ArrayList<>();
			
			for( Chunk c : rae.queryModel( ModelSlice.INPUT_DATA ) ) {
				Field f = rae.getField( c, FieldType.INPUT_VALUE );
				String str = db.readRiskData( target, c.getId() );
				if( str == null ) {
					str = getCustomData( custom, c.getId() );
				}
				if( str == null ) {
					MissingDataItem item = new MissingDataItem();
					item.id = c.getId();
					Field field = rae.getField( c, FieldType.DESCRIPTION );
					item.description = (field != null ? (String)field.getValue() : null);
					field = rae.getField( c, FieldType.QUESTION );
					item.question = (field != null ? (String)field.getValue() : null);
					field = rae.getField( c, FieldType.LABEL );
					item.label = (field != null ? (String)field.getValue() : null);
					item.type = f.getDataType().name();
					switch( f.getDataType() ) {
					case DISTRIBUTION:
						item.value = ((Distribution)f.getValue()).pack();
						break;
					case EVIDENCE:
						item.value = ((Evidence)f.getValue()).pack();
						break;
					case INTEGER:
						item.value = "" + ((int)f.getValue());
						break;
					case NaN:
						item.value = "";
						break;
					case REAL:
						item.value = "" + ((double)f.getValue());
						break;
					case STRING:
						item.value = f.getValue().toString();
						break;
					default:
						break;
						
					}
					missingFields.add( item );
					continue;
				}
				JsonObject json = (JsonObject)new JsonParser().parse( str );
				if( json.get( "value" ) == null ) continue;
				String value = json.get( "value" ).getAsString();
				
				// TODO what to do if a wrong formatted string arrives? e.g., the user entered 'abc' for a 'REAL' field
				try {
					switch( f.getDataType() ) {
					case REAL:
						f.setValue( Double.parseDouble( value ) );
						break;
					case INTEGER:
						f.setValue( (int)Double.parseDouble( value ) );
						break;
					case NaN:
						break;
					case STRING:
						f.setValue( value );
						break;
					case DISTRIBUTION:
						f.setValue( Distribution.unpack(value) );
						break;
					case EVIDENCE:
						f.setValue( Evidence.unpack( value ) );
						break;
					default:
						break;
					}
				}
				catch( Exception ex ) {}
				rae.setField( c, FieldType.INPUT_VALUE, f );
				if( verbosity > 0 ) {
					jinputs.add( json );
				}
			}
			
			if( missingFields.size() > 0 ) {
				if( opt == EAnalysisOption.RequestMissingData ) {
					JsonObject ret = new JsonObject();
					ret.addProperty( "result", EAnalysisResult.DataMissing.name() );
					JsonObject md = new JsonObject();
					ret.add( "missingData", md );
					JsonArray array = new JsonArray();
					for( MissingDataItem missingItem : missingFields ) {
						JsonObject item = new JsonObject();
						item.addProperty( "id", missingItem.id ); // mandatory
						item.addProperty( "type", missingItem.type );  // mandatory
						item.addProperty( "label", (missingItem.label != null ? missingItem.label : missingItem.id ) );
						item.addProperty( "description", (missingItem.description != null ? missingItem.description : "(no description available)" ) );
						String question = missingItem.question;
						if( question == null ) question = "";
						if( "".equals( question ) ) question = "Value of '" + missingItem.id + "'?";
						item.addProperty( "question", question );
						item.addProperty( "value", missingItem.value );
						array.add( item );
					}
					md.add( "list", array );
					return ret.toString();
				}
			}
			
			try {
				rae.runAnalysis( new String[] {} );
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
			
			JsonArray ret = encodeResults( rae, options );
			
			JsonObject o = new JsonObject();
			
			o.add( "results", ret );
			
			if( verbosity > 0 ) {
				JsonObject info = new JsonObject();
				info.addProperty( "entity", target );
				o.add( "info", info );
				o.add( "inputs", jinputs );
			}
			
			o.addProperty( "result", EAnalysisResult.Done.name() );
			
			String jsonString = o.toString();
			
			db.storeRASResult( target, jsonString );
			
			return jsonString;
		}
		finally {
			DBConnector.closeDB( db );
		}
		
	}
	
	private String getCustomData( JsonObject o, String id) {
		if( o == null ) return null;
		o = o.getAsJsonObject();
		if( o == null ) return null;
		JsonElement e = o.get( id );
		if( e == null ) return null;
		if( e.getAsJsonObject() == null ) return null;
		return e.toString();
	}
	
	@POST @Path("/{domain}/whatif")
	@Consumes(MediaType.APPLICATION_JSON)
	@Info("Executes a risk analysis simulation outside a session")
	public String runAnalysisWithCustomData( 
			@PathParam("domain") @Info("The selected domain")							String domain,
			@HeaderParam("token") @Info("The authentication token")						String token, 
			@QueryParam("models") @Info("A list of models to be used")					String modelsString,
			@HeaderParam("values") @Info("An assignment to the input indicators")		String valuesString
			) throws Exception {
		
		JsonObject jvalues = (JsonObject)new JsonParser().parse( valuesString );
		JsonArray jmodels = (JsonArray)new JsonParser().parse( modelsString );
		
		RiskAnalysisEngine rae = ReasoningLibrary.get().createRiskAnalysisEngine();
		
		RiscossDB db = DBConnector.openDB( domain, token );
		
		try {
			for( int i = 0; i < jmodels.size(); i++ ) {
				String modelName = jmodels.get( i ).getAsString();
				String blob = db.getModelBlob( modelName );
				rae.loadModel( blob );
			}
			
			for( Chunk c : rae.queryModel( ModelSlice.INPUT_DATA ) ) {
				JsonObject o = jvalues.getAsJsonObject( c.getId() );
				if( o == null ) continue;
				String value = o.get( "value" ).getAsString();
				Field f = rae.getField( c, FieldType.INPUT_VALUE );
				switch( f.getDataType() ) {
				case DISTRIBUTION: {
					Distribution d = Distribution.unpack( value );
					f.setValue( d );
				}
					break;
				case EVIDENCE: {
					Evidence e = Evidence.unpack( value );
					f.setValue( e );
				}
					break;
				case INTEGER:
					f.setValue( (int)Double.parseDouble( value ) );
					break;
				case NaN:
					break;
				case REAL:
					f.setValue( Double.parseDouble( value ) );
					break;
				case STRING:
					f.setValue( value );
					break;
				default:
					break;
				}
				rae.setField( c, FieldType.INPUT_VALUE, f );
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
		
		rae.runAnalysis( new String[] {} );
		
		JsonArray ret = encodeResults( rae );
		
		JsonObject o = new JsonObject();
		
		o.add( "results", ret );
		
//		System.out.println( o.toString() );
		
		return o.toString();
		
	}
	
	JsonArray encodeResults( RiskAnalysisEngine rae ) {
		return encodeResults( rae,  new Properties() );
	}
	
	JsonArray encodeResults( RiskAnalysisEngine rae, Properties options ) {
		JsonArray ret = new JsonArray();
		
		for( Chunk c : rae.queryModel( ModelSlice.OUTPUT_DATA ) ) {
			Field f = rae.getField( c, FieldType.OUTPUT_VALUE );
			JsonObject o = new JsonObject();
			o.addProperty( "id", c.getId() );
			o.addProperty( "datatype", f.getDataType().name().toLowerCase() );
			switch( f.getDataType() ) {
			case EVIDENCE: {
				Evidence e = f.getValue();
				o.addProperty( "p", "" + e.getPositive() );
				o.addProperty( "m", "" + e.getNegative() );
				if( "full".equals( options.getProperty( "verbosity", "" ) ) ) {
					JsonObject je = new JsonObject();
					je.addProperty( "p", e.getPositive() );
					je.addProperty( "m", e.getNegative() );
					je.addProperty( "e", e.getDirection() );
					je.addProperty( "c", e.getConflict() );
					je.addProperty( "s", e.getStrength() );
					o.add( "e", je );
					o.addProperty( "description", "" + rae.getField( c, FieldType.DESCRIPTION ).getValue() );
					o.addProperty( "label", "" + rae.getField( c, FieldType.LABEL ).getValue() );
				}
			}
			break;
			case DISTRIBUTION: {
				Distribution d = f.getValue();
				JsonArray values = new JsonArray();
				for( int i = 0; i <  d.getValues().size(); i++ ) {
					values.add( new JsonPrimitive( "" + d.getValues().get( i ) ) );
				}
				o.add( "value", values );
			}
			break;
			case INTEGER:
				o.addProperty( "value", f.getValue().toString() );
				break;
			case NaN:
				break;
			case REAL:
				o.addProperty( "value", f.getValue().toString() );
				break;
			case STRING:
				o.addProperty( "value", f.getValue().toString() );
				break;
			default:
				break;
			}
			ret.add( o );
		}
		return ret;
	}
	
	@GET @Path("/{domain}/session/{sid}/mt/list")
	@Info("Returns a list of the currently applied mitigation activities")
	public String getAppliedMitigationTechniques(
			@HeaderParam("token") @Info("The authentication token")							String token,
			@PathParam("domain") @Info("The selected domain")								String domain,
			@PathParam("sid") @Info("The risk session ID")									String sid
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			db = DBConnector.openDB( domain, token );
			
			RiskAnalysisSession ras = db.openRAS( sid );
			
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
		
		return "";
	}
	
	@GET @Path("/{domain}/session/{sid}/mt/{mt}/input")
	@Info("Returns the input, specified by the user, of a previously applied mitigation activity")
	public String getMitigationTechniqueParameters(
			@HeaderParam("token") @Info("The authentication token")							String token,
			@PathParam("domain") @Info("The selected domain")								String domain,
			@PathParam("sid") @Info("The risk session ID")									String sid,
			@PathParam("mt") @Info("The name of the mitigation technique to be applied")	String mtName
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			RiskAnalysisSession ras = db.openRAS( sid );
			
			RiskScenario scenario = ras.getScenario( mtName );
			
			String ret = scenario.get( "input", "" );
			
			return ret;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/session/{sid}/mt/{mt}/output")
	@Info("Returns the output of a previously applied mitigation activity")
	public String getMitigationTechniqueResults(
			@HeaderParam("token") @Info("The authentication token")							String token,
			@PathParam("domain") @Info("The selected domain")								String domain,
			@PathParam("sid") @Info("The risk session ID")									String sid,
			@PathParam("mt") @Info("The name of the mitigation technique to be applied")	String mtName
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			RiskAnalysisSession ras = db.openRAS( sid );
			
			RiskScenario scenario = ras.getScenario( mtName );
			
			String ret = scenario.get( "output", "" );
			
			return ret;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/session/{sid}/mt/{mt}/params")
	@Info("Returns the parameters used to apply a given mitigation technique")
	public String getMitigationActivityParameters(
			@HeaderParam("token") @Info("The authentication token")							String token,
			@PathParam("domain") @Info("The selected domain")								String domain,
			@PathParam("sid") @Info("The risk session ID")									String sid,
			@PathParam("mt") @Info("The name of the mitigation technique to be retrieved")	String mtName
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			RiskAnalysisSession ras = db.openRAS( sid );
			
			RiskScenario scenario = ras.getScenario( mtName );
			
			String ret = scenario.get( "input", "" );
			
			return ret;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/session/{sid}/scenarios/list")
	@Info("Returns a list of the scenarios that have been created in a given risk session")
	public String getScenarioList(
			@HeaderParam("token") @Info("The authentication token")							String token,
			@PathParam("domain") @Info("The selected domain")								String domain,
			@PathParam("sid") @Info("The risk session ID")									String sid
			) {
		
		RiscossDB db = null;
		
		try {
			
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
		
		return "";
	}
	
	
	
	@POST @Path("/{domain}/session/{sid}/mt/{mt}/apply")
	@Info("Applies a specified mitigation technique to current risk session")
	public String applyMitigationTechnique(
			@HeaderParam("token") @Info("The authentication token")							String token,
			@PathParam("domain") @Info("The selected domain")								String domain,
			@PathParam("sid") @Info("The risk session ID")									String sid,
			@PathParam("mt") @Info("The name of the mitigation technique to be applied")	String mtName,
			@Info("The configuration parameters for the selected mitigation technique")		String json
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			RiskAnalysisSession ras = db.openRAS( sid );
			
			RiskScenario scenario = ras.getScenario( mtName );
			
			MitigationActivity ma = RiskAnalysisManager.get().getMitigationTechniqueInstance( mtName );
			
			String output = ma.eval( json, scenario );
			
			scenario.set( "input", json );
			scenario.set( "output", output );
			
			ma.apply( output, scenario );
			
			RiskAnalysisProcess proc = new RiskAnalysisProcess();
			
			proc.start( scenario );
			
			JsonObject res = getAnalysisResults( scenario );
			
			String ret = res.toString();
			
			scenario.saveResults( ret );
			
			return ret;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/ahp")
	@Info("Executes a simulation using the AHP algorithm")
	public String runAHPAnalysis( 
			@PathParam("domain") @Info("The selected domain")			String domain,
			@Info("The AHP parameters")									String json
			) throws IOException {
		
		try {
			
			AHPAnalysis ahp = new AHPAnalysis();
			
			return ahp.eval( json, null );
			
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		
		return "";
	}
	
}