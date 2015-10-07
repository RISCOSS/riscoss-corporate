package eu.riscoss.agent;

import java.util.HashMap;
import java.util.Map;

public class ExecutionContext {
	
	private static final ExecutionContext instance = new ExecutionContext();
	
	public static ExecutionContext get() {
		return instance;
	}
	
	Map<String,Object> map = new HashMap<>();
	
	public void set( String key, Object value ) {
		map.put( key, value );
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get( String key, T def ) {
		try {
			return (T)map.get( key );
		}
		catch( ClassCastException ex ) {
			return def;
		}
	}
	
}
