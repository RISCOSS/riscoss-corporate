package eu.riscoss.ram.dt;

import eu.riscoss.ram.RiskDataType;
import eu.riscoss.ram.RiskDataValue;

public class DTReal implements RiskDataType {

	public String getName() {
		return "Real";
	}
	
	public RiskDataValue fromString( String string ) throws Exception {
		return new DVReal( Double.parseDouble( string ) );
	}
	
}
