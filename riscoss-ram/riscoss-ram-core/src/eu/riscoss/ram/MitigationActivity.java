package eu.riscoss.ram;

import eu.riscoss.db.RiskScenario;


public abstract class MitigationActivity {

	public abstract String eval( String json , RiskScenario context  );

	public abstract void apply( String output, RiskScenario scenario );

}
