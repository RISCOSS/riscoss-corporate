package eu.riscoss.server;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.metadata.security.OSecurityRole;
import com.orientechnologies.orient.core.metadata.security.OSecurityUser;
import com.orientechnologies.orient.server.config.OServerParameterConfiguration;
import com.orientechnologies.orient.server.token.OrientTokenHandler;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

import eu.riscoss.db.RiscossDatabase;

@Path("auth")
public class AuthManager {
	
	private static final OServerParameterConfiguration[] I_PARAMS = new OServerParameterConfiguration[] { 
		new OServerParameterConfiguration( OrientTokenHandler.SIGN_KEY_PAR, "any key")
	};
	
	@POST @Path("/login")
	public String login( @HeaderParam("username") String username, @HeaderParam("password") String password ) throws Exception {
		
		OrientGraphNoTx graph = new OrientGraphFactory( DBConnector.db_addr, username, password ).getNoTx();
		
//		String token = getStringToken( graph );
//		
//		graph.getRawGraph().close();
//		
//		DBConnector.openDatabase( token ).close();
		
		try {
			return new JsonPrimitive( getStringToken( graph ) ).toString();
//			return new JsonPrimitive( token ).toString();
		}
		finally {
			if( graph != null )
				graph.getRawGraph().close();
		}
	}
	
	@GET @Path("token")
	public String checkToken( @HeaderParam("token") String token ) {
		System.out.println( "Received token: " + token );
		DBConnector.openDatabase( token ).close();
		return new JsonPrimitive( "Ok" ).toString();
	}
	
	@POST @Path("register")
	public String register( @HeaderParam("username") String username, @HeaderParam("password") String password ) {
		
		OrientGraphNoTx graph = new OrientGraphFactory( DBConnector.db_addr ).getNoTx();
		
		try {
			OSecurity security = graph.getRawGraph().getMetadata().getSecurity();
			
			ORole guest = security.getRole( "Guest" );
			
			if( guest == null ) {
				guest = security.createRole( "Guest", OSecurityRole.ALLOW_MODES.ALLOW_ALL_BUT );
			}
			
			security.createUser( username, password, guest );
			
			// Already return the login token?
//			graph.getRawGraph().close();
//			
//			graph = new OrientGraphFactory( DBConnector.db_addr, username, password ).getNoTx();
//			
//			return new JsonPrimitive( getStringToken( graph ) ).toString();
			
			return new JsonPrimitive( "Ok" ).toString();
		}
		finally {
			if( graph != null )
				graph.getRawGraph().close();
		}
	}
	
	String getStringToken( OrientBaseGraph graph ) {
		OSecurityUser original = graph.getRawGraph().getUser();
		OrientTokenHandler handler = new OrientTokenHandler();
		handler.config(null, I_PARAMS);
		byte[] token = handler.getSignedWebToken( graph.getRawGraph(), original );
		
		return Base64.encodeBase64String( token );
	}
	
	@GET @Path("/username")
	public String getUsername( @HeaderParam("token") String token ) {
		
		RiscossDatabase database = DBConnector.openDatabase( token );
		
		try {
			
			String username = database.getUsername();
			
			return new JsonPrimitive( username ).toString();
		}
		catch( Exception ex ) {
//			throw ex;
			return new JsonPrimitive( "Error" ).toString();
		}
		finally {
			if( database != null )
				database.close();
		}
		
	}
	
	@GET @Path("/domains/list")
	public String getAvailableDomains(  @Context HttpServletRequest req ) {
		
		JsonArray array = new JsonArray();
		
		String initString = req.getServletContext().getInitParameter( "eu.riscoss.param.domains.list" );
		
		if( initString != null ) {
			String[] tokens = initString.split( "[,]" );
			for( String tok : tokens ) {
				array.add( new JsonPrimitive( tok ) );
			}
		}
		
		return array.toString();
	}
	
	@POST @Path("/domains/selected")
	public String setSessionSelectedDomain( @Context HttpServletRequest req, @QueryParam("domain") String domain ) {
		
		System.out.println( "[SERVER] selected domain " + domain );
		
		String initString = req.getServletContext().getInitParameter( "eu.riscoss.param.domains.list" );
		if( initString == null ) {
			// If not domains configured, proceed
			return new JsonPrimitive( DBConnector.DEFAULT_DOMAIN ).toString();
		}
		
		if( domain == null ) {
			return null;
		}
		
		String[] tokens = initString.split( "[,]" );
		for( String tok : tokens ) {
			if( tok.equals( domain ) ) return new JsonPrimitive( domain ).toString();
		}
		
//		req.getSession( true ).setAttribute( "domain", domain );
//		DBConnector.setThreadLocalValue( CookieNames.DOMAIN_KEY, domain );
		
		throw new RuntimeException( "Invalid domain" );
		
//		return new JsonPrimitive("Invalid domain").toString();
	}
	
}
