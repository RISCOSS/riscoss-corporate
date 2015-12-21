package eu.riscoss.shared;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JGAInput {
	
	Set<String> enabledIndicators = new HashSet<>();
	Map<String,String> objectives = new HashMap<>();
	String problemName;
	
	public void setEnabledIndicators( String indicatorId ) {
		this.enabledIndicators.add( indicatorId );
	}
	
	public void addObjective( String id, String jsonizedValue ) {
		this.objectives.put( id, jsonizedValue );
	}
	
	public void setProblen( String problemName ) {
		this.problemName = problemName;
	}

	public Map<String, String> getObjectives() {
		return this.objectives;
	}
	
	public Set<String> getEnabledIndicators() {
		return this.enabledIndicators;
	}
	
}
