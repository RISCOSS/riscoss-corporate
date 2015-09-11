package eu.riscoss.shared;

import java.util.ArrayList;

public class JSiteMap {
	
	ArrayList<JPageInfo> pages = new ArrayList<JPageInfo>();
	
	public void add( JPageInfo pg ) {
		this.pages.add( pg );
	}
	
}
