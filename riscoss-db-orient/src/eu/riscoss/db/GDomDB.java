package eu.riscoss.db;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.orient.OrientGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientVertex;

public class GDomDB {
	
	public static final String CHILDOF_CLASS = "ChildOf";
	public static final String LINK_CLASS = "Link";
	
	public static final String ROOT_CLASS = "Space";
	public static final String NODE_CLASS = "Node";
	
	static Map<String,OrientGraphFactory> factories = new HashMap<String,OrientGraphFactory>();
	
	private synchronized static OrientGraph acquireFactory( String dbaddress ) {
		OrientGraphFactory factory = factories.get( dbaddress );
		if( factory == null ) {
			factory = new OrientGraphFactory( dbaddress ); //.setupPool(1,10);
			factories.put( dbaddress, factory );
		}
		return factory.getTx();
	}
	
	OrientGraph graph = null;
	Vertex root;
	String rootName;
	
	public GDomDB( String dbaddress, String rootName ) {
		OrientGraph graph = acquireFactory( dbaddress );
		init( graph, rootName );
	}
	
	public GDomDB( String dbaddress ) {
		this( dbaddress, "Root" );
	}
	
	public GDomDB( OrientGraph graph, String rootName ) {
		init( graph, rootName );
	}
	
	private void init( OrientGraph graph, String rootName ) {
		this.graph = graph;
		this.rootName = rootName;
		ensureNodeTypeExistence( graph, ROOT_CLASS );
		ensureNodeTypeExistence( graph, NODE_CLASS );
		ensureEdgeTypeExistence( graph, CHILDOF_CLASS );
		ensureEdgeTypeExistence( graph, LINK_CLASS );
		root = getRoot( rootName, 0 );
		if( root == null ) {
			root = graph.addVertex( ROOT_CLASS, (String)null );
			root.setProperty( "tag", rootName );
			graph.commit();
		}
	}
	
	private void ensureEdgeTypeExistence(OrientGraph graph, String type ) {
		if( graph.getEdgeType( type ) == null ) {
			graph.createEdgeType( type );
		}
	}

	private void ensureNodeTypeExistence( OrientGraph graph, String type ) {
		if( graph.getVertexType( type ) == null ) {
			graph.createVertexType( type );
		}
	}

	private Vertex getRoot( String cls, int index ) {
		try {
			List<ODocument> list = graph.getRawGraph().query( new OSQLSynchQuery<ODocument>( 
					"SELECT FROM " + ROOT_CLASS + " WHERE tag='" + cls + "'" ) );
			if( list == null ) return null;
			if( !(list.size() > index) ) return null;
			return graph.getVertex( list.get( index ).getIdentity() );
		}
		catch( Exception ex ) {
			return null;
		}
	}
	
