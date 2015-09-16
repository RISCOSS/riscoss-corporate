package eu.riscoss.db;

import java.util.HashMap;
import java.util.Map;

public class GDomConfig {
	
	public static final String ROOT_CLASS = "Space";
	
	private static GDomConfig global = new GDomConfig();
	
	public static GDomConfig global() {
		return global;
	}
	
	public static void setGlobalConf( GDomConfig conf ) {
		global = conf;
	}
	
	Map<String,String> mapping = new HashMap<String,String>();
	
	public String getClass( String tag ) {
		String cls = mapping.get( tag );
		if( cls == null ) cls = "";
		return cls;
	}

	public void setMapping( String tag, String classname ) {
		mapping.put( tag, classname );
	}
	
	public String getRootClass() {
		return ROOT_CLASS;
	}
	
}
