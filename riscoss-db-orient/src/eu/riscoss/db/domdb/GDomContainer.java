package eu.riscoss.db.domdb;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.orientechnologies.orient.core.metadata.security.ORole;
import com.orientechnologies.orient.core.metadata.security.ORule.ResourceGeneric;
import com.orientechnologies.orient.core.metadata.security.OSecurity;
import com.orientechnologies.orient.core.metadata.security.OSecurityRole;
import com.orientechnologies.orient.core.metadata.security.OSecurityRole.ALLOW_MODES;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;

public class GDomContainer {
	
	OrientBaseGraph graph;
	
	public GDomContainer( OrientBaseGraph graph ) {
		this.graph = graph;
	}

	public void execute( String cmd ) {
		graph.getRawGraph().command(new OCommandSQL(cmd)).execute();
	}
	
	public List<ODocument> query( String query ) {
		try {
			List<ODocument> docs = graph.getRawGraph().query( 
					new OSQLSynchQuery<ODocument>( query ) );
			return docs;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return new ArrayList<ODocument>();
		}
	}
	
	private Vertex getRoot( String cls ) {
		try {
			List<ODocument> list = graph.getRawGraph().query( new OSQLSynchQuery<ODocument>( 
					"SELECT FROM " + GDomConfig.global().getRootClass() + " WHERE tag='" + cls + "'" ) );
			if( list == null ) return null;
			if( !(list.size() > 0) ) return null;
			return graph.getVertex( list.get( 0 ).getIdentity() );
		}
		catch( Exception ex ) {
			return null;
		}
	}
	
	public void createRole( String roleName ) {
		OSecurity security = graph.getRawGraph().getMetadata().getSecurity();
		if( security.getRole( roleName ) == null ) {
			ORole role = security.createRole( roleName, ALLOW_MODES.ALLOW_ALL_BUT );
//			role.addRule(ORule.ResourceGeneric.DATABASE, null, ORole.PERMISSION_ALL);
//			role.addRule(ORule.ResourceGeneric.SCHEMA, null, ORole.PERMISSION_ALL);
//			role.addRule(ORule.ResourceGeneric.CLUSTER, OMetadataDefault.CLUSTER_INTERNAL_NAME, ORole.PERMISSION_ALL);
//			role.addRule(ORule.ResourceGeneric.CLUSTER, "orole", ORole.PERMISSION_ALL);
//			role.addRule(ORule.ResourceGeneric.CLUSTER, "ouser", ORole.PERMISSION_ALL);
//			role.addRule(ORule.ResourceGeneric.CLUSTER, null, ORole.PERMISSION_ALL);
//			role.addRule(ORule.ResourceGeneric.COMMAND, null, ORole.PERMISSION_ALL);
//			role.addRule(ORule.ResourceGeneric.RECORD_HOOK, null, ORole.PERMISSION_ALL);
//			role.addRule(ORule.ResourceGeneric.FUNCTION, null, ORole.PERMISSION_ALL);
			role.getDocument().field( "type", "template" );
			role.save();
		}
	}
	
	public void setRoleProperty( String roleName, String key, String value ) {
		OSecurity security = graph.getRawGraph().getMetadata().getSecurity();
		ORole role = security.getRole( roleName );
		if( role == null ) return;
		role.getDocument().field( "properties." + key, value );
		
	}
	
	public String getRoleProperty( String roleName, String key, String def ) {
		OSecurity security = graph.getRawGraph().getMetadata().getSecurity();
		ORole role = security.getRole( roleName );
		if( role == null ) return def;
		String ret = role.getDocument().field( "properties." + key );
		if( ret == null ) ret = def;
		return ret;
	}
	
	public void createDom( String domainName ) {
		
		Vertex v = getRoot( domainName );
		
		if( v == null ) {
			v = graph.addVertex( GDomConfig.global().getRootClass(), (String)null );
			v.setProperty( "tag", domainName );
			graph.commit();
		}
		
	}
	public List<String> domList() {
		List<ODocument> list = query( "SELECT FROM " + GDomConfig.global().getRootClass() );
		return new GenericNodeCollection<String>( list, new NameAttributeProvider() );
	}
	
	public List<String> listPublicDomains() {
		
		String q = "select from Space where role is not null";
		
		List<ODocument> docs = graph.getRawGraph().query( 
				new OSQLSynchQuery<ODocument>( q ) );
		
		return new GenericNodeCollection<String>( docs, new NameAttributeProvider() );
	}
	
	public List<String> listRoles() {
		List<ODocument> list = 
				graph.getRawGraph().query( 
						new OSQLSynchQuery<ODocument>( "SELECT FROM orole WHERE type='template'" ) );
		return new GenericNodeCollection<String>( list, new AttributeProvider<String>() {
			@Override
			public String getValue( ODocument doc ) {
				return doc.field( "name" );
			}} );
	}
	
	public List<String> listUsers( String from, String max, String pattern ) {
		
		String q = "SELECT FROM ouser";
		if( pattern != null )
			if( !"".equals( pattern ) )
				q += " WHERE name like '" + pattern + "'";
		q += " SKIP " + from;
		q += " LIMIT " + max;
		List<ODocument> list = 
				graph.getRawGraph().query( 
						new OSQLSynchQuery<ODocument>( q ) );
		return new GenericNodeCollection<String>( list, new AttributeProvider<String>() {
			@Override
			public String getValue( ODocument doc ) {
				return doc.field( "name" );
			}} );
	}

	public GDomDB getDom( String domain ) {
		return new GDomDB( graph, domain );
	}

	public void close() {
		this.graph.getRawGraph().close();
		this.graph = null;
	}

	public List<String> listDomains( String username ) {
		
		Set<? extends OSecurityRole> roles = graph.getRawGraph().getUser().getRoles();
		for( OSecurityRole role : roles ) {
			if( role.hasRule( ResourceGeneric.BYPASS_RESTRICTED, null ) ) {
				return new GenericNodeCollection<String>( 
						query( "select from " + GDomConfig.global().getRootClass() ), new NameAttributeProvider() );
			}
		}
		
		String q = "select from ( select expand(domain) from (select from orole where @this in (select expand(roles) from ouser where name = '" + username + "')))";
		
		List<ODocument> list = query( q );
		
		return new GenericNodeCollection<String>( list, new NameAttributeProvider() );
	}

	public boolean containsDomain( String domain ) {
		
		List<ODocument> docs = query( "select from Space where tag = '" + domain + "'" );
		
		if( docs == null ) return false;
		if( docs.size() < 1 ) return false;
		
		return true;
	}

	public OrientBaseGraph getGraph() {
		return this.graph;
	}
	
}
