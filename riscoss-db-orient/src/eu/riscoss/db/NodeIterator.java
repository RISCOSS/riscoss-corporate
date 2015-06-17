package eu.riscoss.db;

import java.util.Iterator;
import java.util.List;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class NodeIterator implements Iterator<NodeID> {
	
	Iterator<ODocument>	it;
	
	public NodeIterator( List<ODocument> l ) {
		this.it = l.iterator();
	}
	
	@Override
	public boolean hasNext() {
		return it.hasNext();
	}
	
	@Override
	public NodeID next() {
		ODocument v = it.next();
		return new NodeID( v.getIdentity().toString() );
	}
	
	@Override
	public void remove() {
		throw new RuntimeException( "Unsupported operation" );
	}
	
}
