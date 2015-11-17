package eu.riscoss.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.riscoss.db.domdb.GDomDB;
import eu.riscoss.db.domdb.NodeID;

public class OrientRAS implements RiskAnalysisSession {
	
	GDomDB dom;
	String id;
	
	Map<String,NodeID> inputs = new HashMap<String,NodeID>();
	Map<String,NodeID> outputs = new HashMap<String,NodeID>();
	
	public OrientRAS( GDomDB dom ) {
		this.dom = dom;
		this.id = new Date().getTime() + ":" + hashCode();
		dom.create( path() );
	}
	
	public OrientRAS( GDomDB dom, String id ) {
		this.dom = dom;
		this.id = id;
		for( NodeID eid : dom.children( path() + "/entities/" ) ) {
			{
				NodeID oid = dom.getChild( eid, "inputs" );
				if( oid == null ) {
					dom.createChild( eid, "inputs" );
					inputs.put( dom.getName( eid, null ), oid );
				}
			}
			{
				NodeID oid = dom.getChild( eid, "outputs" );
				if( oid == null ) {
					oid = dom.createChild( eid, "outputs" );
				}
				outputs.put( dom.getName( eid, null ), oid );
			}
		}
	}
	
	protected String path() {
		return "/ras/" + id;
	}
	
	protected String getAttribute( String path, String attribute, String def ) {
		NodeID id = dom.create( path );
		String ret = dom.getAttribute( id, attribute, def );
		if( ret == null ) ret = def;
		return ret;
	}
	
	protected void setAttribute( String path, String key, String value ) {
		NodeID id = dom.create( path );
		if( id != null ) {
			dom.setAttribute( id, key, value );
		}
	}
	
	@Override
	public void setName( String name ) {
		setAttribute( path(), "name", name );
	}
	
	@Override
	public String getName() {
		return getAttribute( path(), "name", id );
	}
	
	@Override
	public void setLayers( Collection<String> layers ) {
		dom.deleteChildren( path() + "/layers" );
		for( String layer : layers ) {
			dom.create( path() + "/layers/" + layer );
		}
	}
	
	@Override
	public long getTimestamp() {
		NodeID id = dom.get( path() );
		try {
			return Long.parseLong( dom.getAttribute( id, "timestamp", "0" ) );
		}
		catch( Exception ex ) {
			return 0;
		}
	}
	
	@Override
	public void setTimestamp( long timestamp ) {
		NodeID id = dom.get( path() );
		dom.setAttribute( id, "timestamp", "" + timestamp );
	}
	
	@Override
	public void addEntity( String entity, String layer ) {
		NodeID eid = dom.create( path() + "/entities/" + entity );
		NodeID id = dom.createChild( eid, "inputs" );
		inputs.put( entity, id );
		id = dom.createChild( eid, "outputs" );
		outputs.put( entity, id );
		dom.create( path() + "/layers/" + layer + "/entities" );
		dom.link( 
				path() + "/layers/" + layer + "/entities", 
				path() + "/entities/" + entity, "layer-entity" );
	}
	
	@Override
	public String getTarget() {
		return getAttribute( path(), "target",  null );
	}
	
	@Override
	public void setTarget( String entity ) {
		setAttribute( path(), "target", entity );
	}
	
	@Override
	public void setRCName( String rc ) {
		setAttribute( path(), "rc", rc );
	}
	
	@Override
	public void setRCModels( Map<String, ArrayList<String>> rcModels ) {
		for( String layer : rcModels.keySet() ) {
			for( String model : rcModels.get( layer ) ) {
				dom.create( path() + "/layers/" + layer + "/models/" + model );
			}
		}
	}
	
	@Override
	public String getId() {
		return this.id;
	}
	
	@Override
	public void setResult( String layer, String entity, String id, String attribute, String value ) {
		NodeID oid = outputs.get( entity );
		if( oid != null ) {
			NodeID cid = dom.getChild( oid, id );
			if( cid == null ) {
				cid = dom.createChild( oid, id );
			}
			dom.setAttribute( cid, attribute, value );
		}
		setAttribute( path() + "/entities/" + entity + "/outputs/" + id, attribute, value );
	}
	
	@Override
	public String getResult( String layer, String entity, String indicatorId, String attribute, String def ) {
		NodeID oid = outputs.get( entity );
		if( oid != null ) {
			oid = dom.getChild( oid, indicatorId );
			if( oid == null ) return def;
			return dom.getAttribute( oid, attribute, def );
		}
		return getAttribute( path() + "/entities/" + entity + "/outputs/" + indicatorId, attribute, def );
	}
	
