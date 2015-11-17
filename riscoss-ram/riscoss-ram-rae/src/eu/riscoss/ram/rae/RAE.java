package eu.riscoss.ram.rae;

import eu.riscoss.fbk.language.Proposition;
import eu.riscoss.fbk.language.Relation;
import eu.riscoss.reasoner.CompoundAnalysisEngine;
import eu.riscoss.reasoner.DataType;
import eu.riscoss.reasoner.FBKRiskAnalysisEngine;
import eu.riscoss.reasoner.RiskAnalysisEngine;

public class RAE {
	
	ArgumentationConf conf = new ArgumentationConf();
	
	public Argumentation createArgumentation( RiskAnalysisEngine engine ) {
		
		if( !(engine instanceof CompoundAnalysisEngine) ) return null;
		
		CompoundAnalysisEngine cae = (CompoundAnalysisEngine)engine;
		
		Argumentation argumentation = new Argumentation();
		
		double value = 0;
		int n = 0;
		
		for( RiskAnalysisEngine rae : cae.listEngines() ) {
			
			if( rae instanceof FBKRiskAnalysisEngine ) {
				
				Argumentation a = createArgumentation( (FBKRiskAnalysisEngine)rae );
				
				if( a == null ) continue;
				
				argumentation.add( a );
				
				value += a.getArgument().getTruth();
				n++;
			}
			
		}
		
		if( n > 0 ) {
			argumentation.getArgument().setTruth( value / n );
		}
		
		return argumentation;
		
	}
	
	public Argumentation createArgumentation( FBKRiskAnalysisEngine engine ) {
		
		Argumentation argumentation = new Argumentation();
		
		double value = 0;
		int n = 0;
		
		for( Proposition p : engine.getProgram().getModel().propositions() ) {
			
			if( !engine.isOutput( p ) ) continue;
			
			n++;
			
			Argument arg = createArgument( engine, p, 0 );
			
			if( arg == null ) continue;
			
			argumentation.getArgument().addArgument( arg );
			
			value += arg.getTruth();
			
		}
		
		if( n > 0 ) {
			argumentation.getArgument().setTruth( value / n );
		}
		
		return argumentation;
		
	}
	
	Argument createArgument( FBKRiskAnalysisEngine engine, Proposition p, int depth ) {
		
		Argument arg = new Argument( p.getId() );
		
		if( p.getProperty( "datatype", DataType.EVIDENCE.name() ) != null ) {
			DataType dt = DataType.valueOf( p.getProperty( "datatype", DataType.EVIDENCE.name() ).toUpperCase() );
			switch( dt ) {
			case EVIDENCE:
				arg.setTruth( Math.max( 
						engine.getEngine().getPositiveValue( p.getId() ), 
						arg.getTruth() ) );
				break;
			default:
				// Ignore Distributions, Integers and Reals
				break;
			}
		}
		
		if( p.getProperty( "arg-ignore", null ) != null ) return null;
		
		arg.setSummary( p.getProperty( "argument", "For an unspecified reason, the value of '" + p.getProperty( "label", p.getId() ) + " is related to this risk" ) );
		
		if( arg.getTruth() >= conf.getMin() ) {
			
			// browse tree
			for( Relation r : p.in() ) {
				
				if( conf.relationTypes.contains( r.getStereotype() ) ) {
//				if( "expose".equalsIgnoreCase( r.getStereotype() ) ) {
					
					for( Proposition sub_p : r.getSources() ) {
						
						Argument subArg = createArgument( engine, sub_p, depth +1 );
						
						if( subArg == null ) continue;
						
						arg.addArgument( subArg );
						
					}
					
				}
				
			}
			
			return arg;
		}
		
		return null;
	}
	
}
