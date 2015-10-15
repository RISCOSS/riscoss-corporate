package eu.riscoss.ram.dt;

import eu.riscoss.ram.RiskDataType;
import eu.riscoss.ram.RiskDataValue;

public class DTInteger implements RiskDataType {
	
	public String getName() {
		return "Integer";
	}
	
	public RiskDataValue fromString( String string ) throws Exception {
		return new DVInteger( Integer.parseInt( string ) );
	}
	
}
