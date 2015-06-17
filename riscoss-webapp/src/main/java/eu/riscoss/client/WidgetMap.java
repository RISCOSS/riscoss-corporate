package eu.riscoss.client;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class WidgetMap {
	
	Map<String,Object> map = new HashMap<String,Object>();
	
	public void put( String key, Object value ) {
		map.put( key, value );
	}
	
	public <T> T get( String key ) {
		return (T)map.get( key );
	}
	
}
