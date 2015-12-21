package eu.riscoss.ram.ga;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
	
	ArrayList<Proposition>	inputs = new ArrayList<>();
	Map<String,Double>		constraints = new HashMap<>();
	
	Map<String, Evidence>	outputs = new HashMap<>();
	
	
	public InputOptimizationProblem( Program program, Map<String, Evidence> objectives ) {
		
		this( program );
		
		setObjectives( objectives );
		
	}
	
	public InputOptimizationProblem( Program program ) {
		// Filters the input nodes and stores them in the `inputs` list
		inputs = new ArrayList<Proposition>(program.getModel().getPropositionCount());
		for (Proposition p : program.getModel().propositions())
			if (p.getProperty("input", "false").equalsIgnoreCase("true"))
				inputs.add(p);
		
		this.program = program;
		
		super.setNumberOfObjectives( 1 );
		setFixedVariables( new HashMap<String,Double>() );
//		super.setNumberOfVariables( inputs.size() );
		
	}
	
	public void setFixedVariables( Map<String,Double> idMap ) {
		if( this.program == null ) {
			throw new RuntimeException( "Program not set" );
		}
		inputs = new ArrayList<Proposition>();
		constraints.clear();
		for( Proposition p : program.getModel().propositions() ) {
			if( idMap.get( p.getId() ) != null ) {
				this.constraints.put( p.getId(), idMap.get( p.getId() ) );
			}
			else {
				if( p.getProperty("input", "false").equalsIgnoreCase("true") ) {
					inputs.add( p );
				}
			}
		}
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
	}
	
	public Map<String,Double> getFixedVariables() {
		return this.constraints;
	}
	
	public void setObjectives( Map<String, Evidence> objectives ) {
		this.outputs = objectives;
	}
	
	public List<String> getVariableList() {
		List<String> list = new ArrayList<>();
		for( Proposition p : inputs ) {
			list.add( p.getId() );
		}
		for( String id : constraints.keySet() ) {
			list.add( id );
		}
		return list;
	}
	

	public void evaluate( DoubleSolution solution ) {
		
		analysis = new RiskEvaluation();
		
		program.getScenario().clear();
		
		System.out.print( solution.getNumberOfVariables() + " " );
		
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
		
		for( String id : constraints.keySet() ) {
			
			Proposition p = program.getModel().getProposition( id );
			DataType dt = DataType.valueOf( p.getProperty( "datatype", "evidence" ).toUpperCase() );
			
			System.out.print( "Const( " + p.getId() + ":" + constraints.get( id ) + ") " );
			switch( dt ) {
			case REAL:
				program.getScenario().addConstraint( p.getId(), "st", constraints.get( id ) + "");
				break;
			case INTEGER: {
				int max = Integer.parseInt( p.getProperty( "max", "100" ) ) +1;
				int min = Integer.parseInt( p.getProperty( "min", "0" ) );
				double d = constraints.get( id );
				int val = ((int)(d * (max - min) ) + min);
				if( val > max ) val = max;
				if( val < min ) val = min;
				program.getScenario().addConstraint( p.getId(), "st", val + "" );
			}
				break;
			case EVIDENCE: {
				program.getScenario().addConstraint( p.getId(), "st", constraints.get( id ) + "");
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
