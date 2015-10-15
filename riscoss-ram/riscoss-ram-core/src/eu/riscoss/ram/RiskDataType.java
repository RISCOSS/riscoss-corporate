package eu.riscoss.ram;

public interface RiskDataType {

	String getName();
	
	RiskDataValue fromString( String string ) throws Exception;
	
}
