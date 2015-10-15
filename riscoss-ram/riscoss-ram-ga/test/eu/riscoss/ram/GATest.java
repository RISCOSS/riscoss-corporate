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
		
		Map<String,Object>						map = new HashMap<String,Object>();
		
		map.put( "Affinity Risk", new Evidence( 0.33, 0 ) );
		
		
		
		GA ga = new GA();
		
		ga.run( program, map );
		
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
