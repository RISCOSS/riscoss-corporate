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
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.glassfish.jersey.servlet.ServletContainer;
import org.reflections.Reflections;

import eu.riscoss.db.RiscossDatabase;
import eu.riscoss.rdc.RDC;
import eu.riscoss.rdc.RDCFactory;
import eu.riscoss.rdc.RDCRunner;

public class ServletWrapper extends ServletContainer {
	
	private static final long serialVersionUID = 2410335502314521014L;
	
	@SuppressWarnings("unused")
	public void init() throws ServletException {
		
		super.init();
		
		{
			ServletContext sc = getServletContext();
			
			String dbaddr = sc.getInitParameter( "eu.riscoss.db.address" );
			String dbname = sc.getInitParameter( "eu.riscoss.db.name" );
			
			if( dbname == null ) {
				dbname = "riscoss-db";
			}
			
			if( dbaddr == null ) {
				File location = new File( "/Users/albertosiena" );
				
				if( new File( location, "temp" ).exists() ) {
					dbaddr = "plocal:" + location.getAbsolutePath() + "/temp/" + dbname;
				}
				else {
					try {
						location = DBConnector.findLocation( DBConnector.class );
						String directory = URLDecoder.decode( location.getAbsolutePath(), "UTF-8" );
						dbaddr = "plocal:" + directory + "/" + dbname;
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						dbaddr = "plocal:" + dbname;
					}
				}
				
				if( dbaddr == null )
					dbaddr = "plocal:" + dbname;
				
				System.out.println( "Using database " + dbaddr );
			}
			else {
				dbaddr = dbaddr + "/" + dbname;
			}
			
			DBConnector.setDbaddr(dbaddr);
			
			System.out.println( "DB address: " + dbaddr );
			System.out.println( "DB name: " + dbname );
			
			String initString = sc.getInitParameter( "eu.riscoss.param.domains.list" );
			
			RiscossDatabase db = DBConnector.openDatabase( null, null );
			if( initString != null ) {
				String[] tokens = initString.split( "[,]" );
				for( String tok : tokens ) {
					db.createDomain( tok );
				}
			}
			db.close();
			
//			DBConnector.closeDB( DBConnector.openDB( "Playground" ) ); // Old stuff
			
			Reflections reflections = new Reflections( RDCRunner.class.getPackage().getName() );
			
			Set<Class<? extends RDC>> subTypes = reflections.getSubTypesOf(RDC.class);
			
			for( Class<? extends RDC> cls : subTypes ) {
				try {
					RDC rdc = (RDC)cls.newInstance();
					RDCFactory.get().registerRDC( rdc );
				}
				catch( Exception ex ) {}
			}
		}
		
	}
	
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		
//		try {
//			HttpServletRequest httpReq = (HttpServletRequest) req;
//			for( Cookie cookie : httpReq.getCookies() ) {
//				DBConnector.setThreadLocalValue( cookie.getName(), cookie.getValue() );
//			}
//		}
//		catch( Exception ex ) {}
		
		super.service( req, res );
		
	}
}
