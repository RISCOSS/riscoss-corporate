/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/**
 * @author 	Alberto Siena
**/

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
