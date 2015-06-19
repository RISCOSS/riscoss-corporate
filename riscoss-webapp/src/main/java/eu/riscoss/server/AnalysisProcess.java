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
import java.util.Date;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;
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
import eu.riscoss.server.AnalysisManager.MissingDataItem;
import eu.riscoss.shared.AnalysisOption;
import eu.riscoss.shared.AnalysisResult;

public class AnalysisProcess {
	
	// TODO: remove any reference to the DB; the session must be self-contained
	RiscossDB db;
	
	Date date = new Date();
	
	public AnalysisProcess( RiscossDB db ) {
		this.db = db;
	}
	
	public void start( RiskAnalysisSession session ) {
		
		date = new Date();
		
		analyseEntity( session.getTarget(), session );
		
	}
	
	private void analyseEntity( String target, RiskAnalysisSession session ) {
		
		// TODO restore
		//		// First, analyse all sub-configurations
		//		// The result is stored in context.results
		//		for( Configuration rc : session.getRiskConfiguration().subConfigurations() ) {
		//			analyseEntity( target, rc, session );
		//		}
		
		// Then, analyse top-level configuration
		analyseEntity( target, session.getRCName(), session );
		
	}
	
	private void analyseEntity( String entity, String rc, RiskAnalysisSession session ) {
		
		for( String child : session.getChildren( entity ) ) {
			analyseEntity( child, rc, session );
		}
		
		analyseEntity( entity, rc, session.getLayer( entity ), session );
		
	}
	
	private void analyseEntity( String target, String rc, String layer, RiskAnalysisSession session ) {
		
		TimeDiff.get().log( "Analysing entity " + target );
		
		RiskAnalysisEngine rae = ReasoningLibrary.get().createRiskAnalysisEngine();
		
		// Load all models at given layer and run risk analysis
		for( String rc_name : session.getModels( layer ) ) {
			String blob = db.getModelBlob( rc_name );
			rae.loadModel( blob );
		}
		
		List<MissingDataItem> missingFields = new ArrayList<>();
		
		TimeDiff.get().log( "Models loaded" );
		
		for( Chunk c : rae.queryModel( ModelSlice.INPUT_DATA ) ) {
			
//			TimeDiff.get().log( "Setting indicator " + c.getId() );
			
			Field f = rae.getField( c, FieldType.INPUT_VALUE );
			
//			Map<String,Object> map = session.getResult( layer, target, c.getId() );
			List<String> childValues = new ArrayList<String>();
			{
				String str = session.getInput( target, c.getId() );
				if( str == null ) {
					
					for( String child : session.getChildren( target ) ) {
						String childLayer = session.getLayer( child );
						str = session.getResult( childLayer, child, c.getId(), "rd", null );
						if( str != null ) {
							childValues.add( str );
						}
						
					}
				}
				else {
					childValues.add( str );
				}
				if( str == null ) {
					// Return?
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
			}
			
			for( String str : childValues ) {
				
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
				
			}
		}
		
//		TimeDiff.get().log( "Inputs set" );
		
		if( missingFields.size() > 0 ) {
			if( AnalysisOption.valueOf( session.getOption( "AnalysisOption", AnalysisOption.RunThrough.name() ) ) == AnalysisOption.RequestMissingData ) {
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
				session.setOption( "status", AnalysisResult.DataMissing.name() );
				session.setOption( "missing-data", ret.toString() );
				return;
			}
		}
		
		try {
			rae.runAnalysis( new String[] {} );
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		
		TimeDiff.get().log( "Analysis executed" );
		
		for( Chunk c : rae.queryModel( ModelSlice.OUTPUT_DATA ) ) {
			Field f = rae.getField( c, FieldType.OUTPUT_VALUE );
			session.setResult( layer, target, c.getId(), "datatype", f.getDataType().name() );
			switch( f.getDataType() ) {
			case EVIDENCE: {
				Evidence e = f.getValue();
				session.setResult( layer, target, c.getId(), "p", "" + e.getPositive() );
				session.setResult( layer, target, c.getId(), "m", "" + e.getNegative() );
				session.setResult( layer, target, c.getId(), "e", "" + e.getDirection() );
				session.setResult( layer, target, c.getId(), "description", "" + rae.getField( c, FieldType.DESCRIPTION ).getValue() );
				session.setResult( layer, target, c.getId(), "label", "" + rae.getField( c, FieldType.LABEL ).getValue() );
				RiskData rd = new RiskData( c.getId(), target, date, RiskDataType.EVIDENCE, e.pack() );
				session.setResult( layer, target, c.getId(), "rd", rd.toJSON() );
			}
			break;
			case DISTRIBUTION: {
				Distribution d = f.getValue();
				session.setResult( layer, target, c.getId(), "value", "" + d.pack() );
				RiskData rd = new RiskData( c.getId(), target, date, RiskDataType.DISTRIBUTION, d.pack() );
				session.setResult( layer, target, c.getId(), "rd", rd.toJSON() );
			}
			break;
			case INTEGER:
			case REAL: {
				session.setResult( layer, target, c.getId(), "value", "" + f.getValue().toString() );
				RiskData rd = new RiskData( c.getId(), target, date, RiskDataType.NUMBER, "" + f.getValue().toString() );
				session.setResult( layer, target, c.getId(), "rd", rd.toJSON() );
			}
			break;
			case STRING:
				session.setResult( layer, target, c.getId(), "value", "" + f.getValue().toString() );
				break;
			case NaN:
			default:
				break;
			}
		}
		
		TimeDiff.get().log( "Output saved" );
		
		session.setStatus( target, AnalysisResult.Done.name() );
	}
	
	RiskData field2RiskData( String id, String target, Field f ) {
		RiskData rd = null;
		switch( f.getDataType() ) {
		case EVIDENCE: {
			Evidence e = f.getValue();
			rd = new RiskData( id, target, date, RiskDataType.EVIDENCE, e.pack() );
		}
		break;
		case DISTRIBUTION: {
			Distribution d = f.getValue();
			rd = new RiskData( id, target, date, RiskDataType.DISTRIBUTION, d.pack() );
		}
		break;
		case INTEGER:
		case REAL: {
			rd = new RiskData( id, target, date, RiskDataType.NUMBER, "" + f.getValue().toString() );
		}
		break;
		case STRING:
			break;
		case NaN:
		default:
			break;
		}
		return rd;
	}
	
	String getResultAsInput( RiskAnalysisSession session, String layer, String entity, String iid ) {
		String value = null;
		DataType dt = DataType.valueOf( session.getResult( layer, entity, iid, "datatype", DataType.REAL.name() ) );
		switch( dt ) {
		case EVIDENCE: {
			String p = session.getResult( layer, entity, iid, "p", "0" );
			String m = session.getResult( layer, entity, iid, "m", "0" );
			Evidence e = new Evidence( 
					Double.parseDouble( p ), Double.parseDouble( m ) );
			value = e.pack();
		}
		break;
		case DISTRIBUTION: 
		case INTEGER:
		case REAL:
			value = session.getResult( layer, entity, iid, "value", null );
			break;
		case STRING:
		case NaN:
		default:
			break;
		}
		if( value == null ) return null;
		JsonObject o = new JsonObject();
		o.addProperty( "value", value );
		return o.toString();
	}
}
