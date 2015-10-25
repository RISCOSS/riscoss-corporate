package eu.riscoss.db.domdb;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class NodeCollection implements List<NodeID> {
	
	List<ODocument>	list;
	
	NodeCollection( List<ODocument> l ) {
		this.list = l;
	}
	
	@Override
	public int size() {
		return list.size();
	}

	@Override
	public boolean isEmpty() {
		return list.isEmpty();
	}

	@Override
	public boolean contains( Object o ) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public Iterator<NodeID> iterator() {
		return new NodeIterator( list );
	}

	@Override
	public Object[] toArray() {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public boolean add(NodeID e) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public boolean remove(Object o) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public boolean addAll(Collection<? extends NodeID> c) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public void clear() {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public boolean addAll(int index, Collection<? extends NodeID> c) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public NodeID get(int index) {
		return new NodeID( list.get( index ).getIdentity().toString() );
	}

	@Override
	public NodeID set(int index, NodeID element) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public void add(int index, NodeID element) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public NodeID remove(int index) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public int indexOf(Object o) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public int lastIndexOf(Object o) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public ListIterator<NodeID> listIterator() {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public ListIterator<NodeID> listIterator(int index) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public List<NodeID> subList(int fromIndex, int toIndex) {
		throw new RuntimeException( "Unsupported operation" );
	}
	
}