	@Override
	public List<String> getModels( String layer ) {
		NodeID id = dom.create( path() + "/layers/" + layer + "/models/" );
		return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null );
	}
	
	@Override
	public String getLayer( int i ) {
		return dom.getName( dom.children( path() + "/layers" ).get( i ), null );
	}
	
	@Override
	public String getRCName() {
		return getAttribute( path(), "rc", "" );
	}
	
	@Override
	public int getLayerCount() {
		return dom.children( path() + "/layers" ).size();
	}
	
	@Override
	public int getEntityCount( String layer ) {
		NodeID id = dom.create( path() + "/layers/" + layer + "/entities" );
		return dom.listOutEdgeNames( id, GDomDB.LINK_CLASS, "layer-entity", null, null ).size();
	}
	
	@Override
	public String getEntity( String layer, int index ) {
		NodeID id = dom.create( path() + "/layers/" + layer + "/entities" );
		return dom.listOutEdgeNames( id, GDomDB.LINK_CLASS, "layer-entity", null, null ).get( index );
	}
	
	@Override
	public Collection<String> getResults( String layer, String entity ) {
		NodeID id = dom.create( path() + "/entities/" + entity + "/outputs" );
		if( id == null ) return new ArrayList<String>();
		return dom.listOutEdgeNames( id, GDomDB.CHILDOF_CLASS, null, null, null );
	}
	
	@Override
	public void saveInput( String entity, String indicator_id, String origin, String value ) {
		NodeID id = inputs.get( entity );
		if( id != null ) {
			NodeID cid = dom.getChild( id, indicator_id );
			if( cid == null ) {
				cid = dom.createChild( id, indicator_id );
			}
			dom.setAttribute( cid, "value", value );
			dom.setAttribute( cid, "origin", origin );
		}
		setAttribute( path() + "/entities/" + entity + "/inputs/" + indicator_id, "value", value );
		setAttribute( path() + "/entities/" + entity + "/inputs/" + indicator_id, "origin", origin );
	}
	
	@Override
	public String getInput( String entity, String indicator_id ) {
		NodeID id = inputs.get( entity );
		if( id != null ) {
			id = dom.getChild( id, indicator_id );
			if( id == null ) return null;
			return dom.getAttribute( id, "value", null );
		}
		return getAttribute( path() + "/entities/" + entity + "/inputs/" + indicator_id, "value", null );
	}
	
	public List<String> listInputs( String entity ) {
		return dom.listChildren( path() + "/entities/" + entity + "/inputs" );
	}
	
	@Override
	public void setStatus( String entity, String value ) {
		setAttribute( path() + "/entities/" + entity, "status", value );
	}
	
	@Override
	public String getOption( String key, String def ) {
		return getAttribute( path(), key, def );
	}
	
	@Override
	public void setOption( String key, String value ) {
		setAttribute( path(), key, value );
	}
	
	@Override
	public String getLayer( String entity ) {
//		TimeDiff.get().log( "getLayer( " + entity + " )" );
		NodeID id = dom.create( path() + "/entities/" + entity );
		if( id == null ) return null;
		
		List<NodeID> list = dom.listInEdges( id, GDomDB.LINK_CLASS, "layer-entity" );
		if( list == null ) return null;
		if( list.size() < 1 ) return null;
		NodeID entityWrapper = list.get( 0 );
		String ret = dom.getParentName( entityWrapper );
//		TimeDiff.get().log( "getLayer::return" );
		return ret;
	}
	
	@Override
	public void setParent( String child, String parent ) {
		dom.link( path() + "/entities/" + parent, path() + "/entities/" + child, "entity:parent-child" );
	}

	@Override
	public Collection<String> getChildren( String entity ) {
//		TimeDiff.get().log( "getChildren( " + entity + " )" );
		Collection<String> ret = dom.listOutEdgeNames( dom.create( path() + "/entities/" + entity ), GDomDB.LINK_CLASS, "entity:parent-child", null, null );
//		TimeDiff.get().log( "getChildren:: return" );
		return ret;
	}

	@Override
	public void saveResults( String json ) {
		setAttribute( path(), "last-results", json );
	}

	@Override
	public String readResults() {
		return getAttribute( path(), "last-results", "" );
	}

	@Override
	public List<String> getEntities( String layer ) {
		NodeID id = dom.create( path() + "/layers/" + layer + "/entities" );
		return dom.listOutEdgeNames( id, GDomDB.LINK_CLASS, "layer-entity", null, null );
	}

	@Override
	public Map<String, Object> getResult( String layer, String entity, String indicator ) {
		NodeID oid = outputs.get( entity );
		if( oid != null ) {
			oid = dom.getChild( oid, indicator );
			if( oid == null ) return null;
			return dom.getAttributes( oid );
		}
		return null;
	}
	
	@Override
	public void storeModelBlob( String name, String layer, String blob ) {
		dom.create( path() + "/models/" + name );
		setAttribute( path() + "/models/" + name, "layer", layer );
		setAttribute( path() + "/models/" + name, "blob", blob );
	}
	
	@Override
	public String getStoredModelBlob( String modelName ) {
		return getAttribute( path() + "/models/" + modelName, "blob", null );
	}

	@Override
	public void setEntityAttribute( String entity, String key, String value ) {
		setAttribute( path() + "/entities/" + entity, key, value );
	}

	@Override
	public String getEntityAttribute( String entity, String key, String def ) {
		return getAttribute( path() + "/entities/" + entity, key, def);
	}

	@Override
	public RiskScenario getScenario( String name ) {
		if( name == null ) {
			throw new RuntimeException( "<name> can not be null" );
		}
		while( name.endsWith( "/" ) ) {
			name = name.substring( 0, name.lastIndexOf( "/" ) );
		}
		while( name.startsWith( "/" ) ) {
			name = name.substring( 1 );
		}
		return new ORiskScenario( this, name );
	}

//	private RiskScenario withScenario( String name ) {
//		this.scenario = name;
//		return this;
//	}
//	
//	@Override
//	public void set( String key, String value ) {
//		setAttribute( path() + "/scenarios/" + this.scenario, key, value);
//	}
//
//	@Override
//	public String get( String key, String def ) {
//		return getAttribute( path() + "/scenarios/" + this.scenario, key, def );
//	}
	
}
