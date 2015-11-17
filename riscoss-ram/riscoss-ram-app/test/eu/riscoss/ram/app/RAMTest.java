//package eu.riscoss.ram.app;
//
//import java.util.ArrayList;
//import java.util.List;
//
//import eu.riscoss.db.RiskScenario;
//import eu.riscoss.ram.MitigationObjective;
//import eu.riscoss.ram.RiskAnalysisManager;
//import eu.riscoss.ram.ahp.AHPAnalysis;
//import eu.riscoss.ram.ahp.AHPInput;
//import eu.riscoss.ram.rae.dt.DVEvidence;
//import eu.riscoss.reasoner.Evidence;
//
//public class RAMTest {
//	
//	public static void main( String[] args ) {
//		new RAMTest().run();
//	}
//	
//	public void run() {
//		
//		RiskAnalysisManager ram = new RiskAnalysisManager();
//		
//		ram.register( "AHP", AHPAnalysis.class );
//		
////		RiskConfiguration conf = new RiskConfiguration();
//		
//		{
//			List<String> models = new ArrayList<String>();
//			
//			models.add( "<model-blob-content/>" );
//			
//			conf.set( "models", new ArrayList<String>() );
//		}
//		
//		conf.set( "target", "myentity" ); // TODO: replace with actual object?
//		
//		RiskScenario scenario = new InMemoryScenario();
//		
//		MitigationObjective objective = new MitigationObjective();
//		
//		objective.setValue( "myentity", "myrisk", new DVEvidence( new Evidence( 0, 0 ) ) );
//		
//		{
////			RiskConfiguration rc = new RiskConfiguration();
//			
//			// TODO User input to set parameters
//			
//			conf.set( "ahpinput", new AHPInput() );
//			
//			ram.apply( conf, scenario );
//			
//			// TODO change parameters of apply a different mitigation activity
//			
//		}
//		
//	}
//	
//}
