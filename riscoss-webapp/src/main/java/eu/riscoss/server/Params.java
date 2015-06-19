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

package eu.riscoss.server;

import java.net.URL;
import java.net.URLDecoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Params implements Iterable<String> {
	Map<String,String> map;
	
	public static Map<String, String> splitQuery( URL url ) {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		try
		{
			String query = url.getQuery();
			String[] pairs = query.split("&");
			for (String pair : pairs) {
				int idx = pair.indexOf("=");
				query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
			}
		}
		catch( Exception ex )
		{
			//			ex.printStackTrace();
		}
		return query_pairs;
	}
	
	public static Map<String, String> splitQuery( String query ) {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		try
		{
			String[] pairs = query.split("&");
			for (String pair : pairs) {
				int idx = pair.indexOf("=");
				query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
			}
		}
		catch( Exception ex )
		{
			//			ex.printStackTrace();
		}
		return query_pairs;
	}
	
	public Params( URL url ) {
		map = splitQuery( url );
	}

	public Params( String input ) {
		map = splitQuery( input );
	}

	public String get( String key ) {
		return map.get( key );
	}

	public String get( String key, String def ) {
		String ret = get( key );
		if( ret == null ) ret = def;
		return ret;
	}

	public Iterator<String> iterator() {
		return map.keySet().iterator();
	}
	
	public void set( String param, String value ) {
		map.put( param, value );
	}
	
	public String toString() {
		String sep = "";
		StringBuilder str = new StringBuilder();
		for( String key : map.keySet() ) {
			str.append( sep + key + "=" + map.get( key ) );
			sep = "&";
		}
		return str.toString();
	}
}
