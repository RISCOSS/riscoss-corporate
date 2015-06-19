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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.RiskAnalysisSession;
import eu.riscoss.db.TimeDiff;
import eu.riscoss.reasoner.Chunk;
import eu.riscoss.reasoner.DataType;
import eu.riscoss.reasoner.Distribution;
import eu.riscoss.reasoner.Evidence;
import eu.riscoss.reasoner.Field;
import eu.riscoss.reasoner.FieldType;
import eu.riscoss.reasoner.ModelSlice;
import eu.riscoss.reasoner.ReasoningLibrary;
import eu.riscoss.reasoner.RiskAnalysisEngine;
import eu.riscoss.shared.AnalysisOption;
import eu.riscoss.shared.AnalysisResult;

@Path("analysis")
public class AnalysisManager {
	
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
	
	@GET @Path( "/session/list")
	public String listRAS(
			@QueryParam("entity") String entity,
			@QueryParam("rc") String rc
			) {
		RiscossDB db = DBConnector.openDB();
		try {
			JsonObject json = new JsonObject();
			JsonArray array = new JsonArray();
			for( String ras : db.listRAS( entity,  rc ) ) {
				JsonObject o = new JsonObject();
				o.addProperty( "id", ras );
				array.add( o );
			}
			json.add( "list", array );
			return json.toString();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST
	@Path("/session/new")
	public String createSession(
			@QueryParam("rc") String rc,
			@QueryParam("target") String target
			) {
		RiscossDB db = DBConnector.openDB();
		try {
			
			// Create a new risk analysis session
			RiskAnalysisSession ras = db.createRAS();
			
			// setup layers
			ras.setLayers( db.layerNames() );
			
			// set target
			ras.setTarget( target );
			
			// setup entities
			gatherEntityTree( target, db, ras );
			
			// setup risk configuration
			ras.setRCName( rc );
			ras.setRCModels( db.getRCModels( rc ) );
			
			cacheRDRData( ras, db );
			
			db.saveRAS( ras );
			
			JsonObject ret = new JsonObject();
			ret.addProperty( "id", ras.getId() );
			
			return ret.toString();
			
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	/* This method goes through the hierarchy of entities in a given risk analysis session,
	 * reads the required data from the rdr, and stores the data in the risk analysis session
	 */
	@GET @Path("/session/{sid}/update-data")
	public void updateSessionData( @PathParam("sid") String sid ) {
		RiscossDB db = DBConnector.openDB();
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
		
		new EntityTreeBrowser( db ).analyseEntity( ras.getTarget(), new EntityTreeBrowser.Callback<AnalysisDataSource>( new AnalysisDataSource( ras, db ) ) {
			@Override
			public void onEntityFound( String entity ) {
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

			@Override
			public void onLayerFound( String layer ) {}
		} );
		
	}
	
	@GET
	@Path("/session/{sid}/missing-data")
	public String getSessionMissingData(
			@PathParam("sid") String sid
			) {
		RiscossDB db = DBConnector.openDB();
		
		try {
			// Sia i dati che mancano, sia quelli marcati come "user"
		}
		finally {
			DBConnector.closeDB( db );
		}
		
		return "";
	}
	
	@PUT @Path("/session/{sid}/missing-data")
	public void setSessionMissingData(
			@PathParam("sid") String sid,
			@HeaderParam("values") String values
			) {
		JsonObject json = (JsonObject)new JsonParser().parse( values );
		RiscossDB db = DBConnector.openDB();
		try {
			RiskAnalysisSession ras = db.openRAS( sid );
			JsonArray jentities = json.get( "entities" ).getAsJsonArray();
			for( int i = 0; i < jentities.size(); i++ ) {
				JsonObject je = jentities.get( i ).getAsJsonObject();
				String entity = je.get( "name" ).getAsString();
				JsonArray jvalues = je.get( "values" ).getAsJsonArray();
				for( int v = 0; v < jvalues.size(); v++ ) {
					String id = jvalues.get( v ).getAsJsonObject().get( "id" ).getAsString();
					String value = jvalues.get( v ).getAsJsonObject().get( "value" ).getAsString();
					ras.saveInput( entity, id, "user", value );
				}
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/session/{sid}/summary")
	public String getSessionSummary(
			@PathParam("sid") String sid
			) {
		RiscossDB db = DBConnector.openDB();
		try {
			RiskAnalysisSession ras = db.openRAS( sid );
			JsonObject json = new JsonObject();
			json.addProperty( "id", ras.getId() );
			json.addProperty( "target", ras.getTarget() );
			json.addProperty( "rc", ras.getRCName() );
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
	
	@GET
	@Path("/session/{sid}/results")
	@Produces("application/json")
	public String getRAD( 
			@PathParam("sid") String sid ) {
		RiscossDB db = DBConnector.openDB();
		try {
			RiskAnalysisSession ras = db.openRAS( sid );
			return ras.readResults();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@DELETE @Path("/session/{sid}/delete")
	public void deleteRiskAnalysis( @PathParam("sid") String sid ) {
		RiscossDB db = DBConnector.openDB();
		try {
			db.destroyRAS( sid );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/session/{sid}/newrun")
	public String runRiskAnalysis(
			@PathParam("sid") String sid,
			@DefaultValue("RunThrough") @QueryParam("opt") String strOpt /* See AnalysisOption.RunThrough */
			) {
		
		RiscossDB db = DBConnector.openDB();
		
		try {
			
			TimeDiff.get().init();
			TimeDiff.get().log( "Initializing analysis" );
			
			// Create a new risk analysis session
			RiskAnalysisSession ras = db.openRAS( sid );
			
			TimeDiff.get().log( "Starting analysis process" );
			AnalysisProcess proc = new AnalysisProcess( db );
			
			// Apply analysis algorithm
			proc.start( ras );
			
			// Save session (in case of in-memory sessions)
			db.saveRAS( ras );
			
			TimeDiff.get().log( "Encoding analysis results" );
			JsonObject res = getAnalysisResults( ras );
			
			String ret = res.toString();
			
			TimeDiff.get().log( "Caching analysis results" );
			ras.saveResults( ret );
			
			ras.setTimestamp( new Date().getTime() );
			
			TimeDiff.get().log( "Analysis done" );
			
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
//						Map<String,Object> map = ras.getResult( layerName, entityName, indicatorId );
//						Object datatype = map.get( "datatype" );
//						if( datatype == null ) datatype = DataType.REAL.name();
//						DataType dt = DataType.valueOf( datatype.toString() );
						DataType dt = DataType.valueOf( ras.getResult( layerName, entityName, indicatorId, "datatype", DataType.REAL.name() ) );
						o.addProperty( "datatype", dt.name().toLowerCase() );
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
		
		
		json.addProperty( "result", AnalysisResult.Done.name() );
//		json.addProperty( "result", getResults().getValue( "", "", "", "analysis-result", AnalysisResult.Failure.name() ) );
		
			JsonObject info = new JsonObject();
			info.addProperty( "entity", ras.getTarget() );
			json.add( "info", info );
			
			// TODO: read the whole set of input values
//			json.add( "inputs", ras.getInput(  ) );
		
		return json;
		
	}
	
	void gatherEntityTree( String entity, RiscossDB db, RiskAnalysisSession ras ) {
		
		String layer = db.layerOf( entity );
		
		ras.addEntity( entity, layer );
		
		for( String child : db.getChildren( entity ) ) {
			gatherEntityTree( child, db, ras );
			ras.setParent( child, entity );
		}
		
	}
	
	@POST
	@Path("/new")
	public String runAnalysisWithRealDataOld( 
			@QueryParam("rc") String rc,
			@QueryParam("target") String target,
			@QueryParam("verbosity") String flags,
			@DefaultValue("RunThrough") @QueryParam("opt") String strOpt /* See AnalysisOption.RunThrough */,
			@DefaultValue("") @HeaderParam("customData") String customData ) throws Exception {
		
		AnalysisOption opt = AnalysisOption.valueOf( strOpt );
		if( opt == null ) opt = AnalysisOption.RunThrough;
		
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
		
		RiscossDB db = DBConnector.openDB();
		
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
				if( opt == AnalysisOption.RequestMissingData ) {
					JsonObject ret = new JsonObject();
					ret.addProperty( "result", AnalysisResult.DataMissing.name() );
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
			
			o.addProperty( "result", AnalysisResult.Done.name() );
			
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
	
	@POST
	@Path("/whatif")
	@Consumes(MediaType.APPLICATION_JSON)
	public String runAnalysisWithCustomData( 
//			@QueryParam("rc") String rc,
			@QueryParam("models") String modelsString,
			@HeaderParam("values") String valuesString ) throws Exception {
		
		JsonObject jvalues = (JsonObject)new JsonParser().parse( valuesString );
		JsonArray jmodels = (JsonArray)new JsonParser().parse( modelsString );
		
		RiskAnalysisEngine rae = ReasoningLibrary.get().createRiskAnalysisEngine();
		
		RiscossDB db = DBConnector.openDB();
		
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
				case DISTRIBUTION:
					break;
				case EVIDENCE:
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
		
//		{
//			ret = new JsonArray();
//			
//			for( Chunk c : rae.queryModel( ModelSlice.OUTPUT_DATA ) ) {
//				Field f = rae.getField( c, FieldType.OUTPUT_VALUE );
//				JsonObject o = new JsonObject();
//				o.addProperty( "id", c.getId() );
//				o.addProperty( "datatype", f.getDataType().name().toLowerCase() );
//				switch( f.getDataType() ) {
//				case EVIDENCE: {
//					Evidence e = f.getValue();
//					o.addProperty( "p", "" + e.getPositive() );
//					o.addProperty( "m", "" + e.getNegative() );
////					if( "full".equals( options.getProperty( "verbosity", "" ) ) ) {
//						JsonObject je = new JsonObject();
//						je.addProperty( "p", e.getPositive() );
//						je.addProperty( "m", e.getNegative() );
//						je.addProperty( "e", e.getDirection() );
////						je.addProperty( "c", e.getConflict() );
////						je.addProperty( "s", e.getStrength() );
//						o.add( "e", je );
////						o.addProperty( "description", "" + rae.getField( c, FieldType.DESCRIPTION ).getValue() );
////						o.addProperty( "label", "" + rae.getField( c, FieldType.LABEL ).getValue() );
////					}
//				}
//				break;
//				case DISTRIBUTION: {
//					Distribution d = f.getValue();
//					JsonArray values = new JsonArray();
//					for( int i = 0; i <  d.getValues().size(); i++ ) {
//						values.add( new JsonPrimitive( "" + d.getValues().get( i ) ) );
//					}
//					o.add( "value", values );
//				}
//				break;
//				case INTEGER:
//					o.addProperty( "value", f.getValue().toString() );
//					break;
//				case NaN:
//					break;
//				case REAL:
//					o.addProperty( "value", f.getValue().toString() );
//					break;
//				case STRING:
//					o.addProperty( "value", f.getValue().toString() );
//					break;
//				default:
//					break;
//				}
//				ret.add( o );
//			}
//		}
		
		JsonObject o = new JsonObject();
		
		o.add( "results", ret );
		
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
}