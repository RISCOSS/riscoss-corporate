package eu.riscoss.ram;

import java.util.HashMap;
import java.util.Map;

import eu.riscoss.fbk.io.RiscossLoader;
import eu.riscoss.fbk.language.Program;
import eu.riscoss.reasoner.Evidence;

public class GATest {
	
	public static void main( String[] args ) {
		new GATest().run( args );
	}
	
	public void run( String[] args ) {
		
		Program program = new Program();
		RiscossLoader loader = new RiscossLoader();
		loader.load( convertStreamToString( 
				GATest.class.getResourceAsStream( "cenatic.xml" ) ), program );
		
		Map<String,Evidence>						map = new HashMap<String,Evidence>();
		
		map.put( "Affinity Risk", new Evidence( 0.33, 0 ) );
		
		Map<String,Double> input = new HashMap<>();
		
		input.put( "#s:MIT", 1.0 );
		
		GA ga = new GA();
		
		Map<String,Double> output =
				ga.run( program, map, input );
		
		for( String id : output.keySet() ) {
			System.out.println( id + ": " + output.get( id ) );
		}
		
	}
	
	String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = null;
		try {
			s = new java.util.Scanner(is).useDelimiter("\\A");
			return s.hasNext() ? s.next() : "";
		}
		finally {
			if( s != null ) s.close();
		}
	}
	
}
