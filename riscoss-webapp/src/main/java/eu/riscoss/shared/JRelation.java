package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

public class JRelation {

	public String stereotype;
	
	JProposition target;
	List<JProposition> sources = new ArrayList<JProposition>();

	public void setTarget( JProposition p ) {
		this.target = p;
	}

	public void addSource( JProposition jsource ) {
		this.sources.add( jsource );
	}
	
}
