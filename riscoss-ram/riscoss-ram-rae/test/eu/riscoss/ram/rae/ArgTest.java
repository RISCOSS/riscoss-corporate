package eu.riscoss.ram.rae;

import java.io.File;

import eu.riscoss.reasoner.Chunk;
import eu.riscoss.reasoner.DataType;
import eu.riscoss.reasoner.FBKRiskAnalysisEngine;
import eu.riscoss.reasoner.Field;
import eu.riscoss.reasoner.FieldType;

public class ArgTest {
	
	public static void main( String[] args ) {
		
		FBKRiskAnalysisEngine engine = new FBKRiskAnalysisEngine();
		
//		engine.loadModel( new File( ArgTest.class.getResource( "github_maintenance_risk-1434467716514.xml" ).getFile() ) );
//		engine.setField( new Chunk( "large-repository" ), FieldType.INPUT_VALUE, new Field( DataType.INTEGER, 1000 ) );
		
		engine.loadModel( new File( ArgTest.class.getResource( "cenatic-project-1444901966781.xml" ).getFile() ) );
		engine.loadModel( new File( ArgTest.class.getResource( "cenatic-declining_risks-1444901966781.xml" ).getFile() ) );
		
		engine.setField( new Chunk( "#s:MIT" ), FieldType.INPUT_VALUE, new Field( DataType.INTEGER, 1 ) );
		engine.setField( new Chunk( "#s:GPL2" ), FieldType.INPUT_VALUE, new Field( DataType.INTEGER, 1 ) );
		engine.setField( new Chunk( "#target:LGPL2.1" ), FieldType.INPUT_VALUE, new Field( DataType.INTEGER, 1 ) );
		
		engine.runAnalysis( new String[] {} );
		
		RAE rae = new RAE();
		
		Argumentation a = rae.createArgumentation( engine );
		
		a.print( System.out );
		
	}
	
}
