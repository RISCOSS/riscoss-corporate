package eu.riscoss.server.ma;

import java.util.List;
import java.util.Map;

import com.google.gson.Gson;

import eu.riscoss.db.RiskScenario;
import eu.riscoss.fbk.io.XmlWriter;
import eu.riscoss.fbk.language.Proposition;
import eu.riscoss.fbk.language.Relation;
import eu.riscoss.ram.CBRankAnalyser;
import eu.riscoss.ram.MitigationActivity;
import eu.riscoss.reasoner.FBKRiskAnalysisEngine;
import eu.riscoss.shared.JCBRankInput;
import eu.riscoss.shared.JCBRankResult;

public class CBRankAnalysis extends MitigationActivity {
	
	//	public enum PARAMETER_NAME {
	//		NUMBER_OF_ALTERNATIVES,
	//		NUMBER_OF_CYCLES,
	//		EPSILON
	//	}
	//	
	//	public Enum<?>[] getParameterNames() {
	//		return PARAMETER_NAME.values();
	//	}
	//	
	//	public void setParameter( String par, String value ) {
	//		PARAMETER_NAME par_name = null;
	//		try {
	//			par_name = PARAMETER_NAME.valueOf( par );
	//		}
	//		catch( Exception ex ) {
	//			ex.printStackTrace();
	//			return;
	//		}
	//		try {
	//			switch( par_name ) {
	//			case NUMBER_OF_ALTERNATIVES:
	//				analyser.setNumberOfAlternatives( Integer.parseInt( value ) );
	//				break;
	//			case NUMBER_OF_CYCLES:
	//				analyser.setNumberOfCycles( Integer.parseInt( value ) );
	//				break;
	//			case EPSILON:
	//				analyser.setEpsilon( (float)Double.parseDouble( value ) );
	//				break;
	//			}
	//		}
	//		catch( Exception ex ) {
	//			ex.printStackTrace();
	//		}
	//	}
	
	CBRankAnalyser analyser = new CBRankAnalyser();
	
	@Override
	public String eval( String json , RiskScenario context  ) {
		
		JCBRankInput ahpInput = new Gson().fromJson( json, JCBRankInput.class );
		
		JCBRankResult result = run( ahpInput );
		
		return new Gson().toJson( result );
		
	}
	
	private JCBRankResult run( JCBRankInput input ) {
		
		analyser.setEpsilon( input.getEpsilon() );
		analyser.setNumberOfAlternatives( input.getNumberOfAlternatives() );
		analyser.setNumberOfCycles( input.getNumberOfCycles() );
		
		for( int c = 0; c < input.getNumberOfAlternatives(); c++ ) {
			for( int r = 0; r < input.getNumberOfAlternatives(); r++ ) {
				
				analyser.setPreference( c, r, input.getPreference( c, r ) );
				
			}
		}
		
		Map<String,Double> map = analyser.run();
		
		JCBRankResult result = new JCBRankResult();
		
		result.values = map;
		
		return result;
	}
	
	@Override
	public void apply( String string, RiskScenario scenario ) {
		
		JCBRankResult result = new Gson().fromJson( string, JCBRankResult.class );
		
		float max = 0;
		for( Double d : result.values.values() ) {
			if( d > max ) max = d.floatValue();
		}
		if( max == 0 ) max = 1;
		
		String target = scenario.getTarget();
		
		String layer = scenario.getLayer( target );
		
		List<String> models = scenario.getModels( layer );
		
		for( String model : models ) {
			String blob = scenario.getStoredModelBlob( model );
			try {
				FBKRiskAnalysisEngine e = new FBKRiskAnalysisEngine();
				
				e.loadModel( blob );
				
				for( String id : result.values.keySet() ) {
					
					Proposition p = e.getProgram().getModel().getProposition( id );
					
					if( p == null ) continue;
					
					for( Relation r : p.in() ) {
						if( !"increase".equals( r.getStereotype() ) ) continue;
						float w = r.getWeight();
						w = w / max;
						r.setWeight( w );
					}
					
				}
				
				String newBlob = new XmlWriter().generateXml( e.getProgram() ).asString();
				
				scenario.storeModelBlob( model, layer, newBlob );
				
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
			catch( Error err ) {
				err.printStackTrace();
			}
		}
		
	}
	
}
