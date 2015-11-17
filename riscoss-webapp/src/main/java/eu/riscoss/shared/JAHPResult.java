package eu.riscoss.shared;

import java.util.HashMap;
import java.util.Map;

public class JAHPResult {
	
	public Map<String,Double> values = new HashMap<String,Double>();
	

	public JAHPResult() {
		// TODO Auto-generated constructor stub
	}

	public void add( String id, double value ) {
		values.put( id, value );
	}
	
}
