package eu.riscoss.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.RiscossDBResource;
import eu.riscoss.db.RiscossDatabase;
import eu.riscoss.shared.DBResource;
import eu.riscoss.shared.JDomainInfo;
import eu.riscoss.shared.JRoleInfo;
import eu.riscoss.shared.JUserInfo;
import eu.riscoss.shared.KnownRoles;
import eu.riscoss.shared.Pair;

@Path("admin")
public class AdminManager {
	
	Gson gson = new Gson();
	
	@GET @Path("/roles/list")
	public String listRoles() {
		
		RiscossDatabase db = null;
		
		try {
			db = DBConnector.openDatabase( null, null );
			JsonArray array = new JsonArray();
			for( String roleName : db.listRoles() ) {
				if( roleName != null )
					array.add( new JsonPrimitive( roleName ) );
			}
			return array.toString();
		}
		finally {
			if( db != null )
				db.close();
		}
	}
	
	@GET @Path("/domains/list")
	public String listDomains(
			@DefaultValue("") @HeaderParam("token") String token,
			@DefaultValue("") @QueryParam("username") String username ) {
		
		Set<String> set = new HashSet<>();
//		JsonArray array = new JsonArray();
		{
			RiscossDatabase db = null;
			
			try {
				db = DBConnector.openDatabase( null, null );
				
				for( String domain : db.listDomains() ) {
					if( domain != null )
						set.add( domain );
//						array.add( new JsonPrimitive( domain ) );
				}
			}
			finally {
				if( db != null )
					db.close();
			}
		}
		
		if( token.length() > 1 ) {
			RiscossDatabase db = null;
			if( !username.equals("") ) try {
				db = DBConnector.openDatabase( token );
				
				if( db.getUsername().equals( username ) ) {
					for( String domain : db.listDomains( username ) ) {
						if( domain != null )
							set.add( domain );
//							array.add( new JsonPrimitive( domain ) );
					}
				}
			}
			finally {
				if( db != null )
					db.close();
			}
		}
		
		return gson.toJson( set ).toString();
//		return array.toString();
		
	}
	
	@POST @Path("/domains/create")
	public String createDomain( @HeaderParam("token") String token, @QueryParam("name") String name ) {
		
		RiscossDatabase db = null;
		
		try {
			db = DBConnector.openDatabase( token );
			db.createDomain( name );
			RiscossDB domainDB = DBConnector.openDB( name, token );
			for( KnownRoles r : KnownRoles.values() ) {
				domainDB.createRole( r.name() );
				for( Pair<DBResource,String> perm : r.permissions() ) {
					domainDB.addPermissions( r.name(), RiscossDBResource.valueOf( perm.getLeft().name() ), perm.getRight() );
				}
			}
			domainDB.close();
			return new JsonPrimitive( name ).toString();
		}
		finally {
			if( db != null )
				db.close();
		}
	}
	
