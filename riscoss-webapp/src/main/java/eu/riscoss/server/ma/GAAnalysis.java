package eu.riscoss.server.ma;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import eu.riscoss.dataproviders.RiskData;
import eu.riscoss.dataproviders.RiskDataType;
import eu.riscoss.db.RiskScenario;
import eu.riscoss.ram.GA;
import eu.riscoss.ram.MitigationActivity;
import eu.riscoss.reasoner.Chunk;
import eu.riscoss.reasoner.Evidence;
import eu.riscoss.reasoner.FBKRiskAnalysisEngine;
import eu.riscoss.reasoner.Field;
import eu.riscoss.reasoner.FieldType;
import eu.riscoss.reasoner.ModelSlice;
import eu.riscoss.reasoner.ReasoningLibrary;
import eu.riscoss.reasoner.RiskAnalysisEngine;
import eu.riscoss.shared.JGAInput;
import eu.riscoss.shared.JGAOutput;

public class GAAnalysis extends MitigationActivity {

	@Override
	public String eval( String json , RiskScenario context  ) {
		
		JGAInput input = new Gson().fromJson( json, JGAInput.class );
		
		GA ga = new GA();
		
		JGAOutput output = new JGAOutput();
		
		Map<String,Evidence> objectives = new HashMap<>();
		for( String key : input.getObjectives().keySet() ) {
			objectives.put( key, Evidence.unpack( input.getObjectives().get( key ) ) );
		}
		
		Map<String, Double> data = new HashMap<>();
		
		FBKRiskAnalysisEngine rae = new FBKRiskAnalysisEngine();
		for( String model : context.getModels( context.getLayer( context.getTarget() ) ) ) {
			String blob = context.getStoredModelBlob( model );
			rae.loadModel( blob );
		}
		
		for( Chunk c : rae.queryModel( ModelSlice.INPUT_DATA ) ) {
			if( !input.getEnabledIndicators().contains( c.getId() ) ) {
				Field f = rae.getField( c, FieldType.INPUT_VALUE );
				switch( f.getDataType() ) {
				case DISTRIBUTION:
					break;
				case EVIDENCE:
					break;
				case INTEGER:
					data.put( c.getId(), (double)(int)f.getValue() );
					break;
				case NaN:
					break;
				case REAL:
					data.put( c.getId(), (double)f.getValue() );
					break;
				case STRING:
					break;
				default:
					break;
				}
			}
		}
		
		output.map = ga.run( rae.getProgram(), objectives, data );
		
		String string = new Gson().toJson( output );
		
		return string;
	}

	@Override
	public void apply( String json, RiskScenario scenario ) {
		
		JGAOutput output = new Gson().fromJson( json, JGAOutput.class );
		
		FBKRiskAnalysisEngine rae = new FBKRiskAnalysisEngine();
		for( String model : scenario.getModels( scenario.getLayer( scenario.getTarget() ) ) ) {
			String blob = scenario.getStoredModelBlob( model );
			rae.loadModel( blob );
		}
		
		Gson gson = new Gson();
		
		for( String id : output.map.keySet() ) {
			
			for( Chunk c : rae.queryModel( ModelSlice.INPUT_DATA ) ) {
				Field f = rae.getField( c, FieldType.INPUT_VALUE );
				switch( f.getDataType() ) {
				case DISTRIBUTION:
					break;
				case EVIDENCE:
					break;
				case INTEGER:
					scenario.saveInput( scenario.getTarget(), id, "user", gson.toJson( 
							new RiskData(
									id, scenario.getTarget(), new Date(), RiskDataType.NUMBER, output.map.get( id ) ) ) );
					break;
				case NaN:
					break;
				case REAL:
					scenario.saveInput( scenario.getTarget(), id, "user", gson.toJson( 
							new RiskData(
									id, scenario.getTarget(), new Date(), RiskDataType.NUMBER, output.map.get( id ) ) ) );
					break;
				case STRING:
					break;
				default:
					break;
				}
			}
			
		}
		
	}
	
}
