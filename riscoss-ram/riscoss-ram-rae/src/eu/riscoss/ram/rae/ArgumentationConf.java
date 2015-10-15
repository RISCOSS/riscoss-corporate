package eu.riscoss.ram.rae;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ArgumentationConf {
	
	public enum Threshold {
		Extreme( 0.95 ), 
		High( 0.75 ), 
		Medium( 0.4 ), 
		Low( 0.15 ), 
		None( 0.0 );
		
		double value;
		
		Threshold( double value ) {
			this.value = value;
		}
		
	}
	
	Map<Threshold,Double> thresholds = new HashMap<Threshold, Double>();
	
	Set<String> relationTypes = new HashSet<String>();
	
	public ArgumentationConf() {
		thresholds.put( Threshold.Extreme, 0.95 );
		thresholds.put( Threshold.High, 0.75 );
		thresholds.put( Threshold.Medium, 0.4 );
		thresholds.put( Threshold.Low, 0.15 );
		thresholds.put( Threshold.None, 0.0 );
		relationTypes.add( "expose" );
		relationTypes.add( "satisfy" );
	}
	
	public double getThreshold( Threshold t ) {
		return t.value;
//		return thresholds.get( t );
	}
	
	public double getMin() {
		return getThreshold( Threshold.Low );
	}
	
	public Threshold getThreshold( double value ) {
		Threshold ret = Threshold.None;
		for( Threshold t : Threshold.values() ) {
			if( value > t.value )
				ret = t;
		}
		return ret;
	}
	
}
