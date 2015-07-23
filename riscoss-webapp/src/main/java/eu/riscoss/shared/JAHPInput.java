package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public class JAHPInput {
	
	int		ngoals = 0;
	int		nrisks = 0;
	
	public List<JAHPComparison> goals = new ArrayList<JAHPComparison>();
	public List<List<JAHPComparison>> risks = new ArrayList<List<JAHPComparison>>();
	
	public JAHPInput() {}
	
	public void setGoalCount( int n ) {
		this.ngoals = n;
	}
	
	public void setRiskCount( int n ) {
		this.nrisks = n;
	}
	
	@JsonIgnore
	public int getGoalCount() {
		return (int) this.ngoals;
	}
	
	@JsonIgnore
	public int getRiskCount() {
		return (int) this.nrisks;
	}
	
}
