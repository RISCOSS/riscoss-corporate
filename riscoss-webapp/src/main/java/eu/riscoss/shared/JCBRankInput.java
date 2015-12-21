package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

public class JCBRankInput {
	
	int numberOfAlternatives = 0;
	int numberOfCycles = 1;
	float epsilon = 0.001F;
	
	List<List<Integer>> preferenceMatrix;
	
	List<List<Double>> functions;
	
	
	public void setNumberOfCycles( int n ) {
		this.numberOfCycles = n;
	}
	
	public void setNumberOfAlternatives( int n ) {
		this.numberOfAlternatives = n;
		this.preferenceMatrix = new ArrayList<List<Integer>>();
		for( int i = 0; i < n; i++ ) {
			List<Integer> row = new ArrayList<>();
			for( int j = 0; j < n; j++ ) {
				row.add( 0 );
			}
			preferenceMatrix.add( row );
		}
		functions = new ArrayList<List<Double>>();
		for( int i = 0; i < n; i++ ) {
			List<Double> row = new ArrayList<>();
			for( int j = 0; j < n; j++ ) {
				row.add( 0.0 );
			}
			functions.add( row );
		}
	}
	
	public void setPreferences( List<List<Integer>> matrix ) {
		preferenceMatrix = new ArrayList<>();
		for( List<Integer> row : matrix ) {
			List<Integer> r = new ArrayList<Integer>();
			for( Integer c : row ) {
				r.add( c );
			}
			preferenceMatrix.add( r );
		}
	}
	
	public int getNumberOfAlternatives() {
		return this.numberOfAlternatives;
	}
	
	public float getEpsilon() {
		return this.epsilon;
	}
	
	public int getPreference( int column, int row ) {
		return preferenceMatrix.get( column ).get( row );
	}
	
	public int getNumberOfCycles() {
		return this.numberOfCycles;
	}
	
}
