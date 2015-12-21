package eu.riscoss.ram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IdMap {
	
	Map<String,Map<String,Integer>> goals = new HashMap<String,Map<String,Integer>>();
	
	public IdMap() {
//		public Map<String,Integer> mkIdMap( List<JAHPComparison> list ) {
//		Map<String,Integer> goal_map = new HashMap<String,Integer>();
//		for( JAHPComparison c : list ) {
//			if( !goal_map.containsKey( c.getId1() ) ) {
//				goal_map.put( c.getId1(), goal_map.size() );
//			}
//			if( !goal_map.containsKey( c.getId2() ) ) {
//				goal_map.put( c.getId2(), goal_map.size() );
//			}
//		}
	}
	
	public void set( String id1, String id2, int value ) {
		
		Map<String,Integer> m = goals.get( id1 );
		
		if( m == null ) {
			m = new HashMap<String,Integer>();
			goals.put( id1, m );
		}
		
		m.put( id1, value );
		
	}
	
	public Map<String,Integer> getIdOrder() {
		Map<String,Integer> list = new HashMap<String,Integer>();
		int i = 0;
		for( String s : goals.keySet() ) {
			list.put( s, i );
			i++;
		}
		return list;
	}
	
	public List<Integer> getIdList() {
		List<Integer> list = new ArrayList<Integer>();
		for( int i = 0; i < goals.keySet().size(); i++ ) {
			list.add( i );
		}
//		int i = 0;
//		for( String s : goals.keySet() ) {
//			list.add( i );
//			i++;
//		}
		return list;
	}
	
	public int size() {
		return goals.size();
	}
	
}
