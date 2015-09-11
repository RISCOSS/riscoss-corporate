package eu.riscoss.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orientechnologies.orient.core.metadata.OMetadataDefault;
import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.ORule;
import com.orientechnologies.orient.core.metadata.security.ORule.ResourceGeneric;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.metadata.security.OSecurityRole.ALLOW_MODES;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;

public class GAuthDom {
	
	static Map<String,Integer> map = new HashMap<>();
	
	static {
		map.put( "r", ORole.PERMISSION_READ );
		map.put( "w", ORole.PERMISSION_CREATE | ORole.PERMISSION_UPDATE | ORole.PERMISSION_DELETE );
		map.put( "*", ORole.PERMISSION_ALL );
		map.put( "", ORole.PERMISSION_NONE );
	}
	
	GDomDB dom;
	
	public GAuthDom( GDomDB db ) {
		this.dom = db;
	}
	
	public void execute( String cmd ) {
		dom.graph.getRawGraph().command(new OCommandSQL(cmd)).execute();
	}
	
	public List<ODocument> query( String query ) {
		try {
			List<ODocument> docs = dom.graph.getRawGraph().query( 
					new OSQLSynchQuery<ODocument>( query ) );
			return docs;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return new ArrayList<ODocument>();
		}
	}
	
	public String getRole() {
		
		// FIXME
		return dom.graph.getRawGraph().getMetadata().getSecurity().getRole( 
				dom.graph.getRawGraph().getUser().getName() ).getName();
		
	}
	
//	public void createRole( String roleName ) {
//		OSecurity security = dom.graph.getRawGraph().getMetadata().getSecurity();
//		if( security.getRole( roleName ) == null )
//			security.createRole( roleName, ALLOW_MODES.ALLOW_ALL_BUT ).getDocument().field( "type", "template" ).save();
//	}
	
//	private void requireRole( OSecurity security, String roleName ) {
//		if( security.getRole( roleName ) == null )
//			security.createRole( roleName, ALLOW_MODES.ALLOW_ALL_BUT ).getDocument().field( "type", "template" ).save();
//	}
//	
//	public void init() {
//		
//		OSecurity security = graph.getRawGraph().getMetadata().getSecurity();
//		
//		requireRole( security, "Administrator" );
//		requireRole( security, "Modeler" );
//		requireRole( security, "Producer" );
//		requireRole( security, "Consumer" );
//		requireRole( security, "Guest" );
//		
////		{ // Example for defining a read-only role
////			ORole visitor = security.createRole( "Guest", ALLOW_MODES.DENY_ALL_BUT );
////			// BEGIN: copied form OSecurityShared - procedure to create the default "reader" role
////			visitor.addRule(ORule.ResourceGeneric.DATABASE, null, ORole.PERMISSION_READ);
////			visitor.addRule(ORule.ResourceGeneric.SCHEMA, null, ORole.PERMISSION_READ);
////			visitor.addRule(ORule.ResourceGeneric.CLUSTER, OMetadataDefault.CLUSTER_INTERNAL_NAME, ORole.PERMISSION_READ);
////			visitor.addRule(ORule.ResourceGeneric.CLUSTER, "orole", ORole.PERMISSION_READ);
////			visitor.addRule(ORule.ResourceGeneric.CLUSTER, "ouser", ORole.PERMISSION_READ);
////			visitor.addRule(ORule.ResourceGeneric.CLASS, null, ORole.PERMISSION_READ);
////			visitor.addRule(ORule.ResourceGeneric.CLUSTER, null, ORole.PERMISSION_READ);
////			visitor.addRule(ORule.ResourceGeneric.COMMAND, null, ORole.PERMISSION_READ);
////			visitor.addRule(ORule.ResourceGeneric.RECORD_HOOK, null, ORole.PERMISSION_READ);
////			visitor.addRule(ORule.ResourceGeneric.FUNCTION, null, ORole.PERMISSION_READ);
////		}
//		
//	}
	
	private String localRole( String roleName ) {
		return roleName + "-" + dom.getRootName();
	}
	
	public void setRoleProperty( String roleName, String key, String value ) {
		
		OSecurity security = dom.graph.getRawGraph().getMetadata().getSecurity();
		
		ORole role = security.getRole( localRole( roleName ) );
		
		if( role == null ) return;
		
		role.getDocument().field( "properties." + key, value );
		
	}
	
	public String getRoleProperty( String roleName, String key, String def ) {
		
		OSecurity security = dom.graph.getRawGraph().getMetadata().getSecurity();
		
		ORole role = security.getRole( localRole( roleName ) );
		
		if( role == null ) return def;
		
		String ret = role.getDocument().field( "properties." + key );
		
		if( ret == null ) ret = def;
		
		return ret;
		
	}
	
