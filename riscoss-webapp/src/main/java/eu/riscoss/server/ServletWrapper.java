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

import java.io.IOException;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.glassfish.jersey.servlet.ServletContainer;
import org.reflections.Reflections;

import eu.riscoss.rdc.RDC;
import eu.riscoss.rdc.RDCFactory;
import eu.riscoss.rdc.RDCRunner;

public class ServletWrapper extends ServletContainer {
	
//	private static Logger logger;
	
	static {
		DBConnector.closeDB( DBConnector.openDB() );
		
		Reflections reflections = new Reflections( RDCRunner.class.getPackage().getName() );
		
		Set<Class<? extends RDC>> subTypes = reflections.getSubTypesOf(RDC.class);
		
		for( Class<? extends RDC> cls : subTypes ) {
			try {
				RDC rdc = (RDC)cls.newInstance();
				RDCFactory.get().registerRDC( rdc );
			}
			catch( Exception ex ) {}
		}
		
//		String nameOfLogger = ServletWrapper.class.getName();

//		logger = Logger.getLogger(nameOfLogger);

	}
	
	private static final long serialVersionUID = 2410335502314521014L;
	
	public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
//		logger.info( this.getClass().getSimpleName() + ".service()" );
		super.service( req, res );
	}
}
