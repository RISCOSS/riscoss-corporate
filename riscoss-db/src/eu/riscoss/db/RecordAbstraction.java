package eu.riscoss.db;

public interface RecordAbstraction {
	
	String getName();
	
	String getProperty( String key, String def );
	
}
