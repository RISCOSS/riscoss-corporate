package eu.riscoss.db;

import java.util.List;
import java.util.Set;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.security.OToken;
import com.orientechnologies.orient.core.sql.OSQLEngine;
import com.orientechnologies.orient.core.sql.functions.OSQLFunction;
import com.orientechnologies.orient.graph.sql.functions.OGraphFunctionFactory;
import com.orientechnologies.orient.server.config.OServerParameterConfiguration;
import com.orientechnologies.orient.server.token.OrientTokenHandler;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

import eu.riscoss.db.domdb.GAuthDom;
import eu.riscoss.db.domdb.GDomConfig;
import eu.riscoss.db.domdb.GDomContainer;

/**
 * (ONLY) Riscoss database superadmin access, for changing domains and users.
 * Use RiscossDB for all accesses to the database content (except users and domain) with a defined domain and user credentials
 *
 */
public class ORiscossDatabase implements RiscossDatabase {
	
	private static final OServerParameterConfiguration[] I_PARAMS = new OServerParameterConfiguration[] { 
		new OServerParameterConfiguration( OrientTokenHandler.SIGN_KEY_PAR, "any key"),
		new OServerParameterConfiguration( OrientTokenHandler.SESSION_LENGHT_PAR, "525600000" ) // ( 1000* 60 * 24 * 365 ) ) = 1 year
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
			}
		}
	}
	
	public static OrientTokenHandler createTokenHandler() {
		
		OrientTokenHandler handler = new OrientTokenHandler();
		handler.config(null, I_PARAMS);
		return handler;
		
	}
	
	
	GDomContainer container;
	
	private String username;
	
	public ORiscossDatabase( String addr, String username, String password) {
		
		OrientBaseGraph graph = null;
		
		if( username != null ) {
			graph = new OrientGraphFactory( addr, username, password ).getNoTx();
		}
		else {
			graph = new OrientGraphFactory( addr ).getNoTx();
		}
		
		container = new GDomContainer( graph );
		
		this.username = username;
		
	}
	
	public ORiscossDatabase( String addr, byte[] tokenBytes ) {
		
		OrientTokenHandler handler = ORiscossDatabase.createTokenHandler();
		
		OToken tok = handler.parseWebToken( tokenBytes );
		handler.validateBinaryToken( tok );
		@SuppressWarnings("resource") // FIXME
		OrientBaseGraph graph = new OrientGraphNoTx( (ODatabaseDocumentTx)new ODatabaseDocumentTx( addr ).open( tok ) );
		this.container = new GDomContainer( graph );
		this.username = graph.getRawGraph().getUser().getName();
		
	}
	
	public List<String> listDomains() {
		return container.domList();
	}
	
	public String getUsername() {
		return this.username;
	}
	
	@Override
	public void close() {
		container.close();
	}

	@Override
	public void createDomain( String domainName ) {
		container.createDom( domainName );
	}
	
	@Override
	public String getRole() {
		
		return container.getGraph().getRawGraph().getMetadata().getSecurity().getRole( username ).getName();
		
	}
	
	@Override
	public void init() {
		
		GDomConfig conf = new GDomConfig();
		
		conf.setMapping( "layers", "Layers" );
		conf.setMapping( "entities", "Entity" );
		conf.setMapping( "models", "Model" );
		conf.setMapping( "risk-configurations", "RiskConf" );
		
		GDomConfig.setGlobalConf( conf );
		
//		{ // Example for defining a read-only role
//			ORole visitor = security.createRole( "Guest", ALLOW_MODES.DENY_ALL_BUT );
//			// BEGIN: copied form OSecurityShared - procedure to create the default "reader" role
//			visitor.addRule(ORule.ResourceGeneric.DATABASE, null, ORole.PERMISSION_READ);
//			visitor.addRule(ORule.ResourceGeneric.SCHEMA, null, ORole.PERMISSION_READ);
//			visitor.addRule(ORule.ResourceGeneric.CLUSTER, OMetadataDefault.CLUSTER_INTERNAL_NAME, ORole.PERMISSION_READ);
//			visitor.addRule(ORule.ResourceGeneric.CLUSTER, "orole", ORole.PERMISSION_READ);
//			visitor.addRule(ORule.ResourceGeneric.CLUSTER, "ouser", ORole.PERMISSION_READ);
//			visitor.addRule(ORule.ResourceGeneric.CLASS, null, ORole.PERMISSION_READ);
//			visitor.addRule(ORule.ResourceGeneric.CLUSTER, null, ORole.PERMISSION_READ);
//			visitor.addRule(ORule.ResourceGeneric.COMMAND, null, ORole.PERMISSION_READ);
//			visitor.addRule(ORule.ResourceGeneric.RECORD_HOOK, null, ORole.PERMISSION_READ);
//			visitor.addRule(ORule.ResourceGeneric.FUNCTION, null, ORole.PERMISSION_READ);
//		}
		
	}
	
	@Override
	public void setRoleProperty( String roleName, String key, String value ) {
		container.setRoleProperty(roleName, key, value);
	}
	
	@Override
	public String getRoleProperty( String roleName, String key, String def ) {
		return container.getRoleProperty(roleName, key, def);
	}
	
	@Override
	public List<String> listRoles() {
		return container.listRoles();
	}
	
	@Override
	public List<String> listUsers( String from, String max, String pattern ) {
		return container.listUsers(from, max, pattern);
	}
	
	@Override
	public List<String> listPublicDomains() {
		return container.listPublicDomains();
	}

	@Override
	public void setPredefinedRole( String domain, String value ) {
		GAuthDom auth = new GAuthDom( container.getDom( domain ) );
		auth.setPredefinedRole( value);
	}

	@Override
	public String getPredefinedRole( String domain ) {
		GAuthDom auth = new GAuthDom( container.getDom( domain ) );
		return auth.getPredefinedRole();
	}

	@Override
	public void createRole( String roleName ) {
		container.createRole( roleName );
	}

	@Override
	public List<String> listDomains( String username ) {
		return container.listDomains( username );
	}

	@Override
	public boolean isAdmin() {
		// FIXME: should check actual DB permissions rather that hardcoded username
		return "admin".equals( getUsername() );
	}

	@Override
	public SiteManager getSiteManager() {
		return new ORiscossSiteManager( container.getGraph() );
	}

	@Override
	public boolean existsDomain( String domain ) {
		return container.containsDomain( domain );
	}
	
}
