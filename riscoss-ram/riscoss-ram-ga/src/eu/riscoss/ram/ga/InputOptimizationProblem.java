package eu.riscoss.ram.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.uma.jmetal.problem.impl.AbstractDoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;

import eu.riscoss.fbk.language.Program;
import eu.riscoss.fbk.language.Proposition;
import eu.riscoss.reasoner.DataType;
import eu.riscoss.reasoner.Evidence;
import eu.riscoss.reasoner.RiskEvaluation;

public class InputOptimizationProblem extends AbstractDoubleProblem {
	
	private static final long serialVersionUID = 1L;
	
	Program					program;
	
	RiskEvaluation			analysis = null;
	
	ArrayList<Proposition>  inputs;
	Map<String, Object>     outputs;
	
	
	public InputOptimizationProblem( Program program, Map<String, Object> outputs ) {
		
		// Filters the input nodes and stores them in the `inputs` list
		inputs = new ArrayList<Proposition>(program.getModel().getPropositionCount());
		for (Proposition p : program.getModel().propositions())
			if (p.getProperty("input", "false").equalsIgnoreCase("true"))
				inputs.add(p);
		
		super.setNumberOfObjectives( 1 );
		super.setNumberOfVariables( inputs.size() );
		
		{
			Double[] lowers = new Double[inputs.size()];
			Arrays.fill( lowers, 0.0 );
			super.setLowerLimit( Arrays.asList( lowers ) );
		}
		{
			Double[] uppers = new Double[inputs.size()];
			Arrays.fill( uppers, 1.0 );
			super.setUpperLimit( Arrays.asList( uppers ) );
		}
		
		this.program = program;
		this.outputs = outputs;
		
	}
	

	public void evaluate( DoubleSolution solution ) {
		
		analysis = new RiskEvaluation();
		
		program.getScenario().clear();
		
		for( int var = 0; var < solution.getNumberOfVariables(); var++ ) {
			
			Proposition p = inputs.get( var );
			DataType dt = DataType.valueOf( p.getProperty( "datatype", "evidence" ).toUpperCase() );
			
			System.out.print( p.getId() + ":" + solution.getVariableValue( var ) + " " );
			switch( dt ) {
			case REAL:
				program.getScenario().addConstraint(inputs.get(var).getId(), "st", solution.getVariableValue( var ) + "");
				break;
			case INTEGER: {
				int max = Integer.parseInt( p.getProperty( "max", "100" ) ) +1;
				int min = Integer.parseInt( p.getProperty( "min", "0" ) );
				double d = solution.getVariableValue( var );
				int val = ((int)(d * (max - min) ) + min);
				if( val > max ) val = max;
				if( val < min ) val = min;
				program.getScenario().addConstraint( inputs.get(var).getId(), "st", val + "" );
			}
				break;
			case EVIDENCE: {
				program.getScenario().addConstraint(inputs.get(var).getId(), "st", solution.getVariableValue( var ) + "");
			}
				break;
			default:
				break;
			}
		}
		
		System.out.println();
		
		program.getScenario().addConstraint("always", "st", "1" );
		
		analysis.run( program );
		
//		for( Proposition p : program.getModel().propositions() ) {
//			System.out.println( p.getId() + " -> " + analysis.getPositiveValue( p.getId() ) + ", " + analysis.getNegativeValue( p.getId() ) );
//		}
		
		double psum = 0.0;
		double msum = 0.0;
		
//		try {
//			Proposition p = program.getModel().getProposition( "Future integration risk" );
//			Chunk c = analysis.kb.index().getChunk(p.getId());
//			Node node = c.getPredicate( "threat" );
//			System.out.println( new Evidence( node.getSatLabel().getValue(), node.getDenLabel().getValue() ) );
//		}
//		catch( Exception ex ) {
//			ex.printStackTrace();
//		}
		
		// Better inputs produces outputs nearer to the expected
		for( Proposition p : program.getModel().propositions() ) {
			Evidence o = (Evidence)outputs.get( p.getId() );
			if( o == null ) continue;
			psum += StrictMath.pow( o.getPositive() - analysis.getPositiveValue( p.getId() ), 2.0);
			msum += StrictMath.pow( o.getNegative() - analysis.getNegativeValue( p.getId() ), 2.0);
//			System.out.print( "psum = " + psum + ", nsum = " + msum + " " );
//			System.out.print(p.getId() + " [" + analysis.getPositiveValue( p.getId() ) + ", " + analysis.getNegativeValue( p.getId() ) + "] ");
//			System.out.println();
		}
		
		System.out.println(" - Obj: psum =" + psum + ", msum = " + msum);
		solution.setObjective( 0, psum + msum );
	}
}
