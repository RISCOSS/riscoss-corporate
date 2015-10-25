package eu.riscoss.db.domdb;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class GenericNodeCollection<T> implements List<T> {
	
	List<ODocument>	list;
	AttributeProvider<T> provider;
	
	
	public GenericNodeCollection( List<ODocument> l, AttributeProvider<T> p ) {
		this.list = l;
		this.provider = p;
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
	public Iterator<T> iterator() {
		return new GenericNodeIterator<T>( list, provider );
	}

	@Override
	public Object[] toArray() {
		throw new RuntimeException( "Unsupported operation" );
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] a) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public boolean add(T e) {
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
	public boolean addAll(Collection<? extends T> c) {
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
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public T get(int index) {
		return provider.getValue( list.get( index ) );
//		return new NodeID( list.get( index ).getIdentity().toString() );
	}

	@Override
	public T set(int index, T element) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public void add(int index, T element) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public T remove(int index) {
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
	public ListIterator<T> listIterator() {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		throw new RuntimeException( "Unsupported operation" );
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		throw new RuntimeException( "Unsupported operation" );
	}
	
}
