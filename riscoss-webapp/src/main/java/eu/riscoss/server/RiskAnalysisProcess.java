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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;
import eu.riscoss.db.RiskAnalysisSession;
import eu.riscoss.ram.rae.Argumentation;
import eu.riscoss.ram.rae.RAE;
import eu.riscoss.reasoner.Chunk;
import eu.riscoss.reasoner.DataType;
import eu.riscoss.reasoner.Distribution;
import eu.riscoss.reasoner.Evidence;
import eu.riscoss.reasoner.Field;
import eu.riscoss.reasoner.FieldType;
import eu.riscoss.reasoner.ModelSlice;
import eu.riscoss.reasoner.Rank;
import eu.riscoss.reasoner.ReasoningLibrary;
import eu.riscoss.reasoner.RiskAnalysisEngine;
import eu.riscoss.server.AnalysisManager.MissingDataItem;
import eu.riscoss.shared.EAnalysisOption;
import eu.riscoss.shared.EAnalysisResult;
import eu.riscoss.shared.EChunkDataType;
import eu.riscoss.shared.EDataOrigin;
import eu.riscoss.shared.JDataItem;
import eu.riscoss.shared.JMissingData;

public class RiskAnalysisProcess {
	
	Date date = new Date();
	private RiskAnalysisSession ras;
	
	Gson gson = new Gson();
	
	public RiskAnalysisProcess() {
	}
	
	public RiskAnalysisProcess( RiskAnalysisSession ras ) {
		this.ras = ras;
	}
	
	public void start( RiskAnalysisSession session ) {
		
		date = new Date();
		this.ras = session;
		
		analyseEntity( session.getTarget() );
		
	}
	
	public void runAnalysis() {
		
		if( this.ras == null ) {
			throw new RuntimeException( "RiskAnalysisSession not initialized" );
		}
		
		date = new Date();
		
		analyseEntity( ras.getTarget() );
		
	}
	
	private void analyseEntity( String target ) {
		
		analyseEntity( target, ras.getRCName(), ras );
		
	}
	
	private void analyseEntity( String entity, String rc, RiskAnalysisSession session ) {
		
		for( String child : session.getChildren( entity ) ) {
			analyseEntity( child, rc, session );
		}
		
		analyseEntity( entity, rc, session.getLayer( entity ), session );
		
	}
	
