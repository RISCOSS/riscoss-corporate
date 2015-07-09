package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JValueMap {
	
	public Map<String,List<JRiskData>> map = new HashMap<>();

	public void add( String entity, String id, String value ) {
		
		List<JRiskData> list = map.get( entity );
		if( list == null ) {
			list = new ArrayList<>();
			map.put( entity, list );
		}
		JRiskData jrd = new JRiskData();
		jrd.id = id;
		jrd.value = value;
		list.add( jrd );
		
	}

}
