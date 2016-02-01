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

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.DELETE;
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
import eu.riscoss.db.RiscossElements;
import eu.riscoss.fbk.language.Proposition;
import eu.riscoss.fbk.language.Relation;
import eu.riscoss.reasoner.Chunk;
import eu.riscoss.reasoner.DataType;
import eu.riscoss.reasoner.Distribution;
import eu.riscoss.reasoner.Evidence;
import eu.riscoss.reasoner.FBKRiskAnalysisEngine;
import eu.riscoss.reasoner.Field;
import eu.riscoss.reasoner.FieldType;
import eu.riscoss.reasoner.ModelSlice;
import eu.riscoss.reasoner.ReasoningLibrary;
import eu.riscoss.reasoner.RiskAnalysisEngine;
import eu.riscoss.shared.EChunkType;
import eu.riscoss.shared.JChunkItem;
import eu.riscoss.shared.JChunkList;
import eu.riscoss.shared.JChunkValue;
import eu.riscoss.shared.JProposition;
import eu.riscoss.shared.JRelation;
import eu.riscoss.shared.JRiskModel;
import eu.riscoss.shared.RiscossUtil;
import gwtupload.server.exceptions.UploadActionException;


@Path("models")
@Info("Manages the upload, download, modification and inspection of risk models")
public class ModelManager {
	
	Gson gson = new Gson();
	
