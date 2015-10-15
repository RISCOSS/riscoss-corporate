package eu.riscoss.ram;

import java.util.Iterator;
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

public class GA {
	
	public void run( Program program, Map<String,Object> map ) {
		
		CrossoverOperator<DoubleSolution>		crossover = new SBXCrossover( 0.1, 0.1 ); // BLXAlphaCrossover( 0.5 );
		MutationOperator<DoubleSolution>		mutation = new NonUniformMutation( 0.2,  0.3, 10 ); // SimpleRandomMutation( 0.5 );
		SelectionOperator<List<DoubleSolution>, DoubleSolution> selection = new BinaryTournamentSelection<DoubleSolution>();
		SolutionListEvaluator<DoubleSolution>	evaluator = new SequentialSolutionListEvaluator<DoubleSolution>();
		
		
		InputOptimizationProblem  				problem = new InputOptimizationProblem( program, map );
		
		NSGAII<DoubleSolution>					algorithm = new NSGAII<DoubleSolution>( problem, 10, 10, crossover, mutation, selection, evaluator );
		
		
		algorithm.run();
		
		List<DoubleSolution> result = algorithm.getResult();
		
		for( Iterator<DoubleSolution> it = result.iterator(); it.hasNext(); ) {
			DoubleSolution s = it.next();
			System.out.print( "Variables: " );
			for( int i = 0; i < s.getNumberOfVariables(); i++ ) {
				System.out.print( "\t" + round( s.getVariableValue(i).doubleValue() ) );
			}
			System.out.print( "\t" + "Objectives: " );
			for( int i = 0; i < s.getNumberOfObjectives(); i++ ) {
				System.out.print( "\t" + round( s.getObjective( i ) ) );
			}
			System.out.println();
		}
		System.out.println();
	}
	
	static double round( double value ) {
		return (double)Math.round(value * 10000) / 10000;
	}
	
}
