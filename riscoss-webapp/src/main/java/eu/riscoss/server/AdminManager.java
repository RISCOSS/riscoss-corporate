package eu.riscoss.server;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

import eu.riscoss.db.RiscossDB;
import eu.riscoss.db.RiscossDBResource;
import eu.riscoss.db.RiscossDatabase;
import eu.riscoss.db.SiteManager;
import eu.riscoss.shared.DBResource;
import eu.riscoss.shared.JDomainInfo;
import eu.riscoss.shared.JRoleInfo;
import eu.riscoss.shared.JSiteMap;
import eu.riscoss.shared.JSiteMap.JSitePage;
import eu.riscoss.shared.JSiteMap.JSiteSection;
import eu.riscoss.shared.JUserInfo;
import eu.riscoss.shared.KnownRoles;
import eu.riscoss.shared.Pair;

@Path("admin")
@Info("Administration")
public class AdminManager {
	
	Gson gson = new Gson();
	
	int counter = 0;
	
	@GET @Path("/{domain}/sitemap")
	@Info("Returns the map of the available UI pages")
	public String getSitemap( 
			@HeaderParam("token") @Info("The authentication token") String token, 
			@PathParam("domain") @Info("The selected domain") String domain ) throws Exception {
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( token );
			
			JSiteMap sitemap = new JSiteMap();
			
			sitemap.domain = domain;
			
			SiteManager sm = db.getSiteManager();
			
			sitemap.main = loadSection( "", "", sm, domain );
			
			return new Gson().toJson( sitemap );
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			
			if( db != null )
				DBConnector.closeDB( db );
		}
	}
	
	private JSiteSection loadSection( String sectionPath, String sectionName, SiteManager sm, String domain ) {
		
		counter++;
		if( counter > 3 ) {
			counter--;
			return new JSiteSection();
		}
		
		JSiteSection section = new JSiteSection( sectionName );
		
		for( String pagename : sm.listPages( sectionPath + "/" + sectionName ) ) {
			String url = sm.getUrl( sectionPath + "/" + sectionName + "/" + pagename );
			if( !sm.isAllowed( sectionPath + "/" + sectionName + "/" + pagename, domain ) ) continue;
			section.add( new JSitePage( pagename, url ) );
		}
		
		for( String sect : sm.listSections( sectionPath + "/" + sectionName ) ) {
			section.add( loadSection( sectionPath + "/" + sectionName, sect, sm, domain ) );
		}
		
		counter--;
		
		return section;
	}

	@GET @Path("/roles/list")
	@Info("Returns the list of registered Roles")
	public String listRoles() throws Exception {
		
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
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/domains/create")
	@Info("Creates a new domain")
	public String createDomain( 
			@HeaderParam("token") @Info("The authentication token") String token, 
			@QueryParam("name") @Info("The name of the new domain") String name ) throws Exception {
		
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
			DBConnector.closeDB( domainDB );
			return new JsonPrimitive( name ).toString();
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("{domain}/delete")
	@Info("Deletes a domain")
	public void deleteDomain( 
			@HeaderParam("token") @Info("The authentication token") String token,
			@PathParam("domain") @Info("The selected domain") String domain) throws Exception {
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( token );
			db.deleteDomain( domain );
			
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/roles/create")
	@Info("Creates a new Role")
	public String createRole( 
			@HeaderParam("token") @Info("The authentication token") String token,
			@PathParam("domain") @Info("The selected domain") String domain, 
			@QueryParam("name") @Info("Name of the new Role") String name,
			@DefaultValue("Guest") @Info("Type") String parentRole ) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( token, domain );
			
			db.createRole( name );
			
			JRoleInfo info = new JRoleInfo( name );
			
			return gson.toJson( info );
			
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/roles/list")
	@Info("Returns the list of available roles in a given domain")
	public String listRoles( 
			@HeaderParam("token") @Info("The authentication token") String token,
			@PathParam("domain") @Info("The selected domain") String domain ) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( token, domain );
			
			JsonArray array = new JsonArray();
			
			List<String> roles = db.listRoles( domain );
			for( String role : roles ) {
				JRoleInfo info = new JRoleInfo( role );
				array.add( gson.toJsonTree( info ) );
			}
			
			return array.toString();
			
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/users/list")
	@Info("Returns the list of registered users")
	public String listUsers( 
			@HeaderParam("token") @Info("The authentication token") String token,
			@DefaultValue("0") @Info("Index of the first item to return; for pagination support") @QueryParam("from") String from, 
			@DefaultValue("100") @Info("Number of items to return; for pagination support") @QueryParam("max") String max,
			@DefaultValue("") @Info("Search pattern") @QueryParam("pattern") String pattern ) throws Exception {
		
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
			
		} catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/users/{user}/info")
	@Info("Returns details about a user")
	public String getUserInfo(
			@HeaderParam("token") @Info("The authentication token") String token,
			@PathParam("user") @Info("User name") String user
			) {
		return "";
	}
	
	@DELETE @Path("/users/{user}/delete")
	@Info("Deletes completely a user account from every domain and from the database")
	public void deleteUser(
			@HeaderParam("token") @Info("The authentication token") String token,
			@PathParam("user") @Info("Name of the user dto be deleted") String username
			) {
		
		OrientGraphNoTx graph = new OrientGraphFactory( DBConnector.db_addr ).getNoTx();
		
		try {
			
			OSecurity security = graph.getRawGraph().getMetadata().getSecurity();
			
			security.dropUser( username );
			
		}
		finally {
			
			if( graph != null )
				graph.getRawGraph().close();
		}
	}
	
	@POST @Path("/{domain}/users/{user}/set")
	@Info("Sets the role of a user in a domain")
	public String setUser(
			@HeaderParam("token") @Info("The authentication token") String token,
			@PathParam("domain") @Info("The selected domain") String domain,
			@PathParam("user") @Info("Name of the user to be set") String user,
			@QueryParam("role") @Info("Role name") String role ) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			
			db.setUserRole( user, role );
			
			return gson.toJson( new JUserInfo( user ) ).toString();
			
		} catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/users/list")
	@Info("Returns the list of users subscribed to a certain domain")
	public String listUsers(
			@HeaderParam("token") @Info("The authentication token") String token,
			@PathParam("domain") @Info("The selected domain") String domain ) throws Exception {
		
		RiscossDB db = null;
		
		try {
			
			db = DBConnector.openDB( domain, token );
			JsonArray array = new JsonArray();
			for( String user : db.listUsers() ) {
				JUserInfo info = new JUserInfo( user );
				array.add( gson.toJsonTree( info ) );
			}
			
			return array.toString();
			
		} catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@GET @Path("/{domain}/info")
	@Info("Returns details about a domain")
	public String getDomainIndo(
			@HeaderParam("token") @Info("The authentication token") String token,
			@PathParam("domain") @Info("The selected domain") String domain ) throws Exception {
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( token );
			
			JDomainInfo dinfo = new JDomainInfo();
			
			dinfo.name = domain;
			dinfo.predefinedRole = db.getPredefinedRole( domain );
			
			return gson.toJson( dinfo ).toString();
			
		} catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( db );
		}
		
	}
	
	/**
	 * Returns the list of domains available to a specific user
	 * i.e., the list of public domains, plus the list of domain which have been granted to the user by an admin
	 * 
	 * @param token
	 * @param username
	 * @return
	 * @throws Exception 
	 */
	@GET @Path("/domains/public")
	@Info("Returns the list of domains available to a specific user; i.e., the list of public domains, plus the list of domain which have been granted to the user by an admin")
	public String listAvailableDomains(
			@HeaderParam("token") @Info("The authentication token") String token, 
			@QueryParam("username") @Info("The user name") String username ) throws Exception {
		
		return gson.toJson( listAvailableUserDomains(token, username ) ).toString();
		
	}
	
	@GET @Path("/domains/list")
	@Info("Returns the list of all domains")
	public String listDomains(
			@HeaderParam("token") @Info("The authentication token") String token) throws Exception {
		
		return gson.toJson( listAllDomains(token) ).toString();
	}
	
	public static Collection<String> listAllDomains(String token) throws Exception {
		Set<String> set = new HashSet<>();
		{
			RiscossDatabase db = null;
			
			try {
				db = DBConnector.openDatabase(null,null);
				for (String domain : db.listDomains()) {
					if (domain != null)
						set.add(domain);
				}
			} catch (Exception e) {
				throw e;
			} finally {
				DBConnector.closeDB(db);
			}
		}
		return set;
	}
	
	public static Collection<String> listAvailableUserDomains( String token, String username ) throws Exception {
		
		Set<String> set = new HashSet<>();
		{
			RiscossDatabase db = null;
			
			try {
				db = DBConnector.openDatabase( null, null );
				
				if( username == null ) username = db.getUsername();
				
				for( String domain : db.listPublicDomains() ) {
					if( domain != null )
						set.add( domain );
				}
			} catch (Exception e) {
				throw e;
			}
			finally {
				DBConnector.closeDB( db );
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
			} catch (Exception e) {
				throw e;
			}
			finally {
				DBConnector.closeDB( db );
			}
		}
		
		return set;
		
	}
	
	@POST @Path("/{domain}/default-role")
	@Info("Returns the default role of a domain. The default role is the role, which is assigned to a user, the first time that the user subscribes to the domain.")
	public void setPredefinedRole( 
			@HeaderParam("token") @Info("The authentication token") String token,
			@PathParam("domain") @Info("The selected domain") String domain,
			@QueryParam("role") @Info("The role name") String value ) throws Exception {
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( token );
			
			db.setPredefinedRole( domain, value );
			
		} catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST @Path("/{domain}/users/{user}/delete")
	@Info("Deletes the role from user in specific domain")
	public void removeUserFromDomain(
			@HeaderParam("token") @Info("The authentication token") String token,
			@PathParam("domain") @Info("The selected domain") String domain,
			@PathParam("user") @Info("The user name") String user
			) throws Exception {
		
		RiscossDB domaindb = null;
		
		try {
			
			domaindb = DBConnector.openDB( domain, token );
			
			domaindb.removeUserFromDomain(user);
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( domaindb );
		}
	}
	
	@POST @Path("/{domain}/users/{user}/role")
	@Info("Sets the role of a user in a given domain")
	public void setUserRole(
			@HeaderParam("token") @Info("The authentication token") String token,
			@PathParam("domain") @Info("The selected domain") String domain,
			@PathParam("user") @Info("The user name") String user,
			@QueryParam("role") @Info("The role name") String role
			) throws Exception {
		
		RiscossDB domaindb = null;
		
		try {
			
			domaindb = DBConnector.openDB( domain, token );
			
			domaindb.setUserRole( user, role );
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( domaindb );
		}
	}
	
	@GET @Path("/{domain}/users/{user}/role")
	@Info("Gets the role of a user in a given domain")
	public String getUserRole(
			@HeaderParam("token") @Info("The authentication token") String token,
			@PathParam("domain") @Info("The selected domain") String domain,
			@PathParam("user") @Info("The user name") String user
			) throws Exception {
		
		RiscossDB domaindb = null;
		
		try {
			
			domaindb = DBConnector.openDB( domain, token );
		
			return gson.toJson(domaindb.getRole(user));
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( domaindb );
		}
	}
	
	@POST @Path("/{domain}/domains/selected")
	@Info("Checks whether the user is authorized to access a given domain")
	public String setSessionSelectedDomain( 
			@PathParam("domain") @Info("The selected domain") String domain, 
			@HeaderParam("token") @Info("The authentication token") String token) throws Exception {
		
		if( domain == null ) return null;
		
		RiscossDatabase db = null;
		
		try {
			
			db = DBConnector.openDatabase( token );
			
			if( db.isAdmin() ) {
				if( db.existsDomain( domain ) )
					return new JsonPrimitive( domain ).toString();
				else
					return null;
			}
			
			String username = db.getUsername();
			
			Collection<String> domains = AdminManager.listAvailableUserDomains( token, username );
			
			for( String d : domains ) {
				if( d.equals( domain ) ) {
					
					RiscossDB domaindb = null;
					
					try {
						domaindb = DBConnector.openDB( domain, token );
						
						String rolename = domaindb.getRole( username );
						
						if( rolename == null ) {
							domaindb.setUserRole( username, db.getPredefinedRole( domain ) );
						}
					}
					catch (Exception e) {
						throw e;
					}
					finally {
						DBConnector.closeDB( domaindb );
					}
					
					return new JsonPrimitive( domain ).toString();
				}
			}
			
			return null;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
}
