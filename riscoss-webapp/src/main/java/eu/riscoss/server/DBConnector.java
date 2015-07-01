/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/**
 * @author 	Alberto Siena
**/

package eu.riscoss.server;

import java.io.File;
import java.util.HashMap;

import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.RiscossOrientDB;
import eu.riscoss.shared.CookieNames;

public class DBConnector {
	
	static String db_addr = null;
	
//	static {
//		
//		File location = new File( "/Users/albertosiena" );
//		
//		if( new File( location, "temp" ).exists() ) {
//			db_addr = "plocal:" + location.getAbsolutePath() + "/temp/riscoss-db";
//		}
//		else {
//			try {
//				location = findLocation( DBConnector.class );
//				String directory = URLDecoder.decode( location.getAbsolutePath(), "UTF-8" );
//				db_addr = "plocal:" + directory + "/riscoss-db";
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//				db_addr = "plocal:riscoss-db";
//			}
//		}
//		
//		
//		if( db_addr == null )
//			db_addr = "plocal:riscoss-db";
//		
//		System.out.println( db_addr );
//		
//	}
	
	public static File findLocation( Class<?> cls ) {
		String t = cls.getPackage().getName() + ".";
		String clsname = cls.getName().substring( t.length() );
		String s = cls.getResource( clsname + ".class" ).toString();
		
		s = s.substring( s.indexOf( "file:" ) + 5 );
		int p = s.indexOf( "!" );
		if( p != -1 )
		{
			s = s.substring( 0, p );
		}
		
		return new File( new File( s ).getParent() );
	}
	
	static ThreadLocal<HashMap<String,String>> threadMap = new ThreadLocal<>();
	
	public static synchronized void setThreadLocalValue( String key, String value ) {
		HashMap<String,String> map = threadMap.get();
		if( map == null ) {
			map = new HashMap<String,String>();
			threadMap.set( map );
		}
		map.put( key, value );
	}
	
	public static synchronized String getThreadLocalValue( String key, String def ) {
		HashMap<String,String> map = threadMap.get();
		if( map == null )return def;
		String ret = map.get( key );
		if( ret == null ) return def;
		return ret;
	}
	
	static RiscossDB openDB() {
		return new RiscossOrientDB( db_addr, getThreadLocalValue( CookieNames.DOMAIN_KEY, "Public Domain" ) );
	}
	
	static void closeDB( RiscossDB db ) {
		if( db == null ) return;
		try {
			db.close();
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
	}
	
}
