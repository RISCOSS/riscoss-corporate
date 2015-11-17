package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

public class JAHPInput {
	
	public int		ngoals = 0;
	public int		nrisks = 0;
	
	public List<JAHPComparison> goals = new ArrayList<JAHPComparison>();
	public List<List<JAHPComparison>> risks = new ArrayList<List<JAHPComparison>>();
	
	public JAHPInput() {}
	
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
