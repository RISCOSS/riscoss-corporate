package eu.riscoss.shared;

import java.util.ArrayList;
import java.util.List;

public class JSiteMap {
	
	public static class JSitePage {
		
		public JSitePage() {}
		
		public JSitePage( String label, String url, KnownRoles ... roles ) {
			this.label = label;
			this.url = url;
		}
		
		public JSitePage( String label, String url ) {
			this.label = label;
			this.url = url;
		}
		
		String label = "";
		String url = "";
		
		public String getUrl() {
			return this.url;
		}

		public String getLabel() {
			return this.label;
		}
		
	}
	
	public static class JSiteSection {
		
		public JSiteSection() {}
		
		public JSiteSection( String label ) {
			this.label = label;
		}
		
		String label = "";
		
		List<JSitePage>	pages = new ArrayList<>();
		List<JSiteSection>	subsections = new ArrayList<>();
		
		public void add( JSitePage jSitePage ) {
			pages.add( jSitePage );
		}

		public void add( JSiteSection section ) {
			subsections.add( section );
		}
		
		public List<JSiteSection> subsections() {
			return this.subsections;
		}
		
		public List<JSitePage> pages() {
			return this.pages;
		}

		public String getLabel() {
			return this.label;
		}
		
	}
	
	public String		domain;
	public JSiteSection	main = new JSiteSection( "" );
	
	public JSiteSection getRoot() {
		return main;
	}
	
}
