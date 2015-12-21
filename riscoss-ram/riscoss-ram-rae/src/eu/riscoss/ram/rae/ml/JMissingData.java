package eu.riscoss.ram.rae.ml;

import java.util.ArrayList;
import java.util.List;

public class JMissingData {
	
	String entity = "";
	String layer = "";
	
	List<JDataItem> items = new ArrayList<>();
	
	List<JMissingData> children = new ArrayList<>();
	
	public JMissingData( String entity, String layer ) {
		this.entity = entity;
		this.layer = layer;
	}
	
	public void add( JMissingData childMD ) {
		this.children.add( childMD );
	}
	
	public void add( JDataItem item ) {
		this.items.add( item );
	}
	
}
