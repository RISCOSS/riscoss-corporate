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
