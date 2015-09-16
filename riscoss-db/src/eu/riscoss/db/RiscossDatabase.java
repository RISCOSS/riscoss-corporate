package eu.riscoss.db;

import java.util.List;

public interface RiscossDatabase {
	
	public void init();
	
	public String getUsername();
	
	public List<String> listDomains();

	public void close();

	public void createDomain( String domainName );

	public String getRole();

	public void setRoleProperty( String role, String key, String value );

	public String getRoleProperty( String role, String key, String def );

	public List<String> listRoles();

	public List<String> listUsers( String from, String max, String pattern );

	public List<String> listPublicDomains();

	public void setPredefinedRole( String domain, String value );
	public String getPredefinedRole( String domain );

	public void createRole( String roleName );

	public List<String> listDomains( String username );

	public boolean isAdmin();

	public SiteManager getSiteManager();
	
}
