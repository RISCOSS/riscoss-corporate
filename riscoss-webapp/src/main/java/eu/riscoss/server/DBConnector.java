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
import java.net.URLEncoder;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.codec.binary.Base64;

import eu.riscoss.db.ORiscossDatabase;
import eu.riscoss.db.ORiscossDomain;
import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.RiscossDatabase;

public class DBConnector {
	
	static String db_addr = null;
	
	private static Lock lock = new ReentrantLock();
	
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
	
	private static void lock() {
//		System.out.println( "Lock by " + Thread.currentThread() );
//		Thread.dumpStack();
		lock.lock();
	}
	
	private static void unlock() {
//		System.out.println( "UNLock by " + Thread.currentThread() );
//		Thread.dumpStack();
		lock.unlock();
	}
	
	/**
	 * Opens the database with username and password, specific for "superuser" access to change domains and users.
	 * @param username
	 * @param password
	 * @return
	 */
	public static RiscossDatabase openDatabase( String username, String password ) {
		lock();
		try {
			return new ORiscossDatabase( db_addr, username, password );
		}
		catch( Exception ex ) {
			unlock();
			throw new RuntimeException( ex );
		}
	}
	/**
	 * Opens the database with a previously stored token (e.g. from a cookie), specific for "superuser" access to change domains and users.
	 * @param token
	 * @return
	 */
	public static RiscossDatabase openDatabase( String token ) {
		lock();
		try {
			return new ORiscossDatabase( db_addr, Base64.decodeBase64( token ) );
		}
		catch( Exception ex ) {
			unlock();
			throw new RuntimeException( ex );
		}
	}
	
	
	/**
	 * Opens the database with username and password, for normal access with domain and user
	 * @param domain
	 * @param username
	 * @param password
	 * @return
	 */
	public static RiscossDB openDB( String domain, String username, String password ) {
		try {
			lock();
			return new ORiscossDomain( db_addr, URLEncoder.encode( domain, "UTF-8" ), username, password );
		}
		catch( Exception e ) {
			unlock();
			throw new RuntimeException( e );
		}
	}
	/**
	 * Opens the database with a previously stored token (e.g. from a cookie), for normal access with domain and user
	 * @param token
	 * @return
	 */
	public static RiscossDB openDB( String domain, String token ) {
		try {
			lock();
			return new ORiscossDomain( db_addr, URLEncoder.encode( domain, "UTF-8" ), Base64.decodeBase64( token ) );
		}
		catch( Exception e ) {
			unlock();
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
		finally {
			unlock();
		}
	}

	public static void initDatabase( String dbaddr ) {
		db_addr = dbaddr;
	}

	public static void closeDB( RiscossDatabase db ) {
		unlock();
		if( db != null )
			db.close();
	}
	
}
