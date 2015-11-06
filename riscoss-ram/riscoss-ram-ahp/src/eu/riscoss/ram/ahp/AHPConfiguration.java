package eu.riscoss.ram.ahp;

import java.util.ArrayList;
import java.util.List;

import eu.riscoss.ram.RiskConfiguration;

public class AHPConfiguration extends RiskConfiguration {
	
	public List<AHPInput> inputs = new ArrayList<AHPInput>();
	
	public String getAnalysisName() {
		return "AHP";
	}
	
}
