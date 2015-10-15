package eu.riscoss.ram.dt;

import eu.riscoss.ram.RiskDataValue;

public class DVReal implements RiskDataValue {
	
	double value = 0;
	
	public DVReal( double initValue ) {
		this.value = initValue;
	}
	
	public String getHumanReadablString() {
		return String.valueOf( value );
	}
	
}