	private void analyseEntity( String target, String rc, String layer, RiskAnalysisSession session ) {
		
		RiskAnalysisEngine rae = ReasoningLibrary.get().createRiskAnalysisEngine();
		
		// Load all models at given layer and run risk analysis
		for( String rc_name : session.getModels( layer ) ) {
			String blob = session.getStoredModelBlob( rc_name );
			rae.loadModel( blob );
		}
		
		List<MissingDataItem> missingFields = new ArrayList<>();
		
		// Setting indicators
		
		for( Chunk c : rae.queryModel( ModelSlice.INPUT_DATA ) ) {
			
			Field f = rae.getField( c, FieldType.INPUT_VALUE );
			
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
		
		if( missingFields.size() > 0 ) {
			if( EAnalysisOption.valueOf( session.getOption( "AnalysisOption", EAnalysisOption.RunThrough.name() ) ) == EAnalysisOption.RequestMissingData ) {
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
				session.setOption( "status", EAnalysisResult.DataMissing.name() );
				session.setOption( "missing-data", ret.toString() );
				return;
			}
		}
		
		// Executing analysis
		
		try {
			rae.runAnalysis( new String[] {} );
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		
		// Saving output
		
		for( Chunk c : rae.queryModel( ModelSlice.OUTPUT_DATA ) ) {
			Field f = rae.getField( c, FieldType.OUTPUT_VALUE );
			session.setResult( layer, target, c.getId(), "datatype", f.getDataType().name() );
			session.setResult( layer, target, c.getId(), "type", rae.getField( c,  FieldType.TYPE ).getValue( "" ) );
			session.setResult( layer, target, c.getId(), "rank", "" + new Rank( c, f ).getRank() );
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
		
		RAE ram = new RAE();
		Argumentation a = ram.createArgumentation( rae );
		if( a != null ) {
			String json = gson.toJson( a );
			ras.setEntityAttribute( target, "argumentation", json );
		}
		
		session.setStatus( target, EAnalysisResult.Done.name() );
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
	
	public JMissingData gatherMissingData( String entity ) {
		JMissingData md = fillMissingDataStructureNew( entity );
		if( md == null ) {
			md = new JMissingData( entity, "" );
		}
		return md;
	}
	
	private JMissingData fillMissingDataStructureNew( String entity ) {
		return fillMissingDataStructureNew( entity, false, true, new String[] { "user" } );
	}
	
	public JMissingData fillMissingDataStructureNew( String entity, boolean includeDefault, boolean includeRDR, String ... origins ) {
		Set<String> set = new HashSet<>();
		if( origins != null )
			for( String o : origins )
				set.add( o );
		return fillMissingDataStructureNew(entity, includeDefault, includeRDR, set );
	}
	
	private JMissingData fillMissingDataStructureNew( String entity, boolean includeDefault, boolean includeRDR, Set<String> origins ) {
		
		JMissingData md = new JMissingData( entity, ras.getLayer( entity ) );
		
		for( String child : ras.getChildren( entity ) ) {
			JMissingData childMD = fillMissingDataStructureNew( child, includeDefault, includeRDR, origins );
			if( childMD != null ) {
				md.add( childMD );
			}
		}
		
		String layer = ras.getLayer( entity );
		RiskAnalysisEngine rae = ReasoningLibrary.get().createRiskAnalysisEngine();
		for( String model : ras.getModels( layer ) ) {
			String blob = ras.getStoredModelBlob( model );
			if( blob != null ) {
				rae.loadModel( blob );
			}
		}
		
		for( Chunk c : rae.queryModel( ModelSlice.INPUT_DATA ) ) {
			
			if( rae.getDefaultValue( c ) != null ) {
				if( !includeDefault )
					continue;
				Field f = rae.getDefaultValue( c );
				
				JDataItem item = new JDataItem();
				item.setDescription( (String)rae.getField( c, FieldType.DESCRIPTION ).getValue() );
				item.setLabel( (String)rae.getField( c, FieldType.LABEL ).getValue() );
				item.setId( c.getId() );
				item.setOrigin( EDataOrigin.RDR );
				item.setValue( "" );
				item.setType( EChunkDataType.valueOf( f.getDataType().name() ) );
				md.add( item );
				
			}
			
			Field f = rae.getField( c, FieldType.INPUT_VALUE );
			
			String raw = ras.getInput( entity, c.getId() );
			if( raw == null ) {
				JDataItem item = new JDataItem();
				item.setDescription( (String)rae.getField( c, FieldType.DESCRIPTION ).getValue() );
				item.setLabel( (String)rae.getField( c, FieldType.LABEL ).getValue() );
				item.setId( c.getId() );
				item.setOrigin( EDataOrigin.RDR );
				item.setValue( "" );
				item.setType( EChunkDataType.valueOf( f.getDataType().name() ) );
				md.add( item );
			}
			else {
				JsonObject json = (JsonObject)new JsonParser().parse( raw );
				if( json.get( "origin" ) != null ) {
					String origin = json.get( "origin" ).getAsString();
					if( !(includeRDR | origins.contains( origin ) ) ) {
						continue;
					}
				}
				JDataItem item = new JDataItem();
				item.setDescription( (String)rae.getField( c, FieldType.DESCRIPTION ).getValue() );
				item.setLabel( (String)rae.getField( c, FieldType.LABEL ).getValue() );
				item.setId( c.getId() );
				item.setOrigin( EDataOrigin.User );
				item.setValue( json.get("value").getAsString() );
				item.setType( EChunkDataType.valueOf( f.getDataType().name() ) );
				md.add( item );
			}
			
		}
		
		return md;
		
	}

}