	@POST @Path("/{domain}/roles/create")
	public String createRole( 
			@HeaderParam("token") String token,
			@DefaultValue("Playground") @PathParam("domain") String domain, 
			@QueryParam("name") String name,
			@DefaultValue("Guest") String parentRole ) {
		
		RiscossDB db = DBConnector.openDB( token, domain );
		
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
	
	@GET @Path("/{domain}/roles/list")
	public String listRoles( 
			@HeaderParam("token") String token,
			@DefaultValue("Playground") @PathParam("domain") String domain ) {
		
		RiscossDB db = DBConnector.openDB( token, domain );
		
		try {
			
			JsonArray array = new JsonArray();
			
			List<String> roles = db.listRoles( domain );
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
	}
	
	@GET @Path("/users/list")
	public String listUsers( 
			@HeaderParam("token") String token,
			@DefaultValue("0") @QueryParam("from") String from, 
			@DefaultValue("100") @QueryParam("max") String max,
			@DefaultValue("") @QueryParam("pattern") String pattern ) {
		
		RiscossDatabase db = null;
		
		try {
			db = DBConnector.openDatabase( token );
			
			JsonArray array = new JsonArray();
			
			List<String> users = db.listUsers( from, max, pattern );
			for( String user : users ) {
				if( "admin".equals( user ) ) continue;
				if( "reader".equals( user ) ) continue;
				if( "writer".equals( user ) ) continue;
				JUserInfo info = new JUserInfo( user );
				array.add( gson.toJsonTree( info ) );
			}
			
			return array.toString();
			
		}
		finally {
			if( db != null )
				db.close();
		}
	}
	
	@GET @Path("/users/{user}/info")
	public String getUserInfo(
			@HeaderParam("token") String token,
			@PathParam("user") String user
			) {
		return "";
	}
	
	@DELETE @Path("/users/{user}/delete")
	public void deleteUser(
			@HeaderParam("token") String token,
			@PathParam("user") String username
			) {
		
		OrientGraphNoTx graph = new OrientGraphFactory( DBConnector.db_addr ).getNoTx();
		
		try {
			OSecurity security = graph.getRawGraph().getMetadata().getSecurity();
			
			security.dropUser( username );
			
//			return new JsonPrimitive( "Ok" ).toString();
		}
		finally {
			if( graph != null )
				graph.getRawGraph().close();
		}
	}
	
	@POST @Path("/{domain}/users/{user}/set")
	public String setUser(
			@HeaderParam("token") String token,
			@PathParam("domain") String domain,
			@PathParam("user") String user,
			@QueryParam("role") String role ) {
		
		RiscossDB db = null;
		
		try {
			db = DBConnector.openDB( domain, token );
			
			db.setUserRole( user, role );
			
			return gson.toJson( new JUserInfo( user ) ).toString();
			
		}
		finally {
			if( db != null ) 
				db.close();
		}
	}
	
	@GET @Path("/{domain}/users/list")
	public String listUsers(
			@HeaderParam("token") String token,
			@PathParam("domain") String domain ) {
		
		RiscossDB db = null;
		
		try {
			db = DBConnector.openDB( domain, token );
			JsonArray array = new JsonArray();
			for( String user : db.listUsers() ) {
				JUserInfo info = new JUserInfo( user );
				array.add( gson.toJsonTree( info ) );
			}
			
			return array.toString();
			
		}
		finally {
			if( db != null ) 
				db.close();
		}
	}
	
	@GET @Path("/{domain}/info")
	public String getDomainIndo(
			@HeaderParam("token") String token,
			@PathParam("domain") String domain ) {
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( token );
			
			JDomainInfo dinfo = new JDomainInfo();
			
			dinfo.name = domain;
			dinfo.predefinedRole = db.getPredefinedRole( domain );
			
			return gson.toJson( dinfo ).toString();
			
		}
		finally {
			if( db != null )
				db.close();
		}
		
//		return "";
	}
	
	@GET @Path("/domains/public")
	public String listAvailableDomains(
			@HeaderParam("token") String token, @DefaultValue("") @QueryParam("username") String username ) {
		
		return gson.toJson( listAvailableUserDomains(token, username ) ).toString();
		
	}
	
	public static Collection<String> listAvailableUserDomains( String token, String username ) {
		
		Set<String> set = new HashSet<>();
		{
			RiscossDatabase db = null;
			
			try {
				db = DBConnector.openDatabase( null, null );
				
				for( String domain : db.listPublicDomains() ) {
					if( domain != null )
						set.add( domain );
				}
			}
			finally {
				if( db != null )
					db.close();
			}
		}
		
		if( token.length() > 1 ) {
			RiscossDatabase db = null;
			if( !"".equals( username ) ) try {
				db = DBConnector.openDatabase( token );
				
				if( db.getUsername().equals( username ) ) {
					for( String domain : db.listDomains( username ) ) {
						if( domain != null )
							set.add( domain );
					}
				}
			}
			finally {
				if( db != null )
					db.close();
			}
		}
		
		return set;
		
	}
	
	@POST @Path("/{domain}/default-role")
	public void setPredefinedRole( 
			@HeaderParam("token") String token,
			@PathParam("domain") String domain,
			@QueryParam("role") String value ) {
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( token );
			
			db.setPredefinedRole( domain, value );
			
		}
		finally {
			if( db != null )
				db.close();
		}
	}
	
	@POST @Path("/{domain}/domains/selected")
	public String setSessionSelectedDomain( @PathParam("domain") String domain,
			//@Context HttpServletRequest req, 
			@HeaderParam("token") String token) {
		
		if( domain == null ) return null;
		
		RiscossDatabase db = null;
		
		try {
			db = DBConnector.openDatabase( token );
			
			if( db.isAdmin() ) 
				return new JsonPrimitive( domain ).toString();
			
			String username = db.getUsername();
			
			Collection<String> domains = AdminManager.listAvailableUserDomains( token, username );
			
			for( String d : domains ) {
				if( d.equals( domain ) ) {
					
					RiscossDB domaindb = DBConnector.openDB( domain, token );
					
					String rolename = domaindb.getRole( username );
					
					if( rolename == null ) {
						domaindb.setUserRole( username, db.getPredefinedRole( domain ) );
					}
					
					domaindb.close();
					
					return new JsonPrimitive( domain ).toString();
				}
			}
			
			throw new RuntimeException( "Invalid domain" );
		}
		finally {
			if( db != null )
				db.close();
		}
		
//		String initString = req.getServletContext().getInitParameter( "eu.riscoss.param.domains.list" );
//		if( initString == null ) {
//			// If not domains configured, proceed
//			return new JsonPrimitive( DBConnector.DEFAULT_DOMAIN ).toString();
//		}
//		
//		if( domain == null ) {
//			return null;
//		}
//		
//		String[] tokens = initString.split( "[,]" );
//		for( String tok : tokens ) {
//			if( tok.equals( domain ) ) return new JsonPrimitive( domain ).toString();
//		}
//		
////		req.getSession( true ).setAttribute( "domain", domain );
////		DBConnector.setThreadLocalValue( CookieNames.DOMAIN_KEY, domain );
//		
//		throw new RuntimeException( "Invalid domain" );
//		
////		return new JsonPrimitive("Invalid domain").toString();
	}
	
}
