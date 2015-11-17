package eu.riscoss.db;


public interface RiskScenario extends RiskAnalysisSession {

	void set( String key, String value );

	String get( String key, String def );
	
}
