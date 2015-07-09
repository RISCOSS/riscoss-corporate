package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

public class JMissingData {
	
	String entity = "";
	String layer = "";
	
	List<JDataItem> data = new ArrayList<JDataItem>();
	
	List<JMissingData> children = new ArrayList<JMissingData>();
	
	public JMissingData() {}
	
	public JMissingData( String entity, String layer  ) {
		this.entity = entity;
		this.layer = layer;
	}

	public void add( JMissingData md ) {
		children.add( md );
	}

	public void add( JDataItem item ) {
		data.add( item );
	}

	public String getEntity() {
		return this.entity;
	}

	public List<JDataItem> items() {
		return data;
	}

	public List<JMissingData> children() {
		return this.children;
	}

	public String getLayer() {
		return this.layer;
	}
	
}
