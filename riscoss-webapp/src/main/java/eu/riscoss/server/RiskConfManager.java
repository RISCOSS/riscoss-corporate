package eu.riscoss.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import eu.riscoss.db.RiscossDB;

@Path("rcs")
public class RiskConfManager {
	
	@GET
	@Path( "/list" )
	public String list(
			@DefaultValue("") @QueryParam("entity") String entity ) {
		
		RiscossDB db = DBConnector.openDB();
		
		JsonArray a = new JsonArray();
		
		try {
			
			if( entity != null ) {
				if( !"".equals( entity ) ) {
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
		finally {
			DBConnector.closeDB( db );
		}
		
		return a.toString();
	}
	
	@GET
	@Path("/rc/get")
	public String getContent( @QueryParam("name") String name ) {
		
		JsonObject json = new JsonObject();
		
		RiscossDB db = DBConnector.openDB();
		try {
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
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@PUT
	@Path("/rc/put")
	public void setContent( @QueryParam("content") String value ) {
		JsonObject json = (JsonObject)new JsonParser().parse( value );
		RiscossDB db = DBConnector.openDB();
		try {
			String rc = json.get( "name" ).getAsString();
			try {
				List<String> list = new ArrayList<String>();
				JsonArray array = json.get( "models" ).getAsJsonArray();
				for( int i = 0; i < array.size(); i++ ) {
					list.add( array.get( i ).getAsJsonObject().get( "name" ).getAsString() );
				}
				db.setModelsFromRiskCfg( rc, list );
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
				db.setRCModels( rc, map );
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST
	@Path("/rc/new")
	public String createNew( @QueryParam("name") String name ) {
		RiscossDB db = DBConnector.openDB();
		try {
			db.createRiskConfiguration( name );
			JsonObject o = new JsonObject();
			o.addProperty( "name", name );
			return o.toString();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@DELETE
	@Path("/rc/delete")
	public String delete( @QueryParam("name") String name ) {
		RiscossDB db = DBConnector.openDB();
		try {
			db.removeRiskConfiguration( name );
			JsonObject o = new JsonObject();
			o.addProperty( "name", name );
			return o.toString();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	// Outdated; was used for RC-based what-if analysis
//	@GET
//	@Path("/rc/chunks")
//	public String getChunks( @QueryParam("rc") String rc ) {
//		
//		RiscossDB db = DBConnector.openDB();
//		try {
//			
//			JsonObject ret = new JsonObject();
//			
//			for( String modelName : db.getModelsFromRiskCfg( rc ) ) {
//				
//				String blob = db.getModelBlob( modelName );
//				
//				if( blob != null ) {
//					
//					RiskAnalysisEngine rae = ReasoningLibrary.get().createRiskAnalysisEngine();
//					rae.loadModel( blob );
//					
//					JsonArray a = new JsonArray();
//					
//					for( Chunk c : rae.queryModel( ModelSlice.INPUT_DATA ) ) {
//						JsonObject o = new JsonObject();
//						o.addProperty( "id", c.getId() );
//						o.addProperty( "label", getProperty( rae, c, FieldType.LABEL, c.getId() ) );
//						o.addProperty( "description", getProperty( rae, c, FieldType.DESCRIPTION, "" ) );
//						o.addProperty( "datatype", 
//								rae.getField( c, FieldType.INPUT_VALUE ).getDataType().name() );
//						switch( rae.getField( c, FieldType.INPUT_VALUE ).getDataType() ) {
//						case DISTRIBUTION: {
//							o.addProperty( "min", "0" );
//							o.addProperty( "max", "1" );
//							Distribution d = rae.getField( c, FieldType.INPUT_VALUE ).getValue();
//							o.add( "value", toJson( d ) );
//						}
//						break;
//						case EVIDENCE: {
//							Evidence e = rae.getField( c, FieldType.INPUT_VALUE ).getValue();
//							o.add( "value", toJson( e ) );
//						}
//						break;
//						case INTEGER: {
//							int min = 0;
//							if( rae.getField( c, FieldType.MIN ).getDataType() != DataType.NaN )
//								min = rae.getField( c, FieldType.MIN ).getValue();
//							int max = 100;
//							if( rae.getField( c, FieldType.MAX ).getDataType() != DataType.NaN )
//								max = rae.getField( c, FieldType.MAX ).getValue();
//							o.addProperty( "min", "" + min );
//							o.addProperty( "max", "" + max );
//							//						range.setStep( "1" );
//							o.addProperty( "value", (int)rae.getField( c, FieldType.INPUT_VALUE ).getValue() );
//						}
//						break;
//						case NaN:
//							break;
//						case REAL: {
//							double min = 0;
//							if( rae.getField( c, FieldType.MIN ).getDataType() != DataType.NaN )
//								min = rae.getField( c, FieldType.MIN ).getValue();
//							double max = 1;
//							if( rae.getField( c, FieldType.MAX ).getDataType() != DataType.NaN )
//								max = rae.getField( c, FieldType.MAX ).getValue();
//							o.addProperty( "min", "" + min );
//							o.addProperty( "max", "" + max );
//							o.addProperty( "value", (double)rae.getField( c, FieldType.INPUT_VALUE ).getValue() );
//						}
//						break;
//						case STRING:
//							break;
//						default:
//							break;
//						}
//						a.add( o );
//					}
//					
//					ret.add( "inputs", a );
//					
//					a = new JsonArray();
//					
//					for( Chunk c : rae.queryModel( ModelSlice.OUTPUT_DATA ) ) {
//						JsonObject o = new JsonObject();
//						o.addProperty( "id", c.getId() );
//						String label = c.getId();
//						Field f = rae.getField( c, FieldType.LABEL );
//						if( f != null ) {
//							label = f.getValue();
//						}
//						o.addProperty( "label", label );
//						f = rae.getField( c, FieldType.DESCRIPTION );
//						if( f == null ) {
//							o.addProperty( "description", "" );
//						}
//						else {
//							o.addProperty( "description", (String)f.getValue() );
//						}
//						a.add( o );
//					}
//					
//					ret.add( "outputs", a );
//				}
//				
//				System.out.println( ret.toString() );
//				
//				return ret.toString();
//			}
//		}
//		finally {
//			DBConnector.closeDB( db );
//		}
//		
//		return "";
//	}
//	
//	private String getProperty( RiskAnalysisEngine rae, Chunk chunk, FieldType ft, String def ) {
//		Field f = rae.getField( chunk, ft );
//		if( f == null ) return def;
//		if( f.getValue() == null ) return def;
//		return f.getValue().toString();
//	}
//	
//	private JsonElement toJson(Evidence e) {
//		JsonObject o = new JsonObject();
//		o.addProperty( "p", e.getPositive() );
//		o.addProperty( "e", e.getNegative() );
//		return o;
//	}
//	
//	private JsonElement toJson(Distribution d) {
//		JsonArray a = new JsonArray();
//		for( double val : d.getValues() ) {
//			a.add( new JsonPrimitive( "" + val ) );
//		}
//		return a;
//	}

}