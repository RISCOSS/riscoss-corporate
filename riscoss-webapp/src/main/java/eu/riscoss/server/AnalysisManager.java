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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.python.core.PyList;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;
import eu.riscoss.db.RecordAbstraction;
import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.RiskAnalysisSession;
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
import eu.riscoss.shared.EAnalysisOption;
import eu.riscoss.shared.EAnalysisResult;
import eu.riscoss.shared.JAHPComparison;
import eu.riscoss.shared.JAHPInput;
import eu.riscoss.shared.JArgument;
import eu.riscoss.shared.JArgumentation;
import eu.riscoss.shared.JMissingData;
import eu.riscoss.shared.JRASInfo;
import eu.riscoss.shared.JRiskData;
import eu.riscoss.shared.JStringList;
import eu.riscoss.shared.JValueMap;

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
	public String listRAS(
			@DefaultValue("Playground") @PathParam("domain") String domain,
			@DefaultValue("") @HeaderParam("token") String token, @QueryParam("entity") String entity, @QueryParam("rc") String rc ) {
		
		RiscossDB db = DBConnector.openDB( domain, token );
		try {
			JsonObject json = new JsonObject();
			JsonArray array = new JsonArray();
			for( RecordAbstraction record : db.listRAS( entity,  rc ) ) {
				array.add( gson.toJsonTree( 
						new JRASInfo( record.getName(), record.getProperty( "name", record.getName() ) ) ) );
			}
			json.add( "list", array );
			return json.toString();
		}
		finally {
			DBConnector.closeDB( db );
		}
		
	}
	
	@POST @Path("/{domain}/session/create")
	public String createSession(
			@DefaultValue("Playground") @PathParam("domain") String domain,
			@DefaultValue("") @HeaderParam("token") String token, @QueryParam("rc") String rc,
			@QueryParam("target") String target, @QueryParam("name") String name
			) {
		RiscossDB db = DBConnector.openDB( domain, token );
		try {
			
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
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	/* This method goes through the hierarchy of entities in a given risk analysis session,
	 * reads the required data from the rdr, and stores the data in the risk analysis session
	 */
	@GET @Path("/{domain}/session/{sid}/update-data")
	public void updateSessionData( @DefaultValue("Playground") @PathParam("domain") String domain,
			@DefaultValue("") @HeaderParam("token") String token, @PathParam("sid") String sid ) {
		RiscossDB db = DBConnector.openDB( domain, token );
		try {
			
			RiskAnalysisSession ras = db.openRAS( sid );
			
			cacheRDRData( ras, db );
			
			ras.setOption( "rdr-read-time", "" + new Date().getTime() );
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
	public String getSessionMissingData(
			@DefaultValue("Playground") @PathParam("domain") String domain,
			@DefaultValue("") @HeaderParam("token") String token, @PathParam("sid") String sid
			) {
		RiscossDB db = DBConnector.openDB( domain, token );
		
		try {
			// Sia i dati che mancano, sia quelli marcati come "user"
			RiskAnalysisSession ras = db.openRAS( sid );
			
			String target = ras.getTarget();
			
			RiskAnalysisProcess rap = new RiskAnalysisProcess( ras );
			JMissingData md = rap.gatherMissingData( target );
			
			return gson.toJson( md );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/session/{sid}/missing-data")
	public void setSessionMissingData(@DefaultValue("Playground") @PathParam("domain") String domain,
			@DefaultValue("") @HeaderParam("token") String token, @PathParam("sid") String sid, String values // @HeaderParam("values")
	) {
		JsonObject json = (JsonObject) new JsonParser().parse(values);
		RiscossDB db = DBConnector.openDB(domain, token);
		try {
			JValueMap valueMap = gson.fromJson(json, JValueMap.class);
			RiskAnalysisSession ras = db.openRAS(sid);
			for (String entity : valueMap.map.keySet()) {
				for (JRiskData jrd : valueMap.map.get(entity)) {
					RiskData rd = new RiskData(jrd.id, entity, new Date(), RiskDataType.NUMBER, jrd.value);
					ras.saveInput(entity, rd.getId(), "user", gson.toJson(rd));
				}
			}
		} finally {
			DBConnector.closeDB(db);
		}
	}

	@GET @Path("/{domain}/session/{sid}/summary")
	public String getSessionSummary(
			@DefaultValue("Playground") @PathParam("domain") String domain,
			@DefaultValue("") @HeaderParam("token") String token, @PathParam("sid") String sid
			) {
		RiscossDB db = DBConnector.openDB( domain, token );
		try {
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
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/session/{sid}/results")
	@Produces("application/json")
	public String getRAD( 
			@DefaultValue("Playground") @PathParam("domain") String domain,
			@DefaultValue("") @HeaderParam("token") String token, @PathParam("sid") String sid ) {
		RiscossDB db = DBConnector.openDB( domain, token );
		try {
			RiskAnalysisSession ras = db.openRAS( sid );
			return ras.readResults();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@DELETE @Path("/{domain}/session/{sid}/delete")
	public void deleteRiskAnalysis( @DefaultValue("Playground") @PathParam("domain") String domain,
			@DefaultValue("") @HeaderParam("token") String token, @PathParam("sid") String sid ) {
		RiscossDB db = DBConnector.openDB( domain, token );
		try {
			db.destroyRAS( sid );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/session/{sid}/newrun")
	public String runRiskAnalysis(
			@DefaultValue("Playground") @PathParam("domain") String domain,
			@DefaultValue("") @HeaderParam("token") String token, @PathParam("sid") String sid,
			@DefaultValue("RunThrough") @QueryParam("opt") String strOpt /* See AnalysisOption.RunThrough */
			) {
		
		RiscossDB db = DBConnector.openDB( domain, token );
		
		try {
			
//			TimeDiff.get().init();
//			TimeDiff.get().log( "Initializing analysis" );
			
			// Create a new risk analysis session
			RiskAnalysisSession ras = db.openRAS( sid );
			
//			TimeDiff.get().log( "Starting analysis process" );
			RiskAnalysisProcess proc = new RiskAnalysisProcess();
			
			// Apply analysis algorithm
			proc.start( ras );
			
			// Save session (in case of in-memory sessions)
			db.saveRAS( ras );
			
//			TimeDiff.get().log( "Encoding analysis results" );
			JsonObject res = getAnalysisResults( ras );
			
			String ret = res.toString();
			
//			TimeDiff.get().log( "Caching analysis results" );
			ras.saveResults( ret );
			
			ras.setTimestamp( new Date().getTime() );
			
//			TimeDiff.get().log( "Analysis done" );
			
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
	
	private JsonObject getAnalysisResults( RiskAnalysisSession ras ) {
		
		JsonObject json = new JsonObject();
		
		{
			JsonArray ret = new JsonArray();
			
			for( int l = 0; l < ras.getLayerCount(); l++ ) {
				
				String layerName = ras.getLayer( l );
				
				for( String entityName : ras.getEntities( layerName ) ) {
					
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
					
				}
			}
			
			json.add( "results", ret );
		}
		
		
		json.addProperty( "result", EAnalysisResult.Done.name() );
		
		JsonObject info = new JsonObject();
		info.addProperty( "entity", ras.getTarget() );
		json.add( "info", info );
		
		// TODO: read the whole set of input values
//		json.add( "inputs", ras.getInputs() );
		
		JMissingData inputs = new RiskAnalysisProcess( ras ).fillMissingDataStructureNew( ras.getTarget(), false, true, (String[])null );
		
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
	public String runAnalysisWithRealDataOld( 
			@DefaultValue("Playground") @PathParam("domain") String domain,
			@DefaultValue("") @HeaderParam("token") String token, 
			@QueryParam("rc") String rc,
			@QueryParam("target") String target,
			@QueryParam("verbosity") String flags,
			@DefaultValue("RunThrough") @QueryParam("opt") String strOpt /* See AnalysisOption.RunThrough */,
			@DefaultValue("") @HeaderParam("customData") String customData ) throws Exception {
		
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
	public String runAnalysisWithCustomData( 
			@DefaultValue("Playground") @PathParam("domain") String domain,
			@DefaultValue("") @HeaderParam("token") String token, 
			@QueryParam("models") String modelsString,
			@HeaderParam("values") String valuesString ) throws Exception {
		
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
	
	@POST @Path("/{domain}/ahp")
	public String runAHPAnalysis( 
			@DefaultValue("Playground") @PathParam("domain") String domain,
			@Context HttpServletRequest req ) throws IOException {
		
		String json = getBody(req );
		
		System.out.println( "Received json: " + json );
		
		System.out.println( "Decoding AHP input" );
		JAHPInput ahpInput = gson.fromJson( json, JAHPInput.class );
		
		System.out.println( "Running AHP" );
		
		try {
			
			Map<String,Integer> goal_map = mkIdMap( ahpInput.goals );
			
			String strList1 = mkList( ahpInput.goals, goal_map );
			String[] strList2 = new String[ahpInput.risks.size()];
			for( int i = 0; i < ahpInput.risks.size(); i++ ) {
				List<JAHPComparison> list = ahpInput.risks.get( i );
				Map<String,Integer> risk_map = mkIdMap( list );
				strList2[i] = mkList( list, risk_map );
			}
			
			System.out.println( "Preparing args" );
			PySystemState stm = new PySystemState();
			List<String> args = new ArrayList<String>();
			
			args.add( "jython" );		// first argument in python is the program name
			args.add( "" + ahpInput.getGoalCount() );
			args.add( "" + ahpInput.getRiskCount() );
			args.add( strList1 );
			for( int i = 0; i < strList2.length; i++ ) {
				args.add( strList2[i] );
			}
			
			stm.argv = new PyList( args );
			
			System.out.println( "Creating Jython object" );
			PythonInterpreter jython =
				    new PythonInterpreter( null, stm );
			
			System.out.println( "Seeting output stream" );
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			jython.setOut( out );
			
			System.out.println( "Executing" );
			jython.execfile( AnalysisManager.class.getResource( "res/ahpNoEig_command.py" ).getFile() );
			
			String output = out.toString();
			
			System.out.println( "Output:" );
			System.out.println( output );
			
			try {
				output = output.substring( 1, output.length() -2 );
				String[] parts = output.split( "[,]" );
				JStringList list = new JStringList();
				for( String p : parts ) {
					list.list.add( p.trim() );
				}
				return gson.toJson( list );
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
			finally {
				if( jython != null )
					jython.close();
			}
			
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		
		return "";
	}
	
	private Map<String,Integer> mkIdMap( List<JAHPComparison> list ) {
		Map<String,Integer> goal_map = new HashMap<String,Integer>();
		for( JAHPComparison c : list ) {
			if( !goal_map.containsKey( c.getId1() ) ) {
				goal_map.put( c.getId1(), goal_map.size() );
			}
			if( !goal_map.containsKey( c.getId2() ) ) {
				goal_map.put( c.getId2(), goal_map.size() );
			}
		}
		return goal_map;
	}
	
	private String mkList( List<JAHPComparison> list, Map<String, Integer> id_map  ) {
		final int[] numbers = new int[] { 9, 7, 5, 3, 1, 3, 5, 7, 9 };
		String ret = "";
		String sep = "";
		for( JAHPComparison c : list ) {
			if( c.value < 4 ) {
				ret += sep + "[" + id_map.get( c.getId1() ) + "," + id_map.get( c.getId2() ) + "," + numbers[c.value] + "]";
			}
			else {
				ret += sep + "[" + id_map.get( c.getId2() ) + "," + id_map.get( c.getId1() ) + "," + numbers[c.value] + "]";
			}
			sep = ",";
		}
		return "[" + ret + "]";
	}
	
	public static String getBody(HttpServletRequest request) throws IOException {

	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            char[] charBuffer = new char[128];
	            int bytesRead = -1;
	            while ((bytesRead = bufferedReader.read(charBuffer)) > 0) {
	                stringBuilder.append(charBuffer, 0, bytesRead);
	            }
	        } else {
	            stringBuilder.append("");
	        }
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }

	    body = stringBuilder.toString();
	    return body;
	}
}