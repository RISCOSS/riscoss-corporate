package eu.riscoss.db;


public interface RiskScenario extends RiskAnalysisSession {
	
	RiskAnalysisSession getSession();
	
	void set( String key, String value );
	
	String get( String key, String def );
	
}
