package eu.riscoss.db;

import java.util.Iterator;
import java.util.List;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class GenericNodeIterator<T> implements Iterator<T> {
	
	Iterator<ODocument>	it;
	
	AttributeProvider<T> provider;
	
	public GenericNodeIterator( List<ODocument> l, AttributeProvider<T> provider ) {
		this.it = l.iterator();
		this.provider = provider;
	}
	
	@Override
	public boolean hasNext() {
		return it.hasNext();
	}
	
	@Override
	public T next() {
		ODocument v = it.next();
		return provider.getValue( v );
//		return new NodeID( v.getIdentity().toString() );
	}
	
	@Override
	public void remove() {
		throw new RuntimeException( "Unsupported operation" );
	}
	
}