	public List<String> listPublicDomains() {
		
		String q = "select from Space where role is not null";
		
		List<ODocument> docs = dom.graph.getRawGraph().query( 
				new OSQLSynchQuery<ODocument>( q ) );
		
		return new GenericNodeCollection<String>( docs, new NameAttributeProvider() );
	}
	
	public void setPredefinedRole( String value ) {
		
		List<ODocument> docs = query( "select from orole where name = '" + localRole( value ) + "'" );
		
		if( docs == null ) docs = new ArrayList<>();
		if( docs.size() < 1 ) {
			execute( "update Space set role = null where tag = '" + dom.getRootName() + "'" );
		}
		else {
			execute( "update Space set role = (select from orole where name = '" + localRole( value ) + "') where tag = '" + dom.getRootName() + "'" );
		}
		
	}
	
	public String getPredefinedRole() {
		
		List<ODocument> docs = query( "select expand(role) from (select from Space where tag = '" + dom.getRootName() + "')" );
		
		if( docs == null ) return "";
		if( docs.size() < 1 ) return "";
		
		return docs.get( 0 ).field( "inheritedRole.name" );
	}
	
	public void createRole( String name ) {
		
		String localName = localRole( name );
		
		List<ODocument> docs = dom.querySynch( "select from orole where name = '" + localName + "'" );
		
		if( docs != null ) {
			if( docs.size() != 0 ) return; // already existing
		}
		
		execute( "INSERT INTO orole SET name = '" + localName + 
				"', mode = 0, domain=" + dom.getRoot().toString() +
				", inheritedRole = (SELECT FROM orole WHERE name = '" + name + "')" );
		
		ORole role = dom.graph.getRawGraph().getMetadata().getSecurity().getRole( localName );
		role.setMode( ALLOW_MODES.DENY_ALL_BUT );
		role.addRule(ORule.ResourceGeneric.DATABASE, null, ORole.PERMISSION_ALL);
		role.addRule(ORule.ResourceGeneric.SCHEMA, null, ORole.PERMISSION_ALL);
		role.addRule(ORule.ResourceGeneric.CLUSTER, OMetadataDefault.CLUSTER_INTERNAL_NAME, ORole.PERMISSION_ALL);
		role.addRule(ORule.ResourceGeneric.CLUSTER, "orole", ORole.PERMISSION_ALL);
		role.addRule(ORule.ResourceGeneric.CLUSTER, "ouser", ORole.PERMISSION_ALL);
		role.addRule(ORule.ResourceGeneric.CLUSTER, null, ORole.PERMISSION_ALL);
		role.addRule(ORule.ResourceGeneric.COMMAND, null, ORole.PERMISSION_ALL);
		role.addRule(ORule.ResourceGeneric.RECORD_HOOK, null, ORole.PERMISSION_ALL);
		role.addRule(ORule.ResourceGeneric.FUNCTION, null, ORole.PERMISSION_ALL);
		
		role.addRule(ORule.ResourceGeneric.CLASS, "ChildOf", ORole.PERMISSION_ALL);
		role.addRule(ORule.ResourceGeneric.CLASS, "Link", ORole.PERMISSION_ALL);
		role.addRule(ORule.ResourceGeneric.CLASS, "E", ORole.PERMISSION_ALL);
		role.addRule(ORule.ResourceGeneric.CLASS, "V", ORole.PERMISSION_ALL);
		role.addRule(ORule.ResourceGeneric.CLASS, "Space", ORole.PERMISSION_ALL);
		role.save();
	}

	public List<String> listRoles() {
		List<ODocument> list = dom.querySynch( "SELECT FROM orole WHERE domain.tag='" + dom.getRootName() + "'" );
		return new GenericNodeCollection<String>( list, new AttributeProvider<String>() {
			@Override
			public String getValue( ODocument doc ) {
				return doc.field( "name" );
			}} );
	}
	
	public void setPermission( String rolename, String res, String perm ) {
		rolename = localRole( rolename );
		
		ORole role = dom.graph.getRawGraph().getMetadata().getSecurity().getRole( rolename );
		
		if( role == null ) return;
		
		for( int p : mapPermission( perm ) ) {
			role.addRule( ResourceGeneric.CLASS, res, p );
		}
		
		role.save();
	}

	private Collection<Integer> mapPermission( String perm ) {
		ArrayList<Integer> list = new ArrayList<>();
		if( perm == null ) return list;
		for (int i = 0, n = perm.length(); i < n; i++) {
		    char c = perm.charAt(i);
		    Integer val = map.get( String.valueOf( c ) );
		    if( val != null )
		    	list.add( val );
		}
		return list;
	}
	
}
