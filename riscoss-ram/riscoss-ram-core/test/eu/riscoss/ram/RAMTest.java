package eu.riscoss.ram;

import eu.riscoss.ram.dt.DTInteger;

public class RAMTest {
	
	public static void main( String[] args ) {
		new RAMTest().run();
	}
	
	public void run() {
		
		RiskAnalysisFactory raf = RiskAnalysisFactory.get();
		
		RiskAnalysisClass rac = new RiskAnalysisClass();
		
		rac.registerDataType( new DTInteger() );
		
		RiskAnalysisInstance rai = rac.create();
		
	}
	
}
