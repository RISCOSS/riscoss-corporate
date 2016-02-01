package eu.riscoss.db.domdb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class OLinkedList {
	
	String rootPath;
	GDomDB dom;
	
	public OLinkedList( GDomDB dom, String path ) {
		this.rootPath = path;
		this.dom = dom;
	}
	
	public String getName( NodeID id ) {
		return dom.getAttribute( id, "tag", "-" );
	}
	
	public String getNextLayer( String name ) {
		NodeID id = dom.getVertex( this.rootPath + "/" + name );
		if( id == null ) return null;
		id = getNextLayer( id );
		if( id == null ) return null;
		return getName( id );
	}
	
	public NodeID getNextLayer( NodeID prev ) {
		List<NodeID> list = dom.listOutEdges( prev, GDomDB.LINK_CLASS, "next", null );
		if( list == null ) return null;
		if( list.size() < 1 ) return null;
		return list.get( 0 );
	}
	
	public NodeID getTopLayer() {
		NodeID layers = dom.getVertex( this.rootPath );
		if( layers == null ) return null;
		List<NodeID> list = dom.listOutEdges( layers, GDomDB.LINK_CLASS, "first", null );
		if( list == null ) return null;
		if( list.size() < 1 ) return null;
		return list.get( 0 );
	}
	
	NodeID getLayersID() {
		return dom.getVertex( this.rootPath );
	}
	
	public void setTopLayer( NodeID layer ) {
		NodeID layers = getLayersID();
		NodeID old_top = getTopLayer();
		dom.rmlink( "/layers", dom.getPath( old_top ), "first" );
		if( layer != null ) {
			dom.link( layers, layer, "first" );
			if( old_top != null )
				dom.link( layer, old_top, "next" );
		}
	}
	
	public NodeID getBottomLayer() {
		for( NodeID id = getTopLayer(); id != null;  ) {
			NodeID next = getNextLayer( id );
			if( next == null ) return id;
			id = next;
		}
		return null;
	}
	
	public void addLayer( String name, String after ) {
		if( name == null ) return;
		if( "".equals( name ) ) return;
		if( name.contains( "/" ) ) return;
		NodeID id = dom.create( this.rootPath + "/" + name );
		if( after == null ) after = "$leaf";
		if( "$leaf".equals( after ) ) {
			NodeID last = getBottomLayer();
			if( last != null ) {
				setNextLayer( last, id );
			}
			else {
				setTopLayer( id );
			}
		}
		else if( "$root".equals( after ) ) {
			setTopLayer( id );
		}
		else {
			NodeID previous = dom.getVertex( this.rootPath + "/" + after );
			if( previous != null ) {
				NodeID next = getNextLayer( previous );
				if( next != null ) {
					for( NodeID rem = getNextLayer( previous ); rem != null; ) {
						dom.rmlink( previous, rem, "next" );
						rem = getNextLayer( previous );
					}
					dom.link( id, next, "next" );
					dom.link( previous, id, "next" );
				}
				else {
					dom.link( previous, id, "next" );
				}
			}
			else {
				setTopLayer( id );
			}
		}
	}
	
	public void setNextLayer( NodeID layer, NodeID next ) {
		NodeID oldnext = getNextLayer( layer );
		if( oldnext != null ) {
			dom.rmlink( layer, oldnext, "next" );
		}
		if( next != null )
			dom.link( layer, next, "next" );
	}

	public Collection<NodeID> list() {
		ArrayList<NodeID> list = new ArrayList<>();
		
		NodeID id = getTopLayer();
		
		while( id != null ) {
			list.add( id );
			id = getLink( id, "next" );
		}
		
		return list;
	}

	public void editParent(String layer, String newParent) {
		NodeID id = dom.getVertex(this.rootPath + "/" + layer);
		
		//case 1: root layer
		if (id.compareTo(getTopLayer()) == 0) {
			NodeID newTop = getNextLayer(id);
			NodeID layers = getLayersID();
			dom.rmlink( "/layers", dom.getPath( id ), "first" );
			dom.link( layers, newTop, "first" );
			
			dom.rmlink(id, newTop, "next");
			NodeID parent = dom.getVertex(this.rootPath + "/" + newParent);
			if (parent.compareTo(getBottomLayer()) == 0) {
				setNextLayer(parent, id);
			} else {
				NodeID next = getNextLayer(parent);
				dom.rmlink(parent, next, "next");
				setNextLayer(parent, id);
				setNextLayer(id, next);
			}
		} 
		//case 2: bottom layer
		else if (id.compareTo(getBottomLayer()) == 0) {
			NodeID previous = null;
			for( NodeID prev : list() ) {
				if( getNextLayer( prev ).compareTo( id ) == 0 ) {
					previous = prev;
					break;
				}
			}
			dom.rmlink(previous, id, "next");
			if (newParent.equals("[top]")) {
				NodeID newTop = id;
				NodeID layers = getLayersID();
				NodeID oldLayer = getTopLayer();
				dom.rmlink( "/layers", dom.getPath( oldLayer ), "first" );
				dom.link( layers, newTop, "first" );
				setNextLayer(id, oldLayer);
			} else {
				NodeID parent = dom.getVertex(this.rootPath + "/" + newParent);
				NodeID next = getNextLayer(parent);
				dom.rmlink(parent, next, "next");
				setNextLayer(parent, id);
				setNextLayer(id, next);
			}
		} 
		//case 3: middle layer
		else {
			NodeID previous = null;
			for( NodeID prev : list() ) {
				if( getNextLayer( prev ).compareTo( id ) == 0 ) {
					previous = prev;
					break;
				}
			}
			NodeID next = getNextLayer(id);
			dom.rmlink(previous, id, "next");
			dom.rmlink(id, next, "next");
			setNextLayer(previous, next);
			if (newParent.equals("[top]")) {
				NodeID newTop = id;
				NodeID layers = getLayersID();
				NodeID oldLayer = getTopLayer();
				dom.rmlink( "/layers", dom.getPath( oldLayer ), "first" );
				dom.link( layers, newTop, "first" );
				setNextLayer(id, oldLayer);
			} else {
				NodeID parent = dom.getVertex(this.rootPath + "/" + newParent);
				if (parent.compareTo(getBottomLayer()) == 0) {
					setNextLayer(getBottomLayer(), id);
				}
				else {
					NodeID nextParent = getNextLayer(parent);
					dom.rmlink(parent, nextParent, "next");
					setNextLayer(parent, id);
					setNextLayer(id, nextParent);
				}
			}
		}
	}
	
	public void removeLayer(String name) {
		NodeID id = dom.getVertex( this.rootPath + "/" + name );
		
		if( id == null ) return;
		
		// case 1: we are removing the root
		if( id.compareTo( getTopLayer() ) == 0 ) {
			setTopLayer( getNextLayer( getTopLayer() ) );
			dom.deleteVertex( id );
		}
		// case 2: any other layer
		else {
			NodeID previous = null;
			for( NodeID prev : list() ) {
				if( getNextLayer( prev ).compareTo( id ) == 0 ) {
					previous = prev;
					break;
				}
			}
			if( previous != null ) {
				setNextLayer( previous, getNextLayer( id ) );
			}
			dom.deleteVertex( id );
		}
	}
	
	
	
	public void renameLayer(String name, String newName) {
		NodeID id = dom.getVertex( this.rootPath + "/" + name );
		
		if( id == null ) 
			return;
		
		dom.setAttribute(id, "tag", newName);
	}
	
	NodeID getLink( NodeID from, String link ) {
		List<NodeID> list = dom.listOutEdges( from, GDomDB.LINK_CLASS, link, null );
		if( list == null ) return null;
		if( list.size() < 1 ) return null;
		return list.get( 0 );
	}

	public Collection<String> names() {
		ArrayList<String> list = new ArrayList<>();
		
		NodeID id = getTopLayer();
		
		while( id != null ) {
			list.add( getName( id ) );
			id = getLink( id, "next" );
		}
		
		return list;
	}

	public void renameEntity(String name, String newName) {
		NodeID id = dom.getVertex( this.rootPath + "/" + name );
		
		if( id == null ) 
			return;
		
		dom.setAttribute(id, "tag", newName);
	}
	
}
