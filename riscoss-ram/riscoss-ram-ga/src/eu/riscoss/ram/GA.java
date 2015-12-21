package eu.riscoss.ram;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.uma.jmetal.algorithm.multiobjective.nsgaii.NSGAII;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.NonUniformMutation;
import org.uma.jmetal.operator.impl.selection.BinaryTournamentSelection;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;

import eu.riscoss.fbk.language.Program;
import eu.riscoss.ram.ga.InputOptimizationProblem;
import eu.riscoss.reasoner.Evidence;

public class GA {
	
	
	public Map<String,Double> run( Program program, Map<String,Evidence> objectives, Map<String,Double> fixedVariables ) {
		
		CrossoverOperator<DoubleSolution>		crossover					= new SBXCrossover( 0.1, 0.1 ); // BLXAlphaCrossover( 0.5 );
		MutationOperator<DoubleSolution>		mutation					= new NonUniformMutation( 0.2,  0.3, 10 ); // SimpleRandomMutation( 0.5 );
		SelectionOperator<List<DoubleSolution>, DoubleSolution> selection	= new BinaryTournamentSelection<DoubleSolution>();
		SolutionListEvaluator<DoubleSolution>	evaluator					= new SequentialSolutionListEvaluator<DoubleSolution>();
		
		
		InputOptimizationProblem  				problem = new InputOptimizationProblem( program );
		
		problem.setFixedVariables( fixedVariables );
		problem.setObjectives( objectives );
		
		NSGAII<DoubleSolution>					algorithm = new NSGAII<DoubleSolution>( problem, 10, 10, crossover, mutation, selection, evaluator );
		
		
		algorithm.run();
		
		List<DoubleSolution> result = algorithm.getResult();
		
		Map<String,Double> output = new HashMap<String,Double>();
		
		if( result.size() > 0 ) {
			
			System.out.println( problem.getVariableList().size() );
			
			for( String id : problem.getVariableList() ) {
				
				if( program.getScenario().getConstraint( id, "st" ) != null ) {
					output.put( id, 
							Double.parseDouble(
									program.getScenario().getConstraint( id, "st" ) ) );
				}
//				else {
//					output.put( id, Double.NaN );
//				}
			}
			
			
		}
		
		return output;
		
	}
	
	static double round( double value ) {
		return (double)Math.round(value * 10000) / 10000;
	}
	
}
