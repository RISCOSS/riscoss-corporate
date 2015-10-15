package eu.riscoss.ram;

public class RiskAnalysisFactory {
	
	private static RiskAnalysisFactory instance = new RiskAnalysisFactory();
	
	public static RiskAnalysisFactory get() {
		return instance;
	}
	
}
