package eu.riscoss.db;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.security.OToken;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;
import com.orientechnologies.orient.server.token.OrientTokenHandler;
import com.tinkerpop.blueprints.impls.orient.OrientBaseGraph;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.tinkerpop.blueprints.impls.orient.OrientGraphNoTx;

import eu.riscoss.db.domdb.AttributeProvider;
import eu.riscoss.db.domdb.GAuthDom;
import eu.riscoss.db.domdb.GDomDB;
import eu.riscoss.db.domdb.GenericNodeCollection;
import eu.riscoss.db.domdb.NameAttributeProvider;
import eu.riscoss.db.domdb.NodeID;
import eu.riscoss.db.domdb.OLinkedList;

public class ORiscossDomain implements RiscossDB {
	
	GDomDB		dom = null;
	
	ORiscossDomain( OrientBaseGraph graph, String domain ) {
		
		dom = new GDomDB( graph, domain );
		
	}
	
	public ORiscossDomain( String address, String domain ) {
		
		OrientGraphNoTx graph = new OrientGraphFactory( address ).getNoTx();
		
		dom = new GDomDB( graph, domain );
		
	}
	
	public ORiscossDomain( String address, String domain, String username, String password ) {
		
		OrientGraphNoTx graph = new OrientGraphFactory( address, username, password ).getNoTx();
		
		dom = new GDomDB( graph, domain );
		
	}
	
	public ORiscossDomain( String address, String domain, byte[] token ) {
		
		OrientTokenHandler handler = ORiscossDatabase.createTokenHandler();
		
		OToken tok = handler.parseWebToken(token);
		handler.validateBinaryToken( tok );
		
		@SuppressWarnings("resource") // FIXME
		OrientGraphNoTx graph = new OrientGraphNoTx( (ODatabaseDocumentTx)new ODatabaseDocumentTx( address ).open( tok ) );
		
		dom = new GDomDB( graph, domain );
	}
	
