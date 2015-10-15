package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

public class JRiskAnalysisResult {
	
	public EAnalysisResult				result;
	public JRiskAnalysisResultSummary	info;
	
	public List<JRiskAnalysisResultItem>	results = new ArrayList<>();
	
	public JArgumentation				argumentation = new JArgumentation();
	
}
