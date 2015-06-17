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
