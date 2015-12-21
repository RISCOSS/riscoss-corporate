package eu.riscoss.shared;

import java.util.HashMap;
import java.util.Map;

public class JCBRankResult {
	
	public Map<String,Double> values = new HashMap<String,Double>();
	

	public JCBRankResult() {}

	public void add( String id, double value ) {
		values.put( id, value );
	}
	
}