	@GET @Path("/{domain}/list")
	@Info("Returns a list of models previously uploaded in the selected domain")
	public String getList( 
			@PathParam("domain") @Info("The work domain")				String domain,
			@HeaderParam("token") @Info("The authentication token")		String token 
			) throws Exception {
		
		JsonArray a = new JsonArray();
		
		RiscossDB db = null;
		try {
			db = DBConnector.openDB( domain, token );
			for( String model : db.getModelList() ) {
				JsonObject o = new JsonObject();
				o.addProperty( "name", model );
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
	/**
	 * 
	 * @param domain
	 * @param token
	 * @param models a list of models (JsonArray) in the body
	 * @return
	 * @throws Exception 
	 */
	@POST @Path("/{domain}/chunklist")
	@Info("Returns a list of input and output chunks for the specified models; " + 
	"it also includes the type of the chunk (goal, risk, indicator)")
	//returns also the type of each object (goal/risk/...) and is used in AHP
	public String getModelChunkList(
			@PathParam("domain") @Info("The work domain")				String domain, 
			@HeaderParam("token") @Info("The authentication token")		String token,
			@Info("A JSONized array of model names")					String models 
			) throws Exception {
		
		JsonArray json = (JsonArray)new JsonParser().parse( models );
		
		JChunkList list = new JChunkList();
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
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
		catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( db );
		}
		
		String ret = gson.toJson( list );
		
		System.out.println( ret );
		
		return ret;
	}
	
	/**
	 * 
	 * @param domain
	 * @param token
	 * @param models a list of models (JsonArray)
	 * @return
	 * @throws Exception 
	 */
	@POST 	@Path("/{domain}/chunks")
	@Info("Returns a list of input and output chunks for the specified models")
	//used in the whatifanalysis, and in the ModelsModule (for showing the content)
	//returns various info, but not the types.
	public String getModelChunks(
			@PathParam("domain") @Info("The work domain")				String domain,
			@HeaderParam("token") @Info("The authentication token")		String token, 
			@Info("A JSONized array of model names")					String models 
			) throws Exception {
		
		JsonArray json = (JsonArray)new JsonParser().parse( models );
		
		JsonObject ret = new JsonObject();
		JsonArray inputs = new JsonArray();
		JsonArray outputs = new JsonArray();
		ret.add( "inputs", inputs );
		ret.add( "outputs", outputs );
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			RiskAnalysisEngine rae = ReasoningLibrary.get().createRiskAnalysisEngine();
			
			for( int i = 0; i < json.size(); i++ ) {
				String modelName = json.get( i ).getAsString();
				String blob = db.getModelBlob( modelName );
				if( blob != null ) {
					rae.loadModel( blob );
				}
			}
			
			Set<String> set = new HashSet<>();
			
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
				if( !set.contains( c.getId() ) ) {
					inputs.add( o );
					set.add( c.getId() );
				}
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
		catch (Exception e) {
			throw e;
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
	
	@GET @Path("/{domain}/{model}/get")
	@Info("Returns information about a stored model")
	public String getInfo(
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("model") @Info("The name of the model")				String name 
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			String filename = db.getModelFilename( name );  //modelfilename
			String descfile = db.getModelDescFielname(name);
			JsonObject o = new JsonObject();
			
			o.addProperty( "name", name );
			o.addProperty( "modelfilename", filename);
			o.addProperty( "modeldescfilename", descfile);
			return o.toString();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/{model}/blob")
	@Info("Reutrn the actual content of a stored model, e.g., of a previously uploaded xml file")
	public String getBlob( 
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("model") @Info("The model name")						String name 
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			String blob = db.getModelBlob( name );
			JsonObject o = new JsonObject();
			o.addProperty( "name", name );
			o.addProperty( "blob", blob );
			return o.toString();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@DELETE @Path("/{domain}/{model}/delete")
	@Info("Deletes a model from the database")
	public void deleteModel( 
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("model") @Info("The model name")						String name 
			) throws Exception {
		RiscossDB db = null;
		try {
			
			db = DBConnector.openDB( domain, token );
			
			db.removeModel( name );
		} 
		catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/{model}/upload")
	@Info("Stores a model into the database")
	public void uploadModel(
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("domain") @Info("The work domain")					String domain, 
			@PathParam("model") @Info("The model name")						String modelname, 
			String content
			) {
		
		RiscossDB db = null;
		
		try {
			db = DBConnector.openDB( domain, token );
			
			//attention:filename sanitation is not directly notified to the user
			modelname = RiscossUtil.sanitize(modelname);
			
			for (String storedmodel : db.getModelList()) {
				if (storedmodel.equals(modelname)){
					return;
				}
			}
			db.storeModel( content, modelname );
			
		} catch (Exception e) {
			throw new UploadActionException(e);
		}
		finally {
			DBConnector.closeDB( db );
		}
		
	}
	
	@POST @Path("/{domain}/{model}/rename")
	@Info("Changes the name of a model")
	public void changeModelName( 
			@PathParam("domain") @Info("The work domain")					String domain,
			@HeaderParam("token") @Info("Then authentication token")		String token, 
			@PathParam("model") @Info("The name of an existing model")		String name, 
			@QueryParam("newname") @Info("The new name of the model")		String newName 
			) throws Exception{
		
		RiscossDB db = null;
		
		boolean duplicate = false;
		
		//String response = "";
		try {
			
			db = DBConnector.openDB( domain, token );
			
			for (String storedmodel : db.getModelList()) {
				if (storedmodel.equals(newName)) {
					duplicate = true;
					throw new RuntimeException("Duplicate entry");
				}
			}
			if (duplicate == false) {
				db.changeModelName(name, newName);
			}
			
		} catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB(db);
		}
	}
	
	@GET @Path("/{domain}/{name}/content")
	@Info("Experimental. Returns a structured representation of the content of a RiskML model")
	public String getRiskModelContent(
			@PathParam("domain") @Info("The work domain")					String domain, 
			@HeaderParam("token") @Info("The authentication token")			String token, 
			@PathParam("name") @Info("The name of the model")				String name 
			) throws Exception {
		
		RiscossDB db = null;
		
		try {
			db = DBConnector.openDB( domain, token );
			
			String blob = db.getModelBlob( name );
			
			FBKRiskAnalysisEngine fbk = new FBKRiskAnalysisEngine();
			
			fbk.loadModel( blob );
			
			JRiskModel jmodel = new JRiskModel();
			
			jmodel.name = name;
			
			for( Proposition p : fbk.getProgram().getModel().propositions() ) {
				JProposition jp = new JProposition();
				jp.id = p.getId();
				jp.stereotype = p.getStereotype();
				jmodel.add( jp );
			}
			for( Relation r : fbk.getProgram().getModel().relations() ) {
				JRelation jr = new JRelation();
				jr.stereotype = r.getStereotype();
				Proposition target = r.getTarget();
				if( target == null ) continue;
				JProposition jtarget = jmodel.getProposition( target.getId() );
				if( jtarget == null ) continue;
				jr.setTarget( jtarget );
				for( Proposition source : r.getSources() ) {
					JProposition jsource = jmodel.getProposition( source.getId() );
					if( jsource == null ) continue;
					jr.addSource( jsource );
				}
				jmodel.add( jr );
			}
			
			return gson.toJson( jmodel );
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/{model}/description")
	public String getDescription(
			@PathParam("domain") @Info("The selected domain") 			String domain,
			@PathParam("model") @Info("The selected model")				String model,
			@HeaderParam("token") @Info("The authentication token") 	String token
			) throws Exception {
		RiscossDB db = null;
		try {
			db = DBConnector.openDB(domain, token);
			return db.getProperty( RiscossElements.MODEL, model, "description", "" );
		} catch (Exception e) {
			throw e;
		} finally {
			DBConnector.closeDB(db);
		}
	}
	
	@POST @Path("/{domain}/{model}/description")
	public void setDescription(
			@PathParam("domain") @Info("The selected domain") 			String domain,
			@PathParam("model") @Info("The selected model")				String model,
			@HeaderParam("token") @Info("The authentication token") 	String token,
			@Info("The description string to be set")					String description
			) throws Exception {
		RiscossDB db = null;
		try {
			db = DBConnector.openDB(domain, token);
			db.setProperty( RiscossElements.MODEL, model, "description", description );
		} catch (Exception e) {
			throw e;
		} finally {
			DBConnector.closeDB(db);
		}
	}
	
}