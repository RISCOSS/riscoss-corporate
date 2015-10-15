package eu.riscoss.ram.dt;

import eu.riscoss.ram.RiskDataValue;

public class DVInteger implements RiskDataValue {
	
	int value = 0;
	
	public DVInteger( int initValue ) {
		this.value = initValue;
	}

	public String getHumanReadablString() {
		return String.valueOf( value );
	}
	
}
