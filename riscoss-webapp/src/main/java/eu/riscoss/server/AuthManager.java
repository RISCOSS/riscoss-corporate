package eu.riscoss.server;


import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

import org.apache.commons.codec.binary.Base64;

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
import eu.riscoss.shared.KnownRoles;

@Path("auth")
@Info("Authentication and Authorization")
public class AuthManager {
	
	private static final OServerParameterConfiguration[] I_PARAMS = new OServerParameterConfiguration[] { 
		new OServerParameterConfiguration( OrientTokenHandler.SIGN_KEY_PAR, "any key"),
		new OServerParameterConfiguration( OrientTokenHandler.SESSION_LENGHT_PAR, "525600000" ) // ( 1000* 60 * 24 * 365 ) ) = 1 year
	};
	
	/**
	 * Logs in on the DB
	 * @param username
	 * @param password
	 * @return the new token
	 * @throws Exception
	 */
	@POST @Path("/login")
	@Info("This function authenticates the user and returns a token that can be reused (untile the session expires) to call functions that require authentication")
	public String login( 
			@HeaderParam("username") String username, 
			@HeaderParam("password") String password ) throws Exception {
		
//		System.out.println("#### DB address "+new File(DBConnector.db_addr).getAbsolutePath()+" ####");
		
		OrientGraphNoTx graph = new OrientGraphFactory( DBConnector.db_addr, username, password ).getNoTx();
		
		try {
			
			String token = getStringToken( graph );
			
//			System.out.println( "Login succeeded. Token:" );
//			System.out.println( token );
//			System.out.println( EncodingUtil.encrypt( username + "\n" + password ) );
			
			return new JsonPrimitive( token ).toString();
		}
		finally {
			if( graph != null )
				graph.getRawGraph().close();
		}
	}
	
	@GET @Path("token")
	@Info("This function performs a validity check of the token and return successfully if the token is correct and not expired")
	//TODO: change to POST?!
	public String checkToken( 
			@HeaderParam("token") String token
			) throws Exception {
		
		RiscossDatabase db = null;
		try {
			db = DBConnector.openDatabase( token );
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
		return new JsonPrimitive( "Ok" ).toString();
	}
	
	@POST @Path("/register")
	@Info("Registers a new user into the database")
	public String register(
			@HeaderParam("username") String username, 
			@HeaderParam("password") String password ) {
		
		OrientGraphNoTx graph = new OrientGraphFactory( DBConnector.db_addr ).getNoTx();
		
		try {
			OSecurity security = graph.getRawGraph().getMetadata().getSecurity();
			
			ORole guest = security.getRole( KnownRoles.Guest.name() );
			
			if( guest == null ) {
				guest = security.createRole( KnownRoles.Guest.name(), OSecurityRole.ALLOW_MODES.ALLOW_ALL_BUT );
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
	@Info("Returns the user name corresponding to the given token (if valid)")
	public String getUsername( 
			@HeaderParam("token") String token ) {
		
		RiscossDatabase database = null;
		
		try {
			
			database = DBConnector.openDatabase( token );
			
			String username = database.getUsername();
			
			return new JsonPrimitive( username ).toString();
		}
		catch( Exception ex ) {
			return new JsonPrimitive( "Error" ).toString();
		}
		finally {
			DBConnector.closeDB( database );
		}
		
	}
}
