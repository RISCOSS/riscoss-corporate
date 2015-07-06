package eu.riscoss.server;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import eu.riscoss.db.RiscossDB;
import eu.riscoss.shared.JRoleInfo;
import eu.riscoss.shared.JUserInfo;

@Path("admin")
public class SecurityManager {
	
	Gson gson = new Gson();
	
	private static final SecurityManager instance = new SecurityManager();

	public User getUser( HttpServletRequest req ) {
		HttpSession session = req.getSession( true );
		User user = (User)session.getAttribute( "eu.riscoss.session.data" );
		if( user == null ) {
			return User.noUser;
		}
		return user;
//		String token = data.token;
//		String username = data.username;
//		String password = data.password;
	}
	
	public static SecurityManager get() {
		return instance;
	}
	
	public boolean canAccess( User user, String requestURI ) {
		return true;
	}
	
	@POST @Path("roles/create")
	public String createRole( @QueryParam("name") String name ) {
		
		RiscossDB db = DBConnector.openDB();
		
		try {
			
			db.createRole( name );
			
			JRoleInfo info = new JRoleInfo( name );
			
			return gson.toJson( info );
			
		}
		finally {
			if( db != null )
				DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("roles/list")
	public String listRoles() {
		
		RiscossDB db = DBConnector.openDB();
		
		try {
			
			JsonArray array = new JsonArray();
			
			List<String> roles = db.listRoles();
			for( String role : roles ) {
				JRoleInfo info = new JRoleInfo( role );
				array.add( gson.toJsonTree( info ) );
			}
			
			return array.toString();
			
		}
		finally {
			if( db != null )
				DBConnector.closeDB( db );
		}
		
//		return "";
	}
	
	@GET @Path("users/list")
	public String listUsers() {
		
		RiscossDB db = DBConnector.openDB();
		
		try {
			
			JsonArray array = new JsonArray();
			
			List<String> users = db.listUsers();
			for( String user : users ) {
				JUserInfo info = new JUserInfo( user );
				array.add( gson.toJsonTree( info ) );
			}
			
			return array.toString();
			
		}
		finally {
			if( db != null )
				DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("users/create")
	public void createUser( 
			@Context HttpServletRequest req,
			@HeaderParam("firstName") String firstName,
			@HeaderParam("lastName") String lastName,
			@HeaderParam("username") String username,
			@HeaderParam("password") String password ) {
		
	}
	
	public void execute( String cmd ) {
//		graph.getRawGraph().command(new OCommandSQL(cmd)).execute();
	}
	
	public void createRole2( String name ) {
		execute( "INSERT INTO orole SET name = '" + name + "', mode = 0" );
//		execute( "UPDATE orole SET inheritedRole = (SELECT FROM orole WHERE name = 'writer') WHERE name = 'appuser'" );
	}
}
