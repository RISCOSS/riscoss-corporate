package eu.riscoss.ram.ahp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AHPInput {
	
	public String mkList( List<AHPComparison> list, Map<String, Integer> id_map  ) {
		final int[] numbers = new int[] { 9, 7, 5, 3, 1, 3, 5, 7, 9 };
		String ret = "";
		String sep = "";
		for( AHPComparison c : list ) {
			if( c.value < 4 ) {
				ret += sep + "[" + id_map.get( c.getId1() ) + "," + id_map.get( c.getId2() ) + "," + numbers[c.value] + "]";
			}
			else {
				ret += sep + "[" + id_map.get( c.getId2() ) + "," + id_map.get( c.getId1() ) + "," + numbers[c.value] + "]";
			}
			sep = ",";
		}
		return "[" + ret + "]";
	}
	
	public Map<String,Integer> mkIdMap( List<AHPComparison> list ) {
		Map<String,Integer> goal_map = new HashMap<String,Integer>();
		for( AHPComparison c : list ) {
			if( !goal_map.containsKey( c.getId1() ) ) {
				goal_map.put( c.getId1(), goal_map.size() );
			}
			if( !goal_map.containsKey( c.getId2() ) ) {
				goal_map.put( c.getId2(), goal_map.size() );
			}
		}
		return goal_map;
	}
	

	
	int		ngoals = 0;
	int		nrisks = 0;
	
	public List<AHPComparison> goals = new ArrayList<AHPComparison>();
	public List<List<AHPComparison>> risks = new ArrayList<List<AHPComparison>>();
	
	public AHPInput() {}
	
	public void setGoalCount( int n ) {
		this.ngoals = n;
	}
	
	public void setRiskCount( int n ) {
		this.nrisks = n;
	}
	
	public int getGoalCount() {
		return (int) this.ngoals;
	}
	
	public int getRiskCount() {
		return (int) this.nrisks;
	}
	
}
