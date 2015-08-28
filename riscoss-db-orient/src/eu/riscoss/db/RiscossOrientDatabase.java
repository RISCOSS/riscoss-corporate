package eu.riscoss.db;

import java.util.List;
import java.util.Set;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.security.OToken;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OSQLEngine;
import com.orientechnologies.orient.core.sql.functions.OSQLFunction;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.orientechnologies.orient.graph.sql.functions.OGraphFunctionFactory;
import com.orientechnologies.orient.server.config.OServerParameterConfiguration;
import com.orientechnologies.orient.server.token.OrientTokenHandler;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

public class RiscossOrientDatabase implements RiscossDatabase {
	
	private static final OServerParameterConfiguration[] I_PARAMS = new OServerParameterConfiguration[] { 
		new OServerParameterConfiguration( OrientTokenHandler.SIGN_KEY_PAR, "any key")
	};
	
	static {
		registerGraphFunctions();
	}
	
	private static void registerGraphFunctions() {
		OGraphFunctionFactory graphFunctions = new OGraphFunctionFactory();
		Set<String> names = graphFunctions.getFunctionNames();
		
		for (String name : names) {
			System.out.println("Enabling ODB graph function: [" + name + "]");
			OSQLEngine.getInstance().registerFunction(name, graphFunctions.createFunction(name)); 
			OSQLFunction function = OSQLEngine.getInstance().getFunction(name);
			if (function != null) {
				// Dummy call, just to ensure that the class is loaded
				function.getSyntax();
//				System.out.println("ODB graph function [" + name + "] is registered: [" + function.getSyntax() + "]");
			}
			else {
//				System.out.println("ODB graph function [" + name + "] NOT registered!!!");
			}
		}
	}
	
	public static OrientTokenHandler createTokenHandler() {
		
		OrientTokenHandler handler = new OrientTokenHandler();
		handler.config(null, I_PARAMS);
		return handler;
		
	}
	
	
	OrientBaseGraph graph;
	private String username;
	
	public RiscossOrientDatabase( String addr, String username, String password) {
		
		if( username != null ) {
			graph = new OrientGraphFactory( addr, username, password ).getNoTx();
		}
		else {
			graph = new OrientGraphFactory( addr ).getNoTx();
		}
		
		this.username = username;
		
	}
	
	@SuppressWarnings("resource") // FIXME
	public RiscossOrientDatabase( String addr, byte[] tokenBytes ) {
		
		OrientTokenHandler handler = RiscossOrientDatabase.createTokenHandler();
		
		OToken tok = handler.parseWebToken( tokenBytes );
		handler.validateBinaryToken( tok );
		this.graph = new OrientGraphNoTx( (ODatabaseDocumentTx)new ODatabaseDocumentTx( addr ).open( tok ) );
		this.username = graph.getRawGraph().getUser().getName(); //tok.getUserName();
		
	}
	
	public List<String> listDomains() {
		try {
			List<ODocument> list = graph.getRawGraph().query( new OSQLSynchQuery<ODocument>( 
					"SELECT FROM " + GDomDB.ROOT_CLASS ) );
			if( list == null ) return null;
			return new GenericNodeCollection<String>( list, new NameAttributeProvider() );
		}
		catch( Exception ex ) {
			return null;
		}
	}
	
	public String getUsername() {
		return this.username;
	}
	
	@Override
	public void close() {
		this.graph.getRawGraph().close();
		this.graph = null;
	}

	@Override
	public void createDomain( String domainName ) {
		
		Vertex v = getRoot( domainName, 0 );
		if( v == null ) {
			v = graph.addVertex( GDomDB.ROOT_CLASS, (String)null );
			v.setProperty( "tag", domainName );
			graph.commit();
		}
		
	}
	
	private Vertex getRoot( String cls, int index ) {
		try {
			List<ODocument> list = graph.getRawGraph().query( new OSQLSynchQuery<ODocument>( 
					"SELECT FROM " + GDomDB.ROOT_CLASS + " WHERE tag='" + cls + "'" ) );
			if( list == null ) return null;
			if( !(list.size() > index) ) return null;
			return graph.getVertex( list.get( index ).getIdentity() );
		}
		catch( Exception ex ) {
			return null;
		}
	}
	
}
