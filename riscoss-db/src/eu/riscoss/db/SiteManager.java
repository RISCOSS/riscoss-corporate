package eu.riscoss.db;

import java.util.Collection;
import java.util.List;

public interface SiteManager {

	void createPage( String sectionName, String label, String url, String ... roles );
	
	void deletePage( String sectionName, String label );

	void createSection( String sectionName );
	
	List<String> listSections( String parentSection );
	
	List<String> listPages( String parentSection );
	
	Collection<String> listRoles( String url );

	String getUrl( String label );

	boolean isAllowed( String path, String domain );

	boolean isUrlAllowed( String url, String domain );

	void createRole( String name );

	void init();
	
}
