package eu.riscoss.ram;

import java.util.HashMap;
import java.util.Map;

public class RiskAnalysisClass {
	
	Map<String,RiskDataType> supportedDataTypes = new HashMap<String,RiskDataType>();
	
	public void registerDataType( RiskDataType dt ) {
		supportedDataTypes.put( dt.getName(), dt );
	}

	public RiskAnalysisInstance create() {
		return null;
	}
	
}
