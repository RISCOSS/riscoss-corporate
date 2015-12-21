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

import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.RiscossDBResource;
import eu.riscoss.db.RiscossDatabase;
import eu.riscoss.db.SiteManager;
import eu.riscoss.ram.RiskAnalysisManager;
import eu.riscoss.rdc.RDC;
import eu.riscoss.rdc.RDCFactory;
import eu.riscoss.rdc.RDCRunner;
import eu.riscoss.server.ma.AHPAnalysis;
import eu.riscoss.server.ma.CBRankAnalysis;
import eu.riscoss.server.ma.GAAnalysis;
import eu.riscoss.shared.DBResource;
import eu.riscoss.shared.KnownRoles;
import eu.riscoss.shared.Pair;

public class ServletWrapper extends ServletContainer {
	
	private static final long serialVersionUID = 2410335502314521014L;
	
	@SuppressWarnings("unused")
	public void init() throws ServletException {
		
		super.init();
		
		RiscossDatabase db = null;
		
		try {
			
			RiskAnalysisManager.get().register( "AHP", AHPAnalysis.class );
			RiskAnalysisManager.get().register( "CBRank", CBRankAnalysis.class );
			RiskAnalysisManager.get().register( "GA", GAAnalysis.class );
			
			
			ServletContext sc = getServletContext();
			
			String dbaddr = sc.getInitParameter( "eu.riscoss.db.address" );
			String dbname = sc.getInitParameter( "eu.riscoss.db.name" );
			
			String superadmin = sc.getInitParameter( "eu.riscoss.db.admin.username" );
			String superpwd = sc.getInitParameter( "eu.riscoss.db.admin.password" );
			
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
			
			DBConnector.initDatabase( dbaddr );
			
			System.out.println( "DB address: " + dbaddr );
			System.out.println( "DB name: " + dbname );
			
			String initString = sc.getInitParameter( "eu.riscoss.param.domains.list" );
			
			db = DBConnector.openDatabase( superadmin, superpwd );
			
			db.init();
			
			for( KnownRoles r : KnownRoles.values() ) {
				db.createRole( r.name() );
			}
			
			{
				SiteManager sm = db.getSiteManager();
				
				sm.init();
				
				for( KnownRoles r : KnownRoles.values() ) {
					sm.createRole( r.name() );
				}
				
				sm.createPage( "/", "RISCOSS Web App", "index.jsp", new String[] { KnownRoles.Administrator.name() } );
				sm.createPage( "/", "Dashboard", "dashboard.jsp", new String[] { KnownRoles.Administrator.name(), KnownRoles.Consumer.name(), KnownRoles.Guest.name(), KnownRoles.Modeler.name(), KnownRoles.Producer.name() });
				
				sm.createSection( "/Configure" );
				sm.createPage( "/Configure", "Layers", "layers.jsp", new String[] { KnownRoles.Administrator.name() } );
				sm.createPage( "/Configure", "Entities", "entities.jsp", new String[] { KnownRoles.Administrator.name(), KnownRoles.Producer.name(), KnownRoles.Modeler.name() } );
				sm.createPage( "/Configure", "Models", "models.jsp", new String[] { KnownRoles.Administrator.name(), KnownRoles.Modeler.name() } );
				//				sm.createPage( "/Configure", "Edit Models", "editor.jsp", new String[] { KnownRoles.Administrator.name(), KnownRoles.Modeler.name() } );
				sm.createPage( "/Configure", "Risk Configurations", "riskconfs.jsp", new String[] { KnownRoles.Administrator.name(), KnownRoles.Modeler.name() } );
				
				sm.createSection( "/Run" );
				//				sm.createPage( "/Run", "One-layer Analysis", "analysis.jsp", new String[] { KnownRoles.Administrator.name(), KnownRoles.Consumer.name(), KnownRoles.Guest.name(), KnownRoles.Modeler.name(), KnownRoles.Producer.name() } );
				sm.deletePage( "/Run", "One-layer Analysis");
				sm.createPage( "/Run", "Multi-layer Analysis", "riskanalysis.jsp", new String[] { KnownRoles.Administrator.name(), KnownRoles.Consumer.name(), KnownRoles.Guest.name(), KnownRoles.Modeler.name(), KnownRoles.Producer.name() } );
				sm.createPage( "/Run", "What-if Analysis", "whatifanalysis.jsp", new String[] { KnownRoles.Administrator.name(), KnownRoles.Consumer.name(), KnownRoles.Guest.name(), KnownRoles.Modeler.name(), KnownRoles.Producer.name() } );
				sm.createPage( "/Run", "AHP Session Analysis", "rma.jsp", new String[] { KnownRoles.Administrator.name(), KnownRoles.Consumer.name(), KnownRoles.Guest.name(), KnownRoles.Modeler.name(), KnownRoles.Producer.name() } );
				//				sm.createPage( "/Run", "CBRank Analysis", "analysis.jsp?type=cbrank", new String[] { KnownRoles.Administrator.name(), KnownRoles.Consumer.name(), KnownRoles.Guest.name(), KnownRoles.Modeler.name(), KnownRoles.Producer.name() } );
				//				sm.createPage( "/Run", "Genetic Algorithm Analysis", "analysis.jsp?type=ga", new String[] { KnownRoles.Administrator.name(), KnownRoles.Consumer.name(), KnownRoles.Guest.name(), KnownRoles.Modeler.name(), KnownRoles.Producer.name() } );
				
				sm.createSection( "/Browse" );
				sm.createPage( "/Browse", "Risk Data Repository", "rdr.jsp", new String[] { KnownRoles.Administrator.name(), KnownRoles.Consumer.name(), KnownRoles.Guest.name(), KnownRoles.Modeler.name(), KnownRoles.Producer.name() } );
				sm.createPage( "/Browse", "Risk Analysis Sessions", "ras.jsp", new String[] { KnownRoles.Administrator.name(), KnownRoles.Consumer.name(), KnownRoles.Guest.name(), KnownRoles.Modeler.name(), KnownRoles.Producer.name() } );
				
				sm.createSection( "/Admin" );
				sm.deletePage( "/Admin", "Users and Roles" );
				sm.createPage( "/Admin", "Domains", "admin.jsp", new String[] { KnownRoles.Administrator.name() } );
				sm.createPage( "/Admin", "Roles", "roles.jsp", new String[] {KnownRoles.Administrator.name() } );
			}
			
			
			db.setRoleProperty( "Guest", "allowedPages", "" );
			
			if( initString != null ) {
				String[] tokens = initString.split( "[,]" );
				for( String tok : tokens ) {
					//tok = tok.replace(' ', '_');
					db.createDomain( tok );
				}
			}
			
			
			if( initString != null ) {
				String[] tokens = initString.split( "[,]" );
				for( String tok : tokens ) {
					RiscossDB domainDB = null;
					try {
						domainDB = DBConnector.openDB( tok, superadmin, superpwd );
						for( KnownRoles r : KnownRoles.values() ) {
							domainDB.createRole( r.name() );
							for( Pair<DBResource,String> perm : r.permissions() ) {
								domainDB.addPermissions( r.name(), RiscossDBResource.valueOf( perm.getLeft().name() ), perm.getRight() );
							}
							db.setPredefinedRole( tok, KnownRoles.Administrator.name() );
						}
					}
					catch( Exception ex ) {}
					finally {
						DBConnector.closeDB( domainDB );
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			DBConnector.closeDB( db );
		}
		
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
	
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
		
		super.service( req, res );
		
	}
}
