//package eu.riscoss.ram.ahp;
//
//import java.io.ByteArrayOutputStream;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import org.python.core.PyList;
//import org.python.core.PySystemState;
//import org.python.util.PythonInterpreter;
//
//import com.google.gson.Gson;
//
//import eu.riscoss.db.RiskScenario;
//import eu.riscoss.fbk.io.XmlWriter;
//import eu.riscoss.fbk.language.Proposition;
//import eu.riscoss.fbk.language.Relation;
//import eu.riscoss.ram.MitigationActivity;
//import eu.riscoss.reasoner.FBKRiskAnalysisEngine;
//import eu.riscoss.server.res.Resources;
//
//public class AHPAnalysis extends MitigationActivity {
//	
//	static class IdMap {
//		
//		Map<String,Map<String,Integer>> goals = new HashMap<String,Map<String,Integer>>();
//		
//		public void set( String id1, String id2, int value ) {
//			
//			Map<String,Integer> m = goals.get( id1 );
//			
//			if( m == null ) {
//				m = new HashMap<String,Integer>();
//				goals.put( id1, m );
//			}
//			
//			m.put( id1, value );
//			
//		}
//		
//		public Map<String,Integer> getIdOrder() {
//			Map<String,Integer> list = new HashMap<String,Integer>();
//			int i = 0;
//			for( String s : goals.keySet() ) {
//				list.put( s, i );
//				i++;
//			}
//			return list;
//		}
//
//		public List<Integer> getIdList() {
//			List<Integer> list = new ArrayList<Integer>();
//			int i = 0;
//			for( String s : goals.keySet() ) {
//				list.add( i );
//				i++;
//			}
//			return list;
//		}
//
//		public int size() {
//			return goals.size();
//		}
//		
//	}
//	
//	IdMap goals = new IdMap();
//	
//	List<String> risks = new ArrayList<String>();
//	
//	public String mkList( List<JAHPComparison> list, Map<String, Integer> id_map  ) {
//		final int[] numbers = new int[] { 9, 7, 5, 3, 1, 3, 5, 7, 9 };
//		String ret = "";
//		String sep = "";
//		for( JAHPComparison c : list ) {
//			if( c.value < 4 ) {
//				ret += sep + "[" + id_map.get( c.getId1() ) + "," + id_map.get( c.getId2() ) + "," + numbers[c.value] + "]";
//			}
//			else {
//				ret += sep + "[" + id_map.get( c.getId2() ) + "," + id_map.get( c.getId1() ) + "," + numbers[c.value] + "]";
//			}
//			sep = ",";
//		}
//		return "[" + ret + "]";
//	}
//	
//	public Map<String,Integer> mkIdMap( List<JAHPComparison> list ) {
//		Map<String,Integer> goal_map = new HashMap<String,Integer>();
//		for( JAHPComparison c : list ) {
//			if( !goal_map.containsKey( c.getId1() ) ) {
//				goal_map.put( c.getId1(), goal_map.size() );
//			}
//			if( !goal_map.containsKey( c.getId2() ) ) {
//				goal_map.put( c.getId2(), goal_map.size() );
//			}
//		}
//		return goal_map;
//	}
//	
////	private JAHPResult run( JAHPInput ahpInput ) {
////		
////		System.out.println( "Running AHP" );
////		
////		try {
////			
////			Map<String,Integer> goal_map = mkIdMap( ahpInput.goals );
////			
////			Map<Integer,String> rmap = new HashMap<Integer,String>();
////			
////			String strList1 = mkList( ahpInput.goals, goal_map );
////			String[] strList2 = new String[ahpInput.risks.size()];
////			for( int i = 0; i < ahpInput.risks.size(); i++ ) {
////				List<JAHPComparison> list = ahpInput.risks.get( i );
////				Map<String,Integer> risk_map = mkIdMap( list );
////				for( String k : risk_map.keySet() ) {
////					rmap.put( risk_map.get( k ), k );
////				}
////				strList2[i] = mkList( list, risk_map );
////			}
////			
////			System.out.println( "Preparing args" );
////			PySystemState stm = new PySystemState();
////			List<String> args = new ArrayList<String>();
////			
////			args.add( "jython" );		// first argument in python is the program name
////			args.add( "" + ahpInput.getGoalCount() );
////			args.add( "" + ahpInput.getRiskCount() );
////			args.add( strList1 );
////			for( int i = 0; i < strList2.length; i++ ) {
////				args.add( strList2[i] );
////			}
////			
////			stm.argv = new PyList( args );
////			
////			System.out.println( "Creating Jython object" );
////			PythonInterpreter jython =
////					new PythonInterpreter( null, stm );
////			
////			System.out.println( "Seeting output stream" );
////			ByteArrayOutputStream out = new ByteArrayOutputStream();
////			jython.setOut( out );
////			
////			System.out.println( "Executing" );
////			jython.execfile( Resources.class.getResource( "ahpNoEig_command.py" ).getFile() );
////			
////			String output = out.toString();
////			
////			System.out.println( "Output:" );
////			System.out.println( output );
////			
////			try {
////				output = output.substring( 1, output.length() -2 );
////				String[] parts = output.split( "[,]" );
////				JAHPResult result = new JAHPResult();
////				for( int i = 0; i < parts.length; i++ ) {
////					result.add( rmap.get( i ), Double.parseDouble( parts[i] ) );
////				}
////				return result;
////			}
////			catch( Exception ex ) {
////				ex.printStackTrace();
////			}
////			finally {
////				if( jython != null )
////					jython.close();
////			}
////			
////		}
////		catch( Exception ex ) {
////			ex.printStackTrace();
////		}
////		
////		return null;
////	}
//	
//	public Map<String,Double> run( ) {
//		
//		System.out.println( "Running AHP" );
//		
//		try {
//			
//			Map<String,Integer> goal_map = goals.getIdOrder(); //mkIdMap( goals );
//			
//			Map<Integer,String> rmap = new HashMap<Integer,String>();
//			
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
//			
//			System.out.println( "Preparing args" );
//			PySystemState stm = new PySystemState();
//			List<String> args = new ArrayList<String>();
//			
//			args.add( "jython" );		// first argument in python is the program name
//			args.add( "" + goals.size() );
//			args.add( "" + risks.size() );
//			args.add( strList1 );
//			for( int i = 0; i < strList2.length; i++ ) {
//				args.add( strList2[i] );
//			}
//			
//			stm.argv = new PyList( args );
//			
//			System.out.println( "Creating Jython object" );
//			PythonInterpreter jython =
//					new PythonInterpreter( null, stm );
//			
//			System.out.println( "Seeting output stream" );
//			ByteArrayOutputStream out = new ByteArrayOutputStream();
//			jython.setOut( out );
//			
//			System.out.println( "Executing" );
//			jython.execfile( Resources.class.getResource( "ahpNoEig_command.py" ).getFile() );
//			
//			String output = out.toString();
//			
//			System.out.println( "Output:" );
//			System.out.println( output );
//			
//			try {
//				output = output.substring( 1, output.length() -2 );
//				String[] parts = output.split( "[,]" );
//				Map<String,Double> result = new HashMap<String,Double>();
//				for( int i = 0; i < parts.length; i++ ) {
//					result.put( rmap.get( i ), Double.parseDouble( parts[i] ) );
//				}
//				return result;
//			}
//			catch( Exception ex ) {
//				ex.printStackTrace();
//			}
//			finally {
//				if( jython != null )
//					jython.close();
//			}
//			
//		}
//		catch( Exception ex ) {
//			ex.printStackTrace();
//		}
//		
//		return null;
//	}
//	
//	public String eval( String value ) {
//		
////		JAHPInput ahpInput = new Gson().fromJson( value, JAHPInput.class );
////		
////		JAHPResult result = run( ahpInput );
////		
////		return new Gson().toJson( result );
//		
//		return "";
//			
//	}
//	
//	@Override
//	public void apply( String string, RiskScenario scenario ) {
//		
//		JAHPResult result = new Gson().fromJson( string, JAHPResult.class );
//		
//		float max = 0;
//		for( Double d : result.values.values() ) {
//			if( d > max ) max = d.floatValue();
//		}
//		if( max == 0 ) max = 1;
//		
//		String target = scenario.getTarget();
//		
//		String layer = scenario.getLayer( target );
//		
//		List<String> models = scenario.getModels( layer );
//		
//		for( String model : models ) {
//			String blob = scenario.getStoredModelBlob( model );
//			try {
//				FBKRiskAnalysisEngine e = new FBKRiskAnalysisEngine();
//				
//				e.loadModel( blob );
//				
//				for( String id : result.values.keySet() ) {
//					
//					Proposition p = e.getProgram().getModel().getProposition( id );
//					
//					if( p == null ) continue;
//					
//					for( Relation r : p.in() ) {
//						if( !"increase".equals( r.getStereotype() ) ) continue;
//						float w = r.getWeight();
//						w = w / max;
//						r.setWeight( w );
//					}
//					
//				}
//				
//				String newBlob = new XmlWriter().generateXml( e.getProgram() ).asString();
//				
//				scenario.storeModelBlob( model, layer, newBlob );
//				
//			}
//			catch( Exception ex ) {
//				ex.printStackTrace();
//			}
//			catch( Error err ) {
//				err.printStackTrace();
//			}
//		}
//		
//	}
//	
//}
