package eu.riscoss.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;

public class JsonDistribution {
	
	List<Double> values = new ArrayList<Double>();
	
	public JsonDistribution( JSONObject o ) {
		JSONArray array = o.get( "value" ).isArray();
		if( array == null ) return;
		for( int i = 0; i < array.size(); i++ ) {
			values.add( Double.parseDouble( array.get( i ).isString().stringValue() ) );
		}
	}
	
	public List<Double> getValues() {
		return values;
	}
	
	public String toString() {
		String ret = "";
		String sep = "";
		for( double val : values ) {
			ret += sep + val;
			sep = ";";
		}
		return ret;
	}
	
	private double diff() {
		double diff = 1;
		for( Double val : getValues() ) {
			diff -= val;
		}
		return diff;
	}
	
	public void flatten( int fixedBar ) {
		List<Double> v = getValues();
		double diff = diff();
		for( int i = 0; i < v.size(); i++ ) {
			if( diff == 0 ) break;
			if( i == fixedBar ) continue;
			double n = v.get( i ) + diff;
			if( n < 0 ) n = 0;
			if( n > 1 ) n = 1;
			v.set( i, n );
			diff = diff();
		}
	}
}
