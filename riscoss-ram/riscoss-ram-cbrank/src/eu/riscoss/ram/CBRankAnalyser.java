package eu.riscoss.ram;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.python.core.PyList;
import org.python.core.PySystemState;
import org.python.util.PythonInterpreter;

import eu.riscoss.server.res.Resources;

public class CBRankAnalyser {
	
	static class EditableNumber {
		private double val = 0;
		public EditableNumber( double initialValue ) {
			this.val = initialValue;
		}
		public double doubleValue() {
			return val;
		}
		public int intValue() {
			return (int)val;
		}
		public void setValue( double preference ) {
			this.val = preference;
		}
	}
	
	int numberOfCycles = 1;
	int numberOfAlternatives = 0;
	float epsilon = 0.001F;
	List<List<EditableNumber>> preferenceMatrix;
	List<List<EditableNumber>> functions = new ArrayList<>();
	
//	public enum PARAMETER_NAME {
//		NUMBER_OF_ALTERNATIVES,
//		NUMBER_OF_CYCLES,
//		EPSILON
//	}
//	
//	public Enum<?>[] getParameterNames() {
//		return PARAMETER_NAME.values();
//	}
//	
//	public void setParameter( String par, String value ) {
//		PARAMETER_NAME par_name = null;
//		try {
//			par_name = PARAMETER_NAME.valueOf( par );
//		}
//		catch( Exception ex ) {
//			ex.printStackTrace();
//			return;
//		}
//		try {
//			switch( par_name ) {
//			case NUMBER_OF_ALTERNATIVES:
//				this.numberOfAlternatives = Integer.parseInt( value );
//				break;
//			case NUMBER_OF_CYCLES:
//				this.numberOfCycles = Integer.parseInt( value );
//				break;
//			case EPSILON:
//				this.epsilon = (float)Double.parseDouble( value );
//				break;
//			}
//		}
//		catch( Exception ex ) {
//			ex.printStackTrace();
//		}
//	}
	
	/**
	 * commands line:
		3 [[10.0, 6.0, 'NaN'], [40.0, 20.0, 1.0]] 1 0.001 [[0,1,1], [-1,0,1], [-1,-1,0]]
		
		3 - number of alternatives
		[[10.0, 6.0, 'NaN'], [40.0, 20.0, 1.0]]  - two functions F (list of lists)
		1 - number of cycles
		0.001 - epsilon
		[[0,1,1], [-1,0,1], [-1,-1,0]] - preferences (matrix 3 x 3, three alternatives)

	 * @return
	 */
	List<String> createCommandLine() {
		
		List<String> args = new ArrayList<>();
		
		args.add( String.valueOf( numberOfAlternatives ) );
		if( functions.size() < 1 ) {
			List<List<EditableNumber>> def = new ArrayList<>();
			List<EditableNumber> def_values = new ArrayList<>();
			for( int i = 0; i < this.numberOfAlternatives; i++ ) {
				def_values.add( new EditableNumber( 0.0 ) );
			}
			def.add( def_values );
			args.add( matrix2Python( def, false ) );
		}
		else {
			args.add( matrix2Python( functions, false ) );
		}
		args.add( String.valueOf( this.numberOfCycles ) );
		args.add( String.valueOf( this.epsilon ) );
		args.add( matrix2Python( preferenceMatrix, true ) );
		
		System.out.print( ">>>>>" );
		for( String arg : args ) {
			System.out.print( " " + arg );
		}
		System.out.println();
		
		return args;
		
	}
	
	private String matrix2Python( List<List<EditableNumber>> matrix, boolean typeInt ) {
		String ret = "[";
		String cell_sep = "";
		String row_sep = "";
		
		for( List<EditableNumber> row : matrix ) {
			ret += row_sep + "[";
			for( EditableNumber cell : row ) {
				
				if( typeInt ) {
					ret += cell_sep + cell.intValue();
				}
				else {
					ret += cell_sep + cell.doubleValue();
				}
				
				cell_sep = ",";
				
			}
			ret += "]";
			cell_sep = "";
			row_sep = ",";
		}
		ret += "]";
		
		return ret;
	}

	public Map<String,Double> run( ) {
		
		System.out.println( "Running CBRank" );
		
		try {
			
			//			Map<String,Integer> goal_map = goals.getIdOrder(); //mkIdMap( goals );
			
			Map<Integer,String> rmap = new HashMap<Integer,String>();
			
			//			String strList1 = mkList( goals, goal_map );
			//			String[] strList2 = new String[risks.size()];
			//			for( int i = 0; i < risks.size(); i++ ) {
			//				List<JAHPComparison> list = risks.get( i );
			//				Map<String,Integer> risk_map = mkIdMap( list );
			//				for( String k : risk_map.keySet() ) {
			//					rmap.put( risk_map.get( k ), k );
			//				}
			//				strList2[i] = mkList( list, risk_map );
			//			}
			
			System.out.println( "Preparing args" );
			PySystemState stm = new PySystemState();
			List<String> args = createCommandLine();
			
			if( args.size() < 1 ) {
				args.add( "jython" );
			}
			else {
				if( !"jython".equals( args.get( 0 ) ) ) {
					args.add( 0,  "jython" );
				}
			}
			
			stm.argv = new PyList( args );
			
			System.out.println( "Creating Jython object" );
			PythonInterpreter jython =
					new PythonInterpreter( null, stm );
			
			System.out.println( "Seeting output stream" );
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			jython.setOut( out );
			
			System.out.println( "Executing" );
			jython.execfile( Resources.class.getResource( "CBRank_feedback_one.py" ).getFile() );
			
			String output = out.toString();
			
			System.out.println( "Output:" );
			System.out.println( output );
			
			try {
				output = output.substring( 1, output.length() -2 );
				String[] parts = output.split( "[,]" );
				Map<String,Double> result = new HashMap<String,Double>();
				for( int i = 0; i < parts.length; i++ ) {
					result.put( rmap.get( i ), Double.parseDouble( parts[i] ) );
				}
				return result;
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

	public void setNumberOfAlternatives( int n ) {
		this.numberOfAlternatives = n;
		this.preferenceMatrix = new ArrayList<List<EditableNumber>>();
		for( int i = 0; i < n; i++ ) {
			List<EditableNumber> row = new ArrayList<>();
			for( int j = 0; j < n; j++ ) {
				row.add( new EditableNumber( 0 ) );
			}
			preferenceMatrix.add( row );
		}
	}
	
	public void setNumberOfCycles( int numberOfCycles ) {
		this.numberOfCycles = numberOfCycles;
	}

	public void setEpsilon( float epsilon ) {
		this.epsilon = epsilon;
	}

	public void setPreference( int c, int r, int preference  ) {
		preferenceMatrix.get( c ).get( r ).setValue( preference );
	}
	
}