	List<ODocument> querySynch( String query ) {
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
	
	public NodeID createVertex( String tag ) {
		Vertex child = graph.addVertex( NODE_CLASS, (String)null );
		child.setProperty( "tag", tag );
		child.setProperty( "root", rootName );
		graph.commit();
		return new NodeID( child.getId().toString() );
	}
	
	public void deleteVertex(NodeID nodeID) {
		Vertex v = graph.getVertex( nodeID.toString() );
		if( v == null ) return;
		while( children( nodeID ).size() > 0 ) {
			deleteVertex( children( nodeID ).get( 0 ) );
		}
		graph.removeVertex( v );
	}
	
	public void deleteChildren( String path ) {
		NodeID id = get( path );
		if( id == null ) return;
		while( children( id ).size() > 0 ) {
			deleteVertex( children( id ).get( 0 ) );
		}
	}
	
	public Collection<NodeID> listVertices( String tag ) {
		String q = "select from " + NODE_CLASS + " where tag='" + tag + "' and root='" + this.rootName + "'";
		try {
			List<ODocument> docs = graph.getRawGraph().query( 
					new OSQLSynchQuery<ODocument>( q ) );
			return new NodeCollection( docs );
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return new NodeCollection( new ArrayList<ODocument>() );
		}
	}
	
	public List<NodeID> listOutEdges( NodeID idFrom, String edgeClass, String label, String targetTag ) {
		
		return listOutEdgeNames( idFrom, edgeClass, label, targetTag, null, new AttributeProvider<NodeID>() {
			@Override
			public NodeID getValue( ODocument doc ) {
				return new NodeID( doc.getIdentity().toString() );
			}} );
		
	}
	
	public List<String> listOutEdgeNames( NodeID idFrom, String edgeClass, String label, String targetTag ) {
		
		return listOutEdgeNames( idFrom, edgeClass, label, targetTag, null, new AttributeProvider<String>() {
			@Override
			public String getValue( ODocument doc ) {
				return doc.field( "tag" );
			}} );
		
	}
	
	public List<String> listOutEdgeNames( NodeID idFrom, String edgeClass, String label, String targetTag, String query ) {
		
		return listOutEdgeNames( idFrom, edgeClass, label, targetTag, query, new AttributeProvider<String>() {
			@Override
			public String getValue( ODocument doc ) {
				return doc.field( "tag" );
			}} );
		
	}
	
	public <T> List<T> listOutEdgeNames( NodeID idFrom, String edgeClass, String label, String targetTag, String query, AttributeProvider<T> provider ) {
		
		String q;
		q = "select expand( inV() ) from ";
		q += "(select expand( outE('" + edgeClass + "')";
		if( label != null ) 
			q += "[name='" + label + "']";
		q += " ) from " + idFrom + ")";
		if( targetTag != null | query != null ) {
			q += " where ";
			if( targetTag != null )
				q += "in.tag='" + targetTag + "'";
			if( query != null )
				if( targetTag != null )
					q += " and " + query;
				else
					q += query;
		}
		
//		System.out.println( q );
		List<ODocument> list = querySynch( q );
		if( list == null )
			list = new ArrayList<ODocument>();
		return new GenericNodeCollection<T>( list, provider );
	}
	
	public List<NodeID> listInEdges( NodeID idFrom, String edgeClass, String label ) {
		String q;
		q = "select expand( outV() ) from ";
		q += "(select expand( inE('" + edgeClass + "')";
		if( label != null ) 
			q += "[name='" + label + "']";
		q += " ) from " + idFrom + ")";
		
		List<ODocument> list = querySynch( q );
		if( list == null )
			list = new ArrayList<ODocument>();
		return new NodeCollection( list );
	}
	
	public <T> List<T> listInEdges( NodeID idFrom, String edgeClass, String label, AttributeProvider<T> provider ) {
		
		String q;
		q = "select expand( outV() ) from ";
		q += "(select expand( inE('" + edgeClass + "')";
		if( label != null ) 
			q += "[name='" + label + "']";
		q += " ) from " + idFrom + ")";
		
		List<ODocument> list = querySynch( q );
		if( list == null )
			list = new ArrayList<ODocument>();
		return new GenericNodeCollection<T>( list, provider );
	}
	
	public List<NodeID> links( NodeID idFrom, String label ) {
		return listOutEdges( idFrom, LINK_CLASS, label, null );
	}
	
	public List<NodeID> children( NodeID parent ) {
		return listOutEdges( parent, CHILDOF_CLASS, null, null );
	}
	
	public List<NodeID> children( String path ) {
		NodeID parent = getVertex( path );
		if( parent == null ) return new ArrayList<NodeID>();
		return listOutEdges( parent, CHILDOF_CLASS, null, null );
	}
	
	public NodeID getChild( NodeID parent, String childName ) {
		List<NodeID> list = listOutEdges( parent, CHILDOF_CLASS, null, childName );
		if( list == null ) return null;
		if( list.size() < 1 ) return null;
		return list.get( 0 );
	}
	
	void createEdge( NodeID idFrom, NodeID idTo, String className, String linkName ) {
		Vertex from = graph.getVertex( idFrom );
		Vertex to = graph.getVertex( idTo );
		Edge e = graph.addEdge( null, from, to, className );
		if( linkName != null )
			e.setProperty( "name", linkName );
		graph.commit();
	}
	
	void deleteEdge( String tagFrom, String tagTo, String className, String linkName ) {
		String q = "delete edge " + className + " " + 
				"from (select from " + NODE_CLASS + " where tag='" + tagFrom + "') " + 
				"to (select from " + NODE_CLASS + " where tag='" + tagTo + "')";
		if( linkName != null ) 
			q += " where name='" + linkName + "'";
		querySynch( q );
	}
	
	public NodeID getRoot() {
		return new NodeID( root.getId().toString() );
	}
	
	public NodeID get( String pathString ) {
		
		NodeID parent = new NodeID( root.getId().toString() );
		
		DomPath path = new DomPath( pathString );
		
		for( DomPath.PathPart part : path ) {
			NodeID child = null;
			if( part.getLink() != null ) {
				List<NodeID> list = listOutEdges( parent, GDomDB.LINK_CLASS, part.getLink(), null );
				if( list == null ) child = null;
				else if( list.size() < 1 ) child = null;
				else child = list.get( 0 );
			}
			else {
				child = getChild( parent, part.getName() );
			}
			if( child == null ) {
				return null;
			}
			parent = child;
		}
		
		return parent;
	}
	
	public List<String> listChildren( String path ) {
		return listOutEdgeNames( get( path ), GDomDB.CHILDOF_CLASS, null, null );
	}
	
	public NodeID create( String pathString ) {
		
		NodeID parent = new NodeID( root.getId().toString() );
		
		DomPath path = new DomPath( pathString );
		
		for( DomPath.PathPart part : path ) {
			NodeID child = getChild( parent, part.getName() );
			if( child == null ) {
				child = createVertex( part.getName() );
				createEdge( parent, child, CHILDOF_CLASS, null );
			}
			parent = child;
		}
		
		return parent;
	}
	
	public NodeID createChild( NodeID eid, String tag ) {
		NodeID id = createVertex( tag );
		createEdge( eid, id, CHILDOF_CLASS, null );
		return id;
	}
	
	public NodeID getVertex( String pathString ) {
		
		NodeID parent = new NodeID( root.getId().toString() );
		
		DomPath path = new DomPath( pathString );
		
		for( DomPath.PathPart part : path ) {
			NodeID child = getChild( parent, part.getName() );
			if( child == null ) return null;
			parent = child;
		}
		
		return parent;
	}
	
	public void link( String pathFrom, String pathTo, String linkName ) {
		NodeID idFrom = getVertex( pathFrom );
		NodeID idTo = getVertex( pathTo );
		link( idFrom, idTo, linkName );
	}
	
	public void link( NodeID idFrom, NodeID idTo, String linkName ) {
		if( getEdge( idFrom, idTo, LINK_CLASS, linkName ) != null ) return;
		createEdge( idFrom, idTo, LINK_CLASS, linkName );
	}
	
	Edge getEdge( NodeID from, NodeID to, String className, String linkName ) {
		List<ODocument> list = querySynch( "select from " + className + " where " + from + "=out and " + to + "=in and name='" + linkName + "'" );
		if( list == null ) return null;
		if( list.size() < 1 ) return null;
		return graph.getEdge( list.get( 0 ).getIdentity() );
	}
	
	public String getName( NodeID id, String def ) {
		Vertex v = graph.getVertex( id.toString() );
		if( v == null ) return def;
		return v.getProperty( "tag" );
	}
	
	public void rmlink( String pathFrom, String pathTo, String linkName ) {
		NodeID from = getVertex( pathFrom );
		if( from == null ) return;
		NodeID to = getVertex( pathTo );
		if( to == null ) return;
		List<ODocument> list = querySynch( "select from Link where " + from + "=out and " + to + "=in and name='" + linkName + "'" );
		if( list == null ) return;
		if( list.size() < 1 ) return;
		Edge e = graph.getEdge( list.get( 0 ).getIdentity() );
		if( e != null ) {
			graph.removeEdge( e );
			graph.commit();
		}
	}
	
	public void rmlink( NodeID from, NodeID to, String linkName ) {
		if( from == null ) return;
		if( to == null ) return;
		List<ODocument> list = querySynch( "select from Link where " + from + "=out and " + to + "=in and name='" + linkName + "'" );
		if( list == null ) return;
		if( list.size() < 1 ) return;
		Edge e = graph.getEdge( list.get( 0 ).getIdentity() );
		if( e != null ) {
			graph.removeEdge( e );
			graph.commit();
		}
	}
	
	public String getAttribute( NodeID id, String key, String def ) {
		Vertex v = graph.getVertex( id.toString() );
		if( v == null ) return def;
		String ret = v.getProperty( key );
		return ret;
	}
	
	public byte[] getByteAttribute(NodeID id, String key) {
		OrientVertex v = graph.getVertex( id.toString() );
		if( v == null ) 
			return new byte[0];
		byte[] ret = v.getRecord().field( key );
		return ret;  
	}
	
	public void setAttribute( NodeID id, String key, String value ) {
		Vertex v = graph.getVertex( id.toString() );
		if( v == null ) return;
		v.setProperty( key, value );
		graph.commit();
	}
	
	public void setAttribute( NodeID id, String key, byte[] value ) {
		OrientVertex v = graph.getVertex( id.toString() );
		if( v == null ) 
			return;
		v.getRecord().field(key, value); 
		graph.commit();	
	}
	
	public void removeAttribute( NodeID id, String key) {
		OrientVertex v = graph.getVertex( id.toString() );
		if( v == null ) 
			return;
		v.getRecord().removeField(key); 
		graph.commit();	
	}
	
	public String getPath( NodeID id ) {
		String path = "";
		NodeID pid = id;
		while( pid != null ) {
			path = "/" + getName( pid, "" ) + path;
			pid = getParent( pid );
		}
		return path;
	}
	
	public String getParentName( NodeID id ) {
		NodeID pid = getParent( id );
		if( pid == null ) return null;
		return getAttribute( pid, "tag", null );
	}
	
	private NodeID getParent( NodeID id ) {
		List<NodeID> list = listInEdges( id, CHILDOF_CLASS, null );
		if( list == null ) return null;
		if( list.size() < 1 ) return null;
		NodeID pid = list.get( 0 );
		if( pid.compareTo( getRoot() ) == 0 ) return null;
		return pid;
	}
	
	public boolean exists(NodeID id) {
		return graph.getVertex( id.toString() ) != null;
	}

	public void close() {
		graph.getRawGraph().close();
	}
	
	public void rmlinks( String path, String linkName ) {
		NodeID from = getVertex( path );
		if( from == null ) return;
		List<ODocument> list = querySynch( "select from Link where " + from + "=out  and name='" + linkName + "'" );
		if( list == null ) return;
		if( list.size() < 1 ) return;
		for( int i = 0; i < list.size(); i++ ) {
			Edge e = graph.getEdge( list.get( i ).getIdentity() );
			graph.removeEdge( e );
			graph.commit();
		}
	}

	public Map<String, Object> getAttributes( NodeID id ) {
		try {
			Vertex v = graph.getVertex( id );
			return ((OrientVertex)v).getRecord().toMap();
		}
		catch( Exception ex ) {
			ex.printStackTrace();
			return null;
		}
	}

}