	NodeID getLink( NodeID from, String link ) {
		List<NodeID> list = dom.listOutEdges( from, GDomDB.LINK_CLASS, link, null );
		if( list == null ) return null;
		if( list.size() < 1 ) return null;
		return list.get( 0 );
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.db.RiscossDBInterface#addLayer(java.lang.String, java.lang.String)
	 */
	@Override
	public void addLayer( String name, String after ) {
		
		new OLinkedList( dom, "/layers" ).addLayer(name, after);
	}
	
	@Override
	public List<String> getModelList() {
		return dom.listOutEdgeNames( 
				dom.create( "/models" ), GDomDB.CHILDOF_CLASS, null, null, null, new NameAttributeProvider() );
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.db.RiscossDBInterface#getProgramBlob(java.lang.String)
	 */
	@Override
	public String getModelBlob( String modelName ) {
		NodeID id = dom.getVertex( "/models/" + modelName );
		if( id == null ) return null;
		return dom.getAttribute( id, "blob", "" );
	}
	
	@Override
	public Collection<String> getRiskConfigurations() {
		
		return dom.listOutEdgeNames( 
				dom.create( "/risk-configurations" ), GDomDB.CHILDOF_CLASS, null, null, null, new NameAttributeProvider() );
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.db.RiscossDBInterface#entities()
	 */
	public Collection<NodeID> listEntities() {
		return dom.children( "/entities" );
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.db.RiscossDBInterface#entities(eu.riscoss.db.NodeID)
	 */
	public Collection<NodeID> listEntities( NodeID lid ) {
		return dom.links( lid, "contains" );
	}
	
	public Collection<NodeID> listEntities( String layer ) {
		NodeID lid = dom.get( "/layers/" + layer );
		if( lid == null ) return new ArrayList<NodeID>();
		return listEntities( lid );
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.db.RiscossDBInterface#storeModel(java.lang.String, java.lang.String)
	 */
	@Override
	public void storeModel( String modelBlob, String modelName ) {
		NodeID id = dom.create( "/models/" + modelName );//"tag" field, defaults to the filename at a new model upload.
		dom.setAttribute( id, "blob", modelBlob );
		dom.setAttribute( id, "modelfilename", modelName ); 
		//TODO: check for duplicate name!
	}
	
	@Override
	public void removeModelBlob(String modelName) {
		NodeID id = dom.get( "/models/" + modelName );
		if( id != null ) {
			dom.deleteVertex( id );
		}
	}
	
	@Override
	public void updateModel(String modelName, String blobFilename, String modelBlob) {
		NodeID id = dom.getVertex( "/models/" + modelName );
		dom.setAttribute( id, "blob", modelBlob );
		dom.setAttribute( id, "modelfilename", blobFilename );
	}
	
	public String getName( NodeID id ) {
		return dom.getAttribute( id, "tag", "-" );
	}
	
	public String getModelAttribute( String modelName, String attribute ) {
		NodeID id = dom.getVertex( "/models/" + modelName );
		if( id == null ) return "";
		return dom.getAttribute( id, attribute, "" );
	}
	
	@Override
	public String getModelFilename(String modelName) {
		return getModelAttribute(modelName, "modelfilename");
	}
		
	@Override
	public void changeModelName(String modelName, String newName) {
		NodeID id = dom.getVertex( "/models/" + modelName );
		dom.setAttribute( id, "tag", newName );		
		//TODO: check for duplicate name!
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.db.RiscossDBInterface#removeEntity(java.lang.String)
	 */
	@Override
	public void removeEntity( String name ) {
		NodeID id = dom.getVertex( "/entities/" + name );
		if( id != null ) {
			removeEntity( id );
		}
	}
	
	@Override
	public void editLayer(String entity, String layer) {
		NodeID entityID = dom.getVertex( "/entities/" + entity );
		dom.rmlink("/layers/" + layerOf(entity), "/entities/" + entity, "contains");
		
		NodeID id = dom.get( "/layers/" + layer );
		if( id != null ) {
			dom.link( "/layers/" + layer, "/entities/" + entity, "contains" );
		}
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.db.RiscossDBInterface#removeEntity(eu.riscoss.db.NodeID)
	 */
	public void removeEntity( NodeID nodeID ) {
		dom.deleteVertex( nodeID );
	}
	
	public String getPath( NodeID id ) {
		return dom.getPath( id );
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.db.RiscossDBInterface#getEntity(java.lang.String)
	 */
	public NodeID getEntity( String name ) {
		return dom.getVertex( "/entities/" + name );
	}
	
	@Override
	public boolean existsEntity( String name ) {
		return null != dom.getVertex( "/entities/" + name );
	}
	
	public void createChild( NodeID entity, String name ) {
		dom.create( "/entities/" + getName( entity ) + "/" + name );
	}
	
	public void setProperty( NodeID id, String relativePath, String key, String value) {
		String path = getPath( id );
		if( relativePath != null )
			path = path + "/" + relativePath;
		id = dom.getVertex( path );
		dom.setAttribute( id, key, value );
	}
	
	public String getProperty( NodeID id, String relativePath, String key ) {
		return getProperty( id, relativePath, key, null );
	}
	
	public String getProperty( NodeID id, String relativePath, String key, String def ) {
		String path = getPath( id );
		if( relativePath != null )
			path = path + "/" + relativePath;
		id = dom.getVertex( path );
		if( id == null ) return null;
		return dom.getAttribute( id, key, def );
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.db.RiscossDBInterface#storeRiskData(java.lang.String)
	 */
	@Override
	public void storeRiskData( String rd ) throws Exception {
		JsonObject o = (JsonObject)new JsonParser().parse( rd );
		if( o.get( "value" ) != null ) {
			NodeID id = dom.create( "/entities/" + o.get( "target" ).getAsString() + "/data/" + o.get( "id" ).getAsString() );
			dom.setAttribute( id, "target", o.get( "target" ).getAsString() );
			dom.setAttribute( id, "type", o.get( "type" ).getAsString() );
			dom.setAttribute( id, "value", o.get( "value" ).getAsString() );
			dom.setAttribute( id, "date", "" + o.get( "date" ) );
			if( o.get( "datatype" ) != null ) {
				dom.setAttribute( id, "datatype", o.get( "datatype" ).getAsString() );
			}
			if( o.get( "origin" ) != null ) {
				dom.setAttribute( id, "origin", o.get( "origin" ).getAsString() );
			}
		}
		else {
			NodeID id = dom.get( "/entities/" + o.get( "target" ).getAsString() + "/data/" + o.get( "id" ).getAsString() );
			if( id != null ) {
				dom.deleteVertex( id );
			}
		}
	}
	
	@Override
	public Collection<String> listRiskData( String e_name ) {
		NodeID entity = getEntity( e_name );
		NodeID did = dom.getVertex( dom.getPath( entity ) + "/data" );
		if( did == null ) return new ArrayList<String>();
		return dom.listOutEdgeNames( did, GDomDB.CHILDOF_CLASS, null, null );
	}
	
	@Override
	public Collection<String> listUserData( String e_name ) {
		NodeID entity = getEntity( e_name );
		NodeID did = dom.getVertex( dom.getPath( entity ) + "/data" );
		if( did == null ) return new ArrayList<String>();
		return dom.listOutEdgeNames( did, GDomDB.CHILDOF_CLASS, null, null, "in.origin='user'" );
	}
	
	@Override
	public String readRiskData( String entity_name, String indicator_id ) {
		
		NodeID id = dom.get( "/entities/" + entity_name + "/data/" + indicator_id );
		
		if( id == null ) return null;
		
		String rid = getName( id );
		String type = dom.getAttribute( id, "type", null );
		String datatype = dom.getAttribute( id, "datatype", "" );
		String value = dom.getAttribute( id, "value", null );
		String target = dom.getAttribute( id, "target", null );
		if( target == null ) target = getName( id );
		if( (rid==null) | (type==null) | (value==null) ) return null;
		Date date = parse( dom.getAttribute( id, "date", null ) );
		String origin = dom.getAttribute( id, "origin", null );
		
		JSONObject o = new JSONObject();
		
		try {
			o.put( "type", type );
			o.put( "datatype", datatype );
			o.put( "value", value );
			o.put( "target", target );
			o.put( "date", date );
			o.put( "id", rid );
			if( origin != null ) 
				o.put( "origin", origin );
		}
		catch( Exception ex ) {
			ex.printStackTrace();
		}
		
		return o.toString();
	}
	
	public static Date parse( String value ) { 
		if( value == null ) return new Date();
		try {
			Long l = Long.parseLong( value );
			return new Date( l.longValue() );
		} catch (Exception e) {
			return new Date();
		} 
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.db.RiscossDBInterface#createRiskConfiguration(java.lang.String)
	 */
	@Override
	public void createRiskConfiguration( String name ) {
		dom.create( "/risk-configurations/" + name );
	}
	
	@Override
	public List<String> getModelsFromRiskCfg( String rc_name, String entity  ) {
		List<String> list = getModelsFromRiskCfg_new( rc_name, entity );
		if( list.size() > 0 ) return list;
		NodeID rcid = getRiskConfigurationID( rc_name );
		String str = getProperty( rcid, null, "models", null );
		if( str == null ) return new ArrayList<String>();
		String[] tok = str.split( "[,]" );
		list = new ArrayList<>();
		for( String t : tok ) {
			NodeID id = new NodeID( t );
			if( dom.exists( id ) ) {
				list.add( getName( id ) );
			}
		}
		return list;
	}
	
	List<String> getModelsFromRiskCfg_new( String rc_name, String entity  ) {
		String layer = layerOf( entity );
		Map<String,ArrayList<String>> models = getRCModels( rc_name );
		ArrayList<String> list = models.get( layer );
		if( list != null ) return list;
		NodeID rcid = getRiskConfigurationID( rc_name );
		return dom.listOutEdgeNames( rcid, GDomDB.LINK_CLASS, "contains", null );
	}
	
	NodeID getRiskConfigurationID( String name ) {
		return dom.getVertex( "/risk-configurations/" + name );
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.db.RiscossDBInterface#close()
	 */
	@Override
	public void close() {
		dom.close();
	}
	
	@Override
	public void removeLayer(String name) {	
		new OLinkedList( dom, "/layers" ).removeLayer( name );
	}
	
	@Override
	public void renameLayer(String name, String newName) {	
		new OLinkedList( dom, "/layers" ).renameLayer( name, newName );
	}
	
	@Override
	public void editParent(String layer, String newParent) {
		new OLinkedList( dom, "/layers" ).editParent(layer, newParent);
	}
	
	@Override
	public void renameEntity(String name, String newName) {
		NodeID id = dom.getVertex( "/entities/" + name );
		if (id != null) {
			dom.setAttribute( id, "tag", newName );
		}
	}
	
	@Override
	public void setModelsFromRiskCfg(String rcName, List<String> list) {
		dom.rmlinks( "/risk-configurations/" + rcName, "contains" );
		for( String modelName : list ) {
			dom.link( 
					"/risk-configurations/" + rcName, 
					"/models/" + modelName, 
					"contains" );
		}
	}
	
	@Override
	public void removeRiskConfiguration(String name) {
		dom.rmlinks( "/risk-configurations/" + name, "contains" );
		NodeID id = dom.getVertex( "/risk-configurations/" + name );
		if( id != null ) {
			dom.deleteVertex( id );
		}
	}
	
//	@Override
//	public void createModelEntry( String modelName ) {
//		if( dom.get( "/models/" + modelName ) == null ) {
//			dom.create( "/models/" + modelName );
//		}
//	}
	
	@Override
	public void removeModel(String name) {
		NodeID id = dom.get( "/models/" + name );
		if( id != null ) {
			dom.deleteVertex( id );
		}
	}
	
	@Override
	public String layerOf( String entity ) {
		
		NodeID eid = getEntity( entity );
		if( eid == null ) return null;
		
		List<NodeID> list = dom.listInEdges( eid, GDomDB.LINK_CLASS, "contains" );
		if( list == null ) return null;
		if( list.size() < 1 ) return null;
		
		return getName( list.get( 0 ) );
	}
	
	@Override
	public Collection<String> layerNames() {
		return new OLinkedList( dom, "/layers" ).names();
	}
	
	@Override
	public Collection<String> entities() {
		NodeID id = dom.create( "/entities" );
		return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null );
	}
	
	@Override
	public Collection<String> entities( String layer ) {
		NodeID lid = dom.get( "/layers/" + layer );
		if( lid == null ) return new ArrayList<String>();
		return dom.listOutEdgeNames( lid, GDomDB.LINK_CLASS, "contains", null );
	}
	
	@Override
	public void addEntity( String name, String layer  ) {
		dom.create( "/entities/" + name );
		if( layer != null ) {
			NodeID id = dom.get( "/layers/" + layer );
			if( id != null ) {
				dom.link( "/layers/" + layer, "/entities/" + name, "contains" );
			}
		}
	}
	
	@Override
	public void assignEntity( String entity, String parent ) {
		dom.link( "/entities/" + parent, "/entities/" + entity, "owned-entity" );
	}
	
	@Override
	public List<String> getParents( String entity ) {
		NodeID eid = getEntity( entity );
		List<String> list = new ArrayList<String>();
		if( eid != null ) {
			for( NodeID id : dom.listInEdges( eid, GDomDB.LINK_CLASS, "owned-entity" ) ) {
				list.add( getName( id ) );
			}
		}
		return list;
	}
	
	@Override
	public List<String> getChildren( String entity ) {
		NodeID eid = getEntity( entity );
		if( eid == null ) return new ArrayList<>();
		return dom.listOutEdgeNames( eid, GDomDB.LINK_CLASS, "owned-entity", null, null );
	}
	
	@Override
	public void removeEntity( String entity, String parent ) {
		dom.rmlink( "/entities/" + parent, "/entities/" + entity, "owned-entity" );
	}
	
	@Override
	public void setRDCParmeter( String entity, String rdc, String key, String value ) {
		NodeID eid = getEntity( entity );
		//		createChild( eid, "rdcs" );
		createChild( eid, "rdcs/" + rdc );
		setProperty( eid, "rdcs/" + rdc, key, value );
		//		setProperty( eid, "rdcs/" + rdc, "enabled", "true" );
	}
	
	public String getRDCParmeter( String entity, String rdcName, String key, String def ) {
		NodeID eid = getEntity( entity );
		if( eid == null ) return def;
		return getProperty( eid, "rdcs/" + rdcName, key, "" );
	}
	
	public void setRDCEnabled( String entity, String rdc, boolean enabled ) {
		NodeID eid = getEntity( entity );
		createChild( eid, "rdcs/" + rdc );
		setProperty( eid, "rdcs/" + rdc, "enabled", "" + enabled );
	}
	
	public boolean isRDCEnabled( String entity, String rdc ) {
		NodeID eid = getEntity( entity );
		if( eid == null ) return false;
		return "true".equals( getProperty( eid, "rdcs/" + rdc, "enabled", "false" ) );
	}
	
	@Override
	public void storeRASResult( String entity, String string ) {
		Date date = new Date();
		NodeID id = dom.create( "/entities/" + entity + "/ras/" + date.getTime() );
		dom.setAttribute( id, "jsonString", string );
		dom.rmlinks( "/entities/" + entity + "/ras", "last-ras" );
		dom.link( "/entities/" + entity + "/ras", 
				"/entities/" + entity + "/ras/" + date.getTime(), 
				"last-ras" );
	}
	
	@Override
	public String readRASResult( String entity ) {
		NodeID id = dom.get( "/entities/" + entity + "/ras/@last-ras" );
		if( id == null ) return null;
		return dom.getAttribute( id, "jsonString", "" );
	}
	
	@Override
	public void setRCModels( String rc, Map<String,ArrayList<String>> map ) {
		dom.deleteChildren( "/risk-configurations/" + rc + "/layers" );
		dom.create( "/risk-configurations/" + rc + "/layers" );
		int l = 0;
		for( String layer : layerNames() ) {
			ArrayList<String> models = map.get( layer );
			if( models == null ) continue;
			if( models.size() < 1 ) continue;
			dom.create( "/risk-configurations/" + rc + "/layers/" + l );
			dom.link( "/risk-configurations/" + rc + "/layers/" + l, 
					"/layers/" + layer, "rc-layer" );
			for( String model : models ) {
				dom.link( 
						"/risk-configurations/" + rc + "/layers/" + l, 
						"/models/" + model, 
						"rc-model" );
			}
			l++;
		}
	}
	
	@Override
	public Map<String,ArrayList<String>> getRCModels( String rc ) {
		Map<String,ArrayList<String>> map = new HashMap<String,ArrayList<String>>();
		
		for( NodeID cid : dom.children( "/risk-configurations/" + rc + "/layers" ) ) {
			
			String layer = null;
			
			for( NodeID lid : dom.links( cid, "rc-layer" ) ) {
				layer = dom.getName( lid, null );
			}
			
			if( layer == null ) continue;
			
			ArrayList<String> models = new ArrayList<>();
			
			for( NodeID mid : dom.links( cid, "rc-model" ) ) {
				String name = dom.getName( mid, null );
				if( name != null ) {
					models.add( name );
				}
			}
			
			map.put( layer, models );
			
		}
		
		return map;
	}
	
	@Override
	public List<String> findCandidateRCs( String layer ) {
		
		List<String> rcs = new ArrayList<String>();
		
		for( String rc : getRiskConfigurations() ) {
			
			for( NodeID cid : dom.children( "/risk-configurations/" + rc + "/layers" ) ) {
				
				List<NodeID> links = dom.links( cid, "rc-layer" );
				
				if( links == null ) continue;
				if( links.size() < 1 ) continue;
				
				String configuredLayer = dom.getName( links.get( 0 ), null );
				
				if( layer.equals( configuredLayer ) ) {
					rcs.add( rc );
				}
				
				break;
			}			
			
		}
		
		return rcs;
	}
	
	@Override
	public RiskAnalysisSession createRAS() {
		return new OrientRAS( dom );
	}
	
//	@Override
	public List<String> listRAS_old( String entity, String rc ) {
		
		NodeID id = dom.create( "/ras" );
		
		if( entity == null & rc == null ) {
			return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null, null );
		}
		if( entity == null ) {
			return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null, "in.rc='" + rc + "'" );
		}
		if( rc == null ) {
			return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null, "in.target='" + entity + "'" );
		}
		return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null, "in.rc='" + rc + "' and in.target='" + entity + "'" );
	}
	
	static class OrientRecordAbstraction implements RecordAbstraction {

		private ODocument doc;

		@Override
		public String getName() {
			return doc.field( "tag" );
		}

		@Override
		public String getProperty( String key, String def ) {
			String ret = doc.field( key );
			if( ret == null ) ret = def;
			return ret;
		}

		public void load( ODocument doc ) {
			this.doc = doc;
		}
		
	}
	
	@Override
	public List<RecordAbstraction> listRAS( String entity, String rc ) {
		
		NodeID id = dom.create( "/ras" );
		
		final AttributeProvider<RecordAbstraction> ap = new AttributeProvider<RecordAbstraction>() {
			
			OrientRecordAbstraction staticRecordAbstraction = new OrientRecordAbstraction();
			
			@Override
			public RecordAbstraction getValue( ODocument doc ) {
				staticRecordAbstraction.load( doc );
				return staticRecordAbstraction;
			}
		};
		
		if( "".equals( entity ) ) entity = null;
		if( "".equals( rc ) ) rc = null;
		
		if( entity == null & rc == null ) {
			return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null, null, ap );
		}
		if( entity == null ) {
			return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null, "in.rc='" + rc + "'", ap );
		}
		if( rc == null ) {
			return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null, "in.target='" + entity + "'", ap );
		}
		return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null, "in.rc='" + rc + "' and in.target='" + entity + "'", ap );
	}
	
	@Override
	public void saveRAS( RiskAnalysisSession ras ) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public RiskAnalysisSession openRAS( String sid ) {
		return new OrientRAS( dom, sid );
	}

	@Override
	public void destroyRAS( String ras ) {
		NodeID id = dom.get( "/ras/" + ras );
		if( id != null )
			dom.deleteVertex( id );
	}

	@Override
	public boolean existsRAS( String ras ) {
		NodeID root = dom.get( "/ras" );
		if( root == null ) return false;
		return dom.listOutEdgeNames( root, GDomDB.CHILDOF_CLASS, null, null, "in.name='" + ras + "'" ).size() > 0;
	}

	public void execute( String cmd ) {
		dom.getGraph().getRawGraph().command(new OCommandSQL(cmd)).execute();
	}
	
	@Override
	public void createRole( String name ) {
		GAuthDom auth = new GAuthDom( dom );
		auth.createRole( name );
	}

	@Override
	public List<String> listRoles( String domain ) {
		return new GAuthDom( dom ).listRoles();
	}

	@Override
	public List<String> listUsers() {
		String q = "select from ouser where roles in (select from orole where domain.tag='" + dom.getRootName() + "')";
		List<ODocument> list = dom.querySynch( q );
		return new GenericNodeCollection<String>( list, new AttributeProvider<String>() {
			@Override
			public String getValue( ODocument doc ) {
				return doc.field( "name" );
			}} );
	}
	
	public void chpwd( String user, String new_pwd ) {
		execute( 
				"UPDATE ouser SET password = '" + new_pwd + "' WHERE name = '" + user + "'" );
	}
	
	public boolean existsUser( String username ) {
		List<ODocument> list = dom.querySynch( "SELECT FROM ouser WHERE username='" + username + "'" );
		if( list == null ) return false;
		if( list.size() < 1 ) return false;
		return true;
	}

	@Override
	public void storeModelDesc(String modelName, String blobFilename, byte[] modelDescBlob) {
		NodeID id = dom.getVertex( "/models/" + modelName );
		dom.setAttribute( id, "descBlob", modelDescBlob );
		dom.setAttribute( id, "descBlobName", blobFilename );
	}

	@Override
	public byte[] getModelDescBlob(String modelName) {
		NodeID id = dom.getVertex( "/models/" + modelName );
		if( id == null ) return null;
		return dom.getByteAttribute( id, "descBlob" );
	}

	@Override
	public void removeModelDescBlob(String modelName) {
		NodeID id = dom.getVertex( "/models/" + modelName );
		dom.removeAttribute( id, "descBlob");;
	}

	@Override
	public String getModelDescFielname(String modelName) {
		return getModelAttribute(modelName, "descBlobName");
	}

	@Override
	public void deleteModelDesc(String modelName) {
		NodeID id = dom.getVertex( "/models/" + modelName );
		dom.setAttribute(id, "descBlobName", "");
		dom.removeAttribute(id, "descBlob");
	}
	
	@Override
	public void setLayerData( String layer, String key, String value ) {
		NodeID id = dom.get( "/layers/" + layer );
		if( id != null )
			dom.setAttribute( id, key, value );
	}

	@Override
	public String getLayerData( String layer, String key ) {
		NodeID id = dom.get( "/layers/" + layer );
		if( id != null )
			return dom.getAttribute( id, key, "" );
		return null;
	}

	@Override
	public List<String> listUsers( String role ) {
		List<ODocument> list = dom.querySynch( "SELECT FROM ouser WHERE roles in (SELECT FROM orole WHERE name='" + role + "')" );
		return new GenericNodeCollection<String>( list, new AttributeProvider<String>() {
			@Override
			public String getValue( ODocument doc ) {
				return doc.field( "name" );
			}} );
	}

	@Override
	public void setUserRole( String user, String abstractRoleName ) {
		
		// 1: Find domain-specific role
		// 2: Remove current role in domain 
		// 3: Assign the new role
		
		// Delete
		// update ouser remove roles = (select from orole where name='X')
		
		String oldSpecificRole = getRoleOfUser( user );
		
		if( oldSpecificRole != null )
			execute( "update ouser remove roles = (select from orole where name='" + oldSpecificRole + "') where name = '" + user + "'" );
		
		// Add
		// update ouser add roles = (select from orole where name='Y')
		
		String specificRole = getRoleInCurrentDomain( abstractRoleName );
		
		execute( "update ouser add roles = (select from orole where name='" + specificRole + "') where name = '" + user + "'" );
		
		// TODO Auto-generated method stub
		
	}
	
//	private String getRoleOfUser() {
//		String user = dom.getGraphDatabase().getRawGraph().getUser().getName();
//		return getRoleOfUser( user );
//	}
	
	private String getRoleOfUser( String user ) {
		
		String q = "select from (select expand(roles) from (select from ouser where name='" + user + "')) where domain.tag='" + dom.getRootName() + "'";
		
		List<ODocument> list = dom.querySynch( q );
		if( list == null ) return null;
		if( list.size() < 1 ) return null;
		return list.get( 0 ).field( "name" );
	}
	
	private String getRoleInCurrentDomain( String abstractRoleName ) {
		
		String q = "SELECT FROM orole WHERE domain.tag='" + dom.getRootName() + "' " + 
				"and inheritedRole.name='" + abstractRoleName + "'";
		
		List<ODocument> list = dom.querySynch( q );
		if( list == null ) return null;
		if( list.size() < 1 ) return null;
		return list.get( 0 ).field( "name" );
	}

	@Override
	public String getRole( String username ) {
		return getRoleOfUser( username );
	}

	@Override
	public void addPermissions( String rolename, RiscossDBResource res, String perm ) {
		GAuthDom auth = new GAuthDom( this.dom );
		auth.setPermission( rolename, res.name(), perm );
	}

	@Override
	public Collection<String> findEntities( String layer, String query, SearchParams params ) {
		if( layer == null ) layer = "";
		query = query.toLowerCase();
		String queryString = //"in.tag like '%" + query + "%'" + 
				"in.tag.toLowerCase() like '%" + query + "%'" + 
				(params.max > 0 ? " limit " + params.max : "") +
				(params.from > 0 ? " skip " + params.from : "") +
				(params.sort == SearchParams.SORT.AZ ? " order by tag" : "" );
		if( !"".equals( layer ) ) {
			NodeID lid = dom.get( "/layers/" + layer );
			if( lid != null ) {
				return dom.listOutEdgeNames( lid, GDomDB.LINK_CLASS, "contains", null, queryString );
			}
		}
		NodeID id = dom.get( "/entities" );
		return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null, queryString );
		
	}
	
	@Override
	public Collection<String> findLayers( String query, SearchParams params ) {
		query = query.toLowerCase();
		String queryString = "in.tag.toLowerCase() like '%" + query + "%'" + 
				(params.max > 0 ? " limit " + params.max : "") +
				(params.from > 0 ? " skip " + params.from : "") +
				(params.sort == SearchParams.SORT.AZ ? " order by tag" : "" );
		NodeID id = dom.get("/layers");
		return dom.listOutEdgeNames(id, GDomDB.CHILDOF_CLASS, null, null, queryString);
	}
	
	@Override
	public Collection<String> findModels( String query, SearchParams params ) {
		query = query.toLowerCase();
		String queryString = "in.tag.toLowerCase() like '%" + query + "%'" + 
				(params.max > 0 ? " limit " + params.max : "") +
				(params.from > 0 ? " skip " + params.from : "") +
				(params.sort == SearchParams.SORT.AZ ? " order by tag" : "" );
		NodeID id = dom.get("/models");
		return dom.listOutEdgeNames(id, GDomDB.CHILDOF_CLASS, null, null, queryString);
	}
	
	@Override
	public Collection<String> findRCs( String query, SearchParams params ) {
		query = query.toLowerCase();
		String queryString = "in.tag.toLowerCase() like '%" + query + "%'" + 
				(params.max > 0 ? " limit " + params.max : "") +
				(params.from > 0 ? " skip " + params.from : "") +
				(params.sort == SearchParams.SORT.AZ ? " order by tag" : "" );
		NodeID id = dom.get("/risk-configurations");
		return dom.listOutEdgeNames(id, GDomDB.CHILDOF_CLASS, null, null, queryString);
	}
	
	@Override
	public List<RecordAbstraction> findRAS( String query, String target, String rc, SearchParams params ) {
		query = query.toLowerCase();
		target = target.toLowerCase();
		rc = rc.toLowerCase();
		final AttributeProvider<RecordAbstraction> ap = new AttributeProvider<RecordAbstraction>() {
			
			OrientRecordAbstraction staticRecordAbstraction = new OrientRecordAbstraction();
			
			@Override
			public RecordAbstraction getValue( ODocument doc ) {
				staticRecordAbstraction.load( doc );
				return staticRecordAbstraction;
			}
		};
		String queryString = "in.name.toLowerCase() like '%" + query + "%'" + 
				(params.max > 0 ? " limit " + params.max : "") +
				(params.from > 0 ? " skip " + params.from : "") +
				"and in.target.toLowerCase() like '%" + target + "%'" +
				"and in.rc.toLowerCase() like '%" + rc + "%'" +
				(params.sort == SearchParams.SORT.AZ ? " order by tag" : "" );
		NodeID id = dom.get("/ras");
		return dom.listOutEdgeNames(id, GDomDB.CHILDOF_CLASS, null, null, queryString, ap);
	}
	
	@Override
	public String getHTMLReport( String sid ) {
		String xml = getXMLReport( sid );
		
		TransformerFactory tFactory=TransformerFactory.newInstance();

        Source xslDoc = new StreamSource("resources/ras-stylesheet.xslt");
        Source xmlDoc = new StreamSource(new StringReader(xml));

        ByteArrayOutputStream htmlFile = new ByteArrayOutputStream();
        Transformer trasform;
		try {
			trasform = tFactory.newTransformer(xslDoc);
			trasform.transform(xmlDoc, new StreamResult(htmlFile));
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        return htmlFile.toString();
	}
	
	@Override
	public String getXMLReport( String sid ) {
		RiskAnalysisSession ras = openRAS( sid );
		//Inits XML parser
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
        Document doc = docBuilder.newDocument();
        
        doc.setXmlStandalone(true);
        ProcessingInstruction pi = doc.createProcessingInstruction("xml-stylesheet", "type=\"text/xsl\" href=\"ras-stylesheet.xslt\"");
        //Parse results data
        Element rootElement = doc.createElement("riscoss");
        doc.appendChild(rootElement);
        doc.insertBefore(pi, rootElement);
        Element risksession = doc.createElement("risksession");
        risksession.setAttribute("label", ras.getName());
        rootElement.appendChild(risksession);
        
        //Risk session main info
        Element target = doc.createElement("target");
        target.setTextContent(ras.getTarget());
        risksession.appendChild(target);
        
        Element rc = doc.createElement("rc");
        rc.setTextContent(ras.getRCName());
        risksession.appendChild(rc);
        
        Element time = doc.createElement("timestamp");
        Date date = new Date( ras.getTimestamp() );
		SimpleDateFormat sdf = new SimpleDateFormat( "dd-MM-yyyy HH.mm.ss" );
        time.setTextContent(sdf.format(date));
        risksession.appendChild(time);
        
        //Models data
        List<String> modelsList = getModelsFromRiskCfg( ras.getRCName(), ras.getTarget() );
        Element models = doc.createElement("models");
        risksession.appendChild(models);
        for (String modelName : modelsList) {
        	Element model = doc.createElement("model");
        	Element name = doc.createElement("name");
        	name.setTextContent(modelName);
        	model.appendChild(name);
        	models.appendChild(model);
        }
        //Results data
        Element results = doc.createElement("results");
        JSONObject jsonRes = null;
		try {
			jsonRes = new JSONObject(ras.readResults());
			if (!jsonRes.has("hresults")) {
	        	results.setAttribute("type", jsonRes.getJSONArray("results").getJSONObject(0).getString("datatype"));
				jsonRes.put("entity", ras.getTarget());
	        	getSequentialResults(jsonRes, results, doc);
			}
			else {
				results.setAttribute("type", jsonRes.getJSONObject("hresults").getJSONArray("results").getJSONObject(0).getString("datatype"));
				getHierarchycalResults(jsonRes.getJSONObject("hresults"), results, doc);
			}
        } catch (JSONException e) {
			e.printStackTrace();
		}
        risksession.appendChild(results);
        
        //Input data
        Element inputs = doc.createElement("inputs");
        try {
			getInputs(jsonRes.getJSONObject("input"), inputs, doc);
		} catch (JSONException e) {
			e.printStackTrace();
		}
        risksession.appendChild(inputs);
        
        //Argumentation
        Element argumentation = doc.createElement("argumentation");
        try {
			getArguments(jsonRes.getJSONObject("argumentation").getJSONObject("arguments"), argumentation, doc);
		} catch (JSONException e) {
			e.printStackTrace();
		}
        //risksession.appendChild(argumentation);
        Map<String, Node> args = new HashMap<>();
        NodeList el = argumentation.getChildNodes();
        for (int i = 0; i < el.getLength(); ++i) {
        	args.put(el.item(i).getAttributes().item(0).getNodeValue(), el.item(i));
        }
        NodeList res = results.getChildNodes().item(1).getChildNodes();
        for (int i = 0; i < res.getLength(); ++i) {
        	String key = res.item(i).getAttributes().item(0).getNodeValue();
        	if (args.containsKey(key)) {
	        	Node n1 = args.get(key);
	        	Element m = (Element) n1;
	        	m.removeAttribute("id");
	        	res.item(i).appendChild(m);
        	}
        	else res.item(i).appendChild(doc.createElement("argument"));
        }
        
        //Get XML string
        StringWriter sw = new StringWriter();
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = null;
		try {
			transformer = tf.newTransformer();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		}
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
			NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']", 
					doc, 
					XPathConstants.NODESET);
			for (int i = 0; i < nodeList.getLength(); ++i) {
		        Node node = nodeList.item(i);
		        node.getParentNode().removeChild(node);
		    }
			
			transformer.transform(new DOMSource(doc), new StreamResult(sw));
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
        return sw.toString();
	}
	
	private void getArguments(JSONObject jsonObject, Element argumentation, Document doc) {
		Iterator<?> keys = jsonObject.keys();
		
		while (keys.hasNext()) {
			String key = (String) keys.next();
			try {
				Element argument = doc.createElement("argument");
				argument.setAttribute("id", key);
				Element summary = doc.createElement("summary");
				summary.setTextContent(jsonObject.getJSONObject(key).getString("summary"));
				Element truth = doc.createElement("truth");
				truth.setTextContent(jsonObject.getJSONObject(key).getString("truth"));
				
				Element subArgs = doc.createElement("subArgs");
				JSONArray r = jsonObject.getJSONObject(key).getJSONArray("subArgs");
				for (int i = 0; i < r.length(); ++i) {
					Element subArg = doc.createElement("argument");
					appendSubArgs(r.getJSONObject(i), subArg, doc);
					subArgs.appendChild(subArg);
				}
				
				argumentation.appendChild(argument);
				argument.appendChild(summary);
				argument.appendChild(truth);
				argument.appendChild(subArgs);
				
			} catch (DOMException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	}

	private void appendSubArgs(JSONObject object, Element subArg, Document doc) {
		try {
			Element summary = doc.createElement("summary");
			summary.setTextContent(object.getString("summary"));
			Element truth = doc.createElement("truth");
			truth.setTextContent(object.getString("truth"));
			
			Element subArgs = doc.createElement("subArgs");
			JSONArray r = object.getJSONArray("subArgs");
			for (int i = 0; i < r.length(); ++i) {
				Element sub = doc.createElement("argument");
				appendSubArgs(r.getJSONObject(i), sub, doc);
				subArgs.appendChild(sub);
			}
			subArg.appendChild(summary);
			subArg.appendChild(truth);
			subArg.appendChild(subArgs);
			
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	private void getInputs(JSONObject jsonRes, Element inputs, Document doc) {
		Element entity = doc.createElement("entity");
		Element data = doc.createElement("data");
		Element children = doc.createElement("children");
		try {
			entity.setTextContent(jsonRes.getString("entity"));
			JSONArray r = jsonRes.getJSONArray("data");
			for (int i = 0; i < r.length(); ++i) {
				Element event = doc.createElement("input");
				event.setAttribute("id", r.getJSONObject(i).getString("id"));
				Element label = doc.createElement("label");
				label.setTextContent(r.getJSONObject(i).getString("label"));
				Element type = doc.createElement("type");
				type.setTextContent(r.getJSONObject(i).getString("type"));
				Element description = doc.createElement("description");
				description.setTextContent(r.getJSONObject(i).getString("description"));
				Element exposure = doc.createElement("value");
				exposure.setTextContent(String.valueOf(r.getJSONObject(i).getString("value")));
				
				event.appendChild(label);
				event.appendChild(type);
				event.appendChild(description);
				event.appendChild(exposure);
				
				data.appendChild(event);
			}
			inputs.appendChild(entity);
			inputs.appendChild(data);
			
			JSONArray childArray = jsonRes.getJSONArray("children");
			for (int i = 0; i < childArray.length(); ++i) {
				Element child = doc.createElement("child");
				getInputs(childArray.getJSONObject(i), child, doc);
				children.appendChild(child);
			}
			inputs.appendChild(children);
			
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void getHierarchycalResults(JSONObject jsonRes, Element results, Document doc) {
		Element entity = doc.createElement("entity");
		Element res = doc.createElement("res");
		Element children = doc.createElement("children");
		try {
			extractRes(jsonRes, results, doc, entity, res);
			
			JSONArray childArray = jsonRes.getJSONArray("children");
			for (int i = 0; i < childArray.length(); ++i) {
				Element child = doc.createElement("child");
				getHierarchycalResults(childArray.getJSONObject(i), child, doc);
				if (child.hasChildNodes()) children.appendChild(child);
			}
			results.appendChild(children);
			
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

	private void getSequentialResults(JSONObject jsonRes, Element results, Document doc) {
		Element entity = doc.createElement("entity");
		Element res = doc.createElement("res");
		Element children = doc.createElement("children");
		try {
			extractRes(jsonRes, results, doc, entity, res);
			results.appendChild(children);
			
		} catch (DOMException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void extractRes(JSONObject jsonRes, Element results, Document doc,
			Element entity, Element res) throws JSONException {
		entity.setTextContent(jsonRes.getString("entity"));
		JSONArray r = jsonRes.getJSONArray("results");
		for (int i = 0; i < r.length(); ++i) {
			Element event = doc.createElement("event");
			event.setAttribute("id", r.getJSONObject(i).getString("id"));
			Element type = doc.createElement("type");
			if (r.getJSONObject(i).has("type")) type.setTextContent(r.getJSONObject(i).getString("type"));
			else type.setTextContent("Risk");
			Element datatype = doc.createElement("datatype");
			datatype.setTextContent(r.getJSONObject(i).getString("datatype"));
			event.appendChild(type);
			switch (r.getJSONObject(i).getString("datatype")) {
				case "evidence":
					Element label = doc.createElement("label");
					label.setTextContent(r.getJSONObject(i).getString("label"));
					Element description = doc.createElement("description");
					description.setTextContent(r.getJSONObject(i).getString("description"));
					Element exposure = doc.createElement("exposure");
					Double d = r.getJSONObject(i).getJSONObject("e").getDouble("e");
					d = d*(float)100;
					d = Math.floor(d * 100) / 100;
					exposure.setTextContent(String.valueOf(d));
					event.appendChild(label);
					event.appendChild(description);
					event.appendChild(exposure);
					res.appendChild(event);
					break;
				case "distribution":
					Element values = doc.createElement("values");
					JSONArray s = r.getJSONObject(i).getJSONArray("value");
					for (int j = 0; j < s.length(); ++j) {
						Element value = doc.createElement("value");
						Double dd = Double.valueOf(s.getString(j));
						dd = dd*(float)100;
						dd = Math.floor(dd * 100) / 100;
						value.setTextContent(String.valueOf(dd));
						values.appendChild(value);
					}
					Element rank = doc.createElement("rank");
					rank.setTextContent(r.getJSONObject(i).getString("rank"));
					event.appendChild(rank);
					event.appendChild(values);
					res.appendChild(event);
					break;
				default:
					break;
			}
		}
		results.appendChild(entity);
		results.appendChild(res);
	}

	@Override
	public List<String> getScope( String layer ) {
		List<String> scope = new ArrayList<String>();
		OLinkedList olist = new OLinkedList( dom, "/layers" );
		scope.add( layer );
		String next = olist.getNextLayer( layer );
		while( next != null ) {
			scope.add( next);
			next = olist.getNextLayer( next );
		}
		return scope;
	}

	@Override
	public String getName() {
		return dom.getRootName();
	}

	@Override
	public void removeUserFromDomain(String name) {
	
		String oldSpecificRole = getRoleOfUser( name );
		
		if( oldSpecificRole != null )
			execute( "update ouser remove roles = (select from orole where name='" + oldSpecificRole + "') where name = '" + name + "'" );
		
	}

	@Override
	public String getProperty( RiscossElements element, String name, String propertyName, String def ) {
		NodeID id = null;
		switch( element ) {
		case ENTITY:
			id = dom.get( "/entities/" + name );
			break;
		case LAYER:
			id = dom.get( "/layers/" + name );
			break;
		case MODEL:
			id = dom.get( "/models/" + name );
			break;
		case RISKCONF:
			id = dom.get( "/risk-configurations/" + name );
			break;
		case SESSION:
			id = dom.get( "/ras/" + name );
			break;
		default:
			break;
		}
		if( id == null ) {
			return def;
		}
		return dom.getAttribute( id, propertyName, def );
	}

	@Override
	public void setProperty( RiscossElements element, String name, String propertyName, String value ) {
		NodeID id = null;
		switch( element ) {
		case ENTITY:
			id = dom.get( "/entities/" + name );
			break;
		case LAYER:
			id = dom.get( "/layers/" + name );
			break;
		case MODEL:
			id = dom.get( "/models/" + name );
			break;
		case RISKCONF:
			id = dom.get( "/risk-configurations/" + name );
			break;
		case SESSION:
			id = dom.get( "/ras/" + name );
			break;
		default:
			break;
		}
		if( id == null ) {
			return;
		}
		dom.setAttribute( id, propertyName, value );
	}
	
}
