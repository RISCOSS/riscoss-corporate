package eu.riscoss.server;

import eu.riscoss.db.RiscossDB;

public interface DataSource {
	
	public String findRiskData( String entity, String dataId, RiscossDB db );
	
}
