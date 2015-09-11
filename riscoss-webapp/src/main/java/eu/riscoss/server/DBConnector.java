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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.codec.binary.Base64;

import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.RiscossDatabase;
import eu.riscoss.db.RiscossOrientDB;
import eu.riscoss.db.RiscossOrientDatabase;

public class DBConnector {
	
	static String db_addr = null;
	
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
	
	public static RiscossDatabase openDatabase( String username, String password ) {
		return new RiscossOrientDatabase( db_addr, username, password );
	}
	
	public static RiscossDatabase openDatabase( String token ) {
		return new RiscossOrientDatabase( db_addr, Base64.decodeBase64( token ) );
	}
	
	public static RiscossDB openDB( String domain ) {
		try {
			return new RiscossOrientDB( db_addr, URLEncoder.encode( domain, "UTF-8" ) );
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException( e );
		}
	}
	
	public static RiscossDB openDB( String domain, String username, String password ) {
		try {
			return new RiscossOrientDB( db_addr, URLEncoder.encode( domain, "UTF-8" ), username, password );
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException( e );
		}
	}
	
	public static RiscossDB openDB( String domain, String token ) {
		try {
			return new RiscossOrientDB( db_addr, URLEncoder.encode( domain, "UTF-8" ), Base64.decodeBase64( token ) );
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException( e );
		}
	}
	
	public static void closeDB( RiscossDB db ) {
		if( db == null ) return;
		try {
			db.close();
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
	}

	public static void initDatabase( String dbaddr ) {
		db_addr = dbaddr;
	}
	
}
