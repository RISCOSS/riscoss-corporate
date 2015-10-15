package eu.riscoss.ram.ahp;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.python.core.PyList;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

public class AHPAnalysis {
	
	public JAHPResult run( JAHPInput ahpInput ) {
		
		System.out.println( "Running AHP" );
		
		try {
			
			Map<String,Integer> goal_map = ahpInput.mkIdMap( ahpInput.goals );
			
			String strList1 = ahpInput.mkList( ahpInput.goals, goal_map );
			String[] strList2 = new String[ahpInput.risks.size()];
			for( int i = 0; i < ahpInput.risks.size(); i++ ) {
				List<JAHPComparison> list = ahpInput.risks.get( i );
				Map<String,Integer> risk_map = ahpInput.mkIdMap( list );
				strList2[i] = ahpInput.mkList( list, risk_map );
			}
			
			System.out.println( "Preparing args" );
			PySystemState stm = new PySystemState();
			List<String> args = new ArrayList<String>();
			
			args.add( "jython" );		// first argument in python is the program name
			args.add( "" + ahpInput.getGoalCount() );
			args.add( "" + ahpInput.getRiskCount() );
			args.add( strList1 );
			for( int i = 0; i < strList2.length; i++ ) {
				args.add( strList2[i] );
			}
			
			stm.argv = new PyList( args );
			
			System.out.println( "Creating Jython object" );
			PythonInterpreter jython =
				    new PythonInterpreter( null, stm );
			
			System.out.println( "Seeting output stream" );
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			jython.setOut( out );
			
			System.out.println( "Executing" );
			jython.execfile( AHPAnalysis.class.getResource( "res/ahpNoEig_command.py" ).getFile() );
			
			String output = out.toString();
			
			System.out.println( "Output:" );
			System.out.println( output );
			
			try {
				output = output.substring( 1, output.length() -2 );
				String[] parts = output.split( "[,]" );
				return new JAHPResult( parts );
			}
			catch( Exception ex ) {
				ex.printStackTrace();
			}
			finally {
				if( jython != null )
					jython.close();
			}
			
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		
		return null;
	}
	
}
