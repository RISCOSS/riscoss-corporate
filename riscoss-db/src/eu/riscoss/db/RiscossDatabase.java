package eu.riscoss.db;

import java.util.List;

public interface RiscossDatabase {
	
	public String getUsername();
	
	public List<String> listDomains();

	public void close();

	public void createDomain( String domainName );
	
}
