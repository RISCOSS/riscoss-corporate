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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import eu.riscoss.db.RiscossDB;
import eu.riscoss.reasoner.Chunk;
import eu.riscoss.reasoner.DataType;
import eu.riscoss.reasoner.Distribution;
import eu.riscoss.reasoner.Evidence;
import eu.riscoss.reasoner.Field;
import eu.riscoss.reasoner.FieldType;
import eu.riscoss.reasoner.ModelSlice;
import eu.riscoss.reasoner.ReasoningLibrary;
import eu.riscoss.reasoner.RiskAnalysisEngine;
import eu.riscoss.shared.EChunkType;
import eu.riscoss.shared.JChunkItem;
import eu.riscoss.shared.JChunkList;
import eu.riscoss.shared.JChunkValue;


@Path("models")
public class ModelManager {
	
	Gson gson = new Gson();
	
	@GET @Path("/{domain}/list")
	public String getList( @DefaultValue("Playground") @PathParam("domain") String domain ) {
		
		JsonArray a = new JsonArray();
		
		RiscossDB db = DBConnector.openDB( domain );
		try {
			for( String model : db.getModelList() ) {
				JsonObject o = new JsonObject();
				o.addProperty( "name", model );
				a.add( o );
			}
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		finally {
			DBConnector.closeDB( db );
		}
		
		return a.toString();
 
	}
	
	@GET @Path("/{domain}/model/chunklist")
	public String getModelChunkList( @DefaultValue("Playground") @PathParam("domain") String domain, @HeaderParam("models") String models ) {
		
		JsonArray json = (JsonArray)new JsonParser().parse( models );
		
		JChunkList list = new JChunkList();
		
		RiscossDB db = DBConnector.openDB( domain );
		try {
			
			for( int i = 0; i < json.size(); i++ ) {
				
				String modelName = json.get( i ).getAsString();
				
				String blob = db.getModelBlob( modelName );
				
				if( blob != null ) {
					
					RiskAnalysisEngine rae = ReasoningLibrary.get().createRiskAnalysisEngine();
					rae.loadModel( blob );
					
					
					for( Chunk c : rae.queryModel( ModelSlice.INPUT_DATA ) ) {
						
						JChunkItem jchunk = new JChunkItem();
						
						jchunk.setId( c.getId() );
						jchunk.setLabel( getProperty( rae, c, FieldType.LABEL, c.getId() ) );
						jchunk.setDescription( getProperty( rae, c, FieldType.DESCRIPTION, "" ) );
						{
							String type = rae.getField( c, FieldType.TYPE ).getValue();
							if( "Goal".equals( type ) ) {
								jchunk.setType( EChunkType.GOAL );
							}
							else if( "Risk".equals( type ) ) {
								jchunk.setType( EChunkType.RISK );
							}
							else if( "Indicator".equals( type ) ) {
								jchunk.setType( EChunkType.INDICATOR );
							}
							else {
								jchunk.setType( EChunkType.OTHER );
							}
						}
						
						switch( rae.getField( c, FieldType.INPUT_VALUE ).getDataType() ) {
						case DISTRIBUTION: {
							
							JChunkValue.JDistribution jd = new JChunkValue.JDistribution();
							Distribution d = rae.getField( c, FieldType.INPUT_VALUE ).getValue();
							jd.values.addAll( d.getValues() );
							jchunk.setValue( jd );
							
						}
							break;
						case EVIDENCE: {
							
							JChunkValue.JEvidence je = new JChunkValue.JEvidence();
							Evidence e = rae.getField( c, FieldType.INPUT_VALUE ).getValue();
							je.p = e.getPositive();
							je.m = e.getNegative();
							jchunk.setValue( je );
							
						}
							break;
						case INTEGER: {
							
							JChunkValue.JInteger ji = new JChunkValue.JInteger();
							
							if( rae.getField( c, FieldType.MIN ).getDataType() != DataType.NaN )
								ji.min = rae.getField( c, FieldType.MIN ).getValue();
							if( rae.getField( c, FieldType.MAX ).getDataType() != DataType.NaN )
								ji.max = rae.getField( c, FieldType.MAX ).getValue();
							ji.value = (int)rae.getField( c, FieldType.INPUT_VALUE ).getValue();
							
							jchunk.setValue( ji );
							
						}
							break;
						case NaN:
							break;
						case REAL: {
							JChunkValue.JReal ji = new JChunkValue.JReal();
							
							if( rae.getField( c, FieldType.MIN ).getDataType() != DataType.NaN )
								ji.min = rae.getField( c, FieldType.MIN ).getValue();
							if( rae.getField( c, FieldType.MAX ).getDataType() != DataType.NaN )
								ji.max = rae.getField( c, FieldType.MAX ).getValue();
							ji.value = (double)rae.getField( c, FieldType.INPUT_VALUE ).getValue();
							
							jchunk.setValue( ji );
							
						}
							break;
						case STRING:
							break;
						default:
							break;
						}
						list.inputs.add( jchunk );
					}
					
					for( Chunk c : rae.queryModel( ModelSlice.OUTPUT_DATA ) ) {
						
						JChunkItem jchunk = new JChunkItem();
						
						jchunk.setId( c.getId() );
						{
							String type = rae.getField( c, FieldType.TYPE ).getValue();
							if( "Goal".equals( type ) ) {
								jchunk.setType( EChunkType.GOAL );
							}
							else if( "Risk".equals( type ) ) {
								jchunk.setType( EChunkType.RISK );
							}
							else if( "Indicator".equals( type ) ) {
								jchunk.setType( EChunkType.INDICATOR );
							}
							else {
								jchunk.setType( EChunkType.OTHER );
							}
						}
						
						String label = c.getId();
						Field f = rae.getField( c, FieldType.LABEL );
						if( f != null ) {
							label = f.getValue();
						}
						jchunk.setLabel( label );
						f = rae.getField( c, FieldType.DESCRIPTION );
						if( f != null ) {
							jchunk.setDescription( (String)f.getValue() );
						}
						switch( rae.getField( c, FieldType.OUTPUT_VALUE ).getDataType() ) {
						case DISTRIBUTION: {
							Distribution d = rae.getField( c, FieldType.OUTPUT_VALUE ).getValue();
							JChunkValue.JDistribution jd = new JChunkValue.JDistribution();
							jd.values.addAll( d.getValues() );
							jchunk.setValue( jd );
						}
							break;
						case EVIDENCE: {
							JChunkValue.JEvidence je = new JChunkValue.JEvidence();
							Evidence e = rae.getField( c, FieldType.OUTPUT_VALUE ).getValue();
							je.p = e.getPositive();
							je.m = e.getNegative();
							jchunk.setValue( je );
						}
							break;
						case INTEGER: {
							JChunkValue.JInteger ji = new JChunkValue.JInteger();
							ji.value = (int)rae.getField( c, FieldType.OUTPUT_VALUE ).getValue();
							jchunk.setValue( ji );
						}
							break;
						case NaN:
							break;
						case REAL: {
							JChunkValue.JReal ji = new JChunkValue.JReal();
							ji.value = (double)rae.getField( c, FieldType.OUTPUT_VALUE ).getValue();
							jchunk.setValue( ji );
						}
							break;
						case STRING:
							break;
						default:
							break;
						}
						list.outputs.add( jchunk );
					}
					
				}
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
		
		String ret = gson.toJson( list );
		
		System.out.println( ret );
		
		return ret;
	}

	@GET
	@Path("/{domain}/model/chunks")
	public String getModelChunks( @DefaultValue("Playground") @PathParam("domain") String domain, @HeaderParam("models") String models ) {
		
		JsonArray json = (JsonArray)new JsonParser().parse( models );
		
		JsonObject ret = new JsonObject();
		JsonArray inputs = new JsonArray();
		JsonArray outputs = new JsonArray();
		ret.add( "inputs", inputs );
		ret.add( "outputs", outputs );
		
		RiscossDB db = DBConnector.openDB( domain );
		try {
			
			for( int i = 0; i < json.size(); i++ ) {
				
				String modelName = json.get( i ).getAsString();
				
				String blob = db.getModelBlob( modelName );
				
				if( blob != null ) {
					
					RiskAnalysisEngine rae = ReasoningLibrary.get().createRiskAnalysisEngine();
					rae.loadModel( blob );
					
					
					for( Chunk c : rae.queryModel( ModelSlice.INPUT_DATA ) ) {
						JsonObject o = new JsonObject();
						o.addProperty( "id", c.getId() );
						o.addProperty( "label", getProperty( rae, c, FieldType.LABEL, c.getId() ) );
						o.addProperty( "description", getProperty( rae, c, FieldType.DESCRIPTION, "" ) );
						o.addProperty( "datatype", 
								rae.getField( c, FieldType.INPUT_VALUE ).getDataType().name() );
						switch( rae.getField( c, FieldType.INPUT_VALUE ).getDataType() ) {
						case DISTRIBUTION: {
							o.addProperty( "min", "0" );
							o.addProperty( "max", "1" );
							Distribution d = rae.getField( c, FieldType.INPUT_VALUE ).getValue();
							o.add( "value", toJson( d ) );
						}
							break;
						case EVIDENCE: {
							Evidence e = rae.getField( c, FieldType.INPUT_VALUE ).getValue();
							o.add( "value", toJson( e ) );
						}
							break;
						case INTEGER: {
							int min = 0;
							if( rae.getField( c, FieldType.MIN ).getDataType() != DataType.NaN )
								min = rae.getField( c, FieldType.MIN ).getValue();
							int max = 100;
							if( rae.getField( c, FieldType.MAX ).getDataType() != DataType.NaN )
								max = rae.getField( c, FieldType.MAX ).getValue();
							o.addProperty( "min", "" + min );
							o.addProperty( "max", "" + max );
							o.addProperty( "value", (int)rae.getField( c, FieldType.INPUT_VALUE ).getValue() );
						}
							break;
						case NaN:
							break;
						case REAL: {
							double min = 0;
							if( rae.getField( c, FieldType.MIN ).getDataType() != DataType.NaN )
								min = rae.getField( c, FieldType.MIN ).getValue();
							double max = 1;
							if( rae.getField( c, FieldType.MAX ).getDataType() != DataType.NaN )
								max = rae.getField( c, FieldType.MAX ).getValue();
							o.addProperty( "min", "" + min );
							o.addProperty( "max", "" + max );
							o.addProperty( "value", (double)rae.getField( c, FieldType.INPUT_VALUE ).getValue() );
						}
							break;
						case STRING:
							break;
						default:
							break;
						}
						inputs.add( o );
					}
					
					for( Chunk c : rae.queryModel( ModelSlice.OUTPUT_DATA ) ) {
						JsonObject o = new JsonObject();
						o.addProperty( "id", c.getId() );
						o.addProperty( "datatype", 
								rae.getField( c, FieldType.OUTPUT_VALUE ).getDataType().name() );
						String label = c.getId();
						Field f = rae.getField( c, FieldType.LABEL );
						if( f != null ) {
							label = f.getValue();
						}
						o.addProperty( "label", label );
						f = rae.getField( c, FieldType.DESCRIPTION );
						if( f == null ) {
							o.addProperty( "description", "" );
						}
						else {
							o.addProperty( "description", (String)f.getValue() );
						}
						switch( rae.getField( c, FieldType.OUTPUT_VALUE ).getDataType() ) {
						case DISTRIBUTION: {
							Distribution d = rae.getField( c, FieldType.OUTPUT_VALUE ).getValue();
							o.add( "value", toJson( d ) );
						}
							break;
						case EVIDENCE: {
							Evidence e = rae.getField( c, FieldType.OUTPUT_VALUE ).getValue();
							o.add( "value", toJson( e ) );
						}
							break;
						case INTEGER: {
							o.addProperty( "value", (int)rae.getField( c, FieldType.OUTPUT_VALUE ).getValue() );
						}
							break;
						case NaN:
							break;
						case REAL: {
							o.addProperty( "value", (double)rae.getField( c, FieldType.OUTPUT_VALUE ).getValue() );
						}
							break;
						case STRING:
							break;
						default:
							break;
						}
						outputs.add( o );
					}
					
				}
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
		
		System.out.println( ret.toString() );
		
		return ret.toString();
	}

	private JsonElement toJson(Evidence e) {
		JsonObject o = new JsonObject();
		o.addProperty( "p", e.getPositive() );
		o.addProperty( "e", e.getNegative() );
		return o;
	}

	private JsonElement toJson(Distribution d) {
		JsonArray a = new JsonArray();
		for( double val : d.getValues() ) {
			a.add( new JsonPrimitive( "" + val ) );
		}
		return a;
	}

	private String getProperty( RiskAnalysisEngine rae, Chunk chunk, FieldType ft, String def ) {
		Field f = rae.getField( chunk, ft );
		if( f == null ) return def;
		if( f.getValue() == null ) return def;
		return f.getValue().toString();
	}
	
	@GET @Path("/{domain}/model/get")
	public String getInfo( @DefaultValue("Playground") @PathParam("domain") String domain, @QueryParam("name") String name ) {
		RiscossDB db = DBConnector.openDB( domain );
		try {
			String filename = db.getModelFilename( name );  //modelfilename
			String descfile = db.getModelDescFielname(name);
			JsonObject o = new JsonObject();
			
			o.addProperty( "name", name );
			o.addProperty( "modelfilename", filename);
			o.addProperty( "modeldescfilename", descfile);
			return o.toString();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/model/blob")
	public String getBlob( @DefaultValue("Playground") @PathParam("domain") String domain, @QueryParam("name") String name ) {
		RiscossDB db = DBConnector.openDB( domain );
		try {
			String blob = db.getModelBlob( name );
			JsonObject o = new JsonObject();
			o.addProperty( "name", name );
			o.addProperty( "blob", blob );
			return o.toString();
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@DELETE @Path("/{domain}/model/delete")
	public void deleteModel( @DefaultValue("Playground") @PathParam("domain") String domain, @QueryParam("name") String name ) {
		RiscossDB db = DBConnector.openDB( domain );
		try {
			db.removeModel( name );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/model/changename")
	public void changeModelName( @DefaultValue("Playground") @PathParam("domain") String domain, @QueryParam("name") String name, @QueryParam("newname") String newName ) throws RuntimeException{
		RiscossDB db = DBConnector.openDB( domain );
		boolean duplicate = false;
		
		//String response = "";
		//try {
			for (String storedmodel : db.getModelList()) {
				// System.out.println(storedmodel);
				if (storedmodel.equals(newName)) {
					duplicate = true;
					// response = "<response>\n" +
					// "Duplicate model name. Please delete the stored model first."+
					// "</response>\n";
					//response = "Error: This name is already in use.";
					 throw new RuntimeException("Duplicate entry");
					//break;
					// throw new
					// UploadActionException("Duplicate model name. Please delete the stored model first.");
				}
			}
			if (duplicate == false) {
				db.changeModelName(name, newName);
				//response = "Success";
			}
			
		//} finally {
			DBConnector.closeDB(db);
		//}
		//return response;
	}

}