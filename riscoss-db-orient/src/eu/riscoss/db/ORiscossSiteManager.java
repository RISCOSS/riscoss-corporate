package eu.riscoss.db;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.ORule.ResourceGeneric;
import com.orientechnologies.orient.core.metadata.security.OSecurityRole;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;

import eu.riscoss.db.domdb.GAuthDom;
import eu.riscoss.db.domdb.GDomConfig;
import eu.riscoss.db.domdb.GDomDB;
import eu.riscoss.db.domdb.NodeID;

public class ORiscossSiteManager implements SiteManager {
	
	static final String CLASSNAME = "Sitemap";
	
	static class SiteMapConf extends GDomConfig {
		
		public String getClass( String tag ) {
			return "";
		}
		
		public String getRootClass() {
			return CLASSNAME;
		}
		
	}
	
	GDomDB			dom;
	
	public ORiscossSiteManager(OrientBaseGraph graph) {
		dom = new GDomDB( new SiteMapConf(), graph, CLASSNAME);
	}
	
	public void init() {
//		dom.execute( "create class SitemapNode extends V" );
//		dom.execute( "alter class SitemapNode extends V, ORestricted" );
	}
	
	@Override
	public void createPage( String sectionPathAndName, String label, String url, String ... roles ) {
		NodeID id = dom.create( sectionPathAndName + "/" + label );
		dom.setAttribute( id, "url", url );
		dom.setAttribute( id, "type", "page" );
		
		// Custom access management
		HashSet<String> set = new HashSet<>();
		for( String role : roles ) set.add( role );
		dom.setAttribute( id, "authorizedRoles", new Gson().toJson( set ) );
		
//		// DB-based access management
//		for( String rolename : roles ) {
//			ORole role = dom.graph.getRawGraph().getMetadata().getSecurity().getRole( rolename );
//			if( role == null ) continue;
//			dom.execute( "update " + id + " add _allowRead = " + role.getIdentity() );
////			dom.execute( "update " + id + " put rules = 'database.class.SitemapNode', 0" );
//		}
		
//		GAuthDom auth = new GAuthDom( dom );
//		for( String rolename : roles )
//			auth.setGlobalNodePermission( rolename, sectionPathAndName + "/" + label, "r" );
	}
	
	@Override
	public void createSection( String sectionPathAndName ) {
		NodeID id = dom.create( sectionPathAndName );
		dom.setAttribute( id, "type", "section" );
	}
	
	@Override
	public List<String> listSections( String sectionPathAndName ) {
		NodeID id = dom.get( sectionPathAndName );
		if( id == null ) return new ArrayList<>();
		return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null, "in.type='section'" );
	}
	
	@Override
	public List<String> listPages( String sectionPathAndName ) {
		NodeID id = dom.get( sectionPathAndName );
		if( id == null ) return new ArrayList<>();
		return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null, "in.type='page'" );
	}
	
	@Override
	public Collection<String> listRoles( String path ) {
//		String path = getPath( url );
		if( path == null ) return new ArrayList<>();
		NodeID id = dom.get( path );
		if( id == null ) return new ArrayList<>();
		String val = dom.getAttribute( id, "authorizedRoles", null );
		if( val == null ) return new ArrayList<>();
		try {
			Type type = new TypeToken<HashSet<String>>() {}.getType();
			HashSet<String> set = new Gson().fromJson( val, type );
			return set;
		}
		catch( Exception ex ) {
			return new ArrayList<String>();
		}
	}

	@Override
	public String getUrl( String path ) {
		
		NodeID id = dom.get( path );
		if( id == null ) return null;
		try {
			String val = dom.getAttribute( id, "url", null );
			if( val == null ) return null;
			if( "".equals( val ) ) return null;
			return val;
		}
		catch( Exception ex ) {
			return null;
		}
	}

	@Override
	public boolean isAllowed( String path, String domain ) {
		
		NodeID id = dom.get( path );
		if( id == null ) return false;
		try {
			// Use embedded security mechanism:
			
			Collection<String> authorizedRoles = listRoles( path );
			if( authorizedRoles.size() < 1 ) return true;
			
			String rolename = extractRole( domain );
			
			for( String authorizedRole : authorizedRoles ) {
				if( authorizedRole.equals( rolename ) ) {
					return true;
				}
			}
			
			return isSuperAdmin();
			
//			String val = dom.getAttribute( id, "url", null );
//			if( val == null ) return false;
//			if( "".equals( val ) ) return false;
//			return true;
		}
		catch( Exception ex ) {
			return false;
		}
	}

	private boolean isSuperAdmin() {
		
		try {
			Set<? extends OSecurityRole> roles = dom.getGraph().getRawGraph().getUser().getRoles();
			
			for( OSecurityRole role : roles ) {
				if( role.hasRule( ResourceGeneric.BYPASS_RESTRICTED, null ) ) {
					return true;
				}
			}
			
			return false;
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			return false;
		}
	}

	private String extractRole( String domainname ) {
		
		try {
			String username = dom.getGraph().getRawGraph().getUser().getName();
			
			String q = "select from (select expand(roles) from (select from ouser where name = '" + username + "')) where domain.tag='" + domainname + "'";
			
			List<ODocument> docs = dom.querySynch( q );
			
			if( docs == null ) return "";
			if( docs.size() < 1 ) return "";
			
			ORole role = dom.getGraph().getRawGraph().getMetadata().getSecurity().getRole( (String)docs.get( 0 ).field( "name" ) );
			
			return role.getParentRole().getName();
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			return "";
		}
	}

	@Override
	public boolean isUrlAllowed( String url, String domain ) {
		
		String path = getPath( url );
		
		if( path == null ) return false;
		
		return isAllowed( path, domain );
	}

	public String getPath( String url ) {
		
		List<ODocument> docs = dom.querySynch( "select from " + dom.getClassName( "" ) + " where url='" + url + "'" );
		
		if( docs == null ) return null;
		if( docs.size() < 1 ) return null;
		
		return dom.getPath( new NodeID( docs.get( 0 ).getIdentity().toString() ) );
		
//		return docs.get( 0 ).field( "url" );
	}

	@Override
	public void createRole( String name ) {
		GAuthDom auth = new GAuthDom( dom );
		auth.createRole( name );
		auth.setPermission( name, "SiteMap", "" );
	}
	
}
