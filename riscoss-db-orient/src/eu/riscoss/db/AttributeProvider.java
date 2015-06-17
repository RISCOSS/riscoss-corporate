package eu.riscoss.db;

import com.orientechnologies.orient.core.record.impl.ODocument;

public interface AttributeProvider<T> {
	
	public T getValue( ODocument doc );
	
}
