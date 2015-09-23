package eu.riscoss.server;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import eu.riscoss.db.RiscossDatabase;
import eu.riscoss.db.SiteManager;
import eu.riscoss.shared.CookieNames;

/*
Request URI=context path + servlet path + path info+query string

Request_URI = Context_Path [1] + Servlet_Path [2] + Path_Info [3] + Query_String [4] 
/catalog[2]/product[3]?mode=view[4]]http://server.com/my_app_context[1]/catalog[2]/product[3]?mode=view[4] 

Context Path/AppName

Servlet Path /ABC

Path Info/Servlet

Query String param1=value1&m2=value2

the value of Servlet path and path info depends on the <url-pattern> subelement of <servlet-mapping> element so
i think ur answer is partially correct(suaitable when the url-pattern defines a exact path match) but Connie has 
correctly mentioned both the scenario when the url pattern defines a)directory matching(eg. /sample/*) b)exact 
path matching(eg /sample/test)
So the answer of the question really depends on how the url-pattern is configured in web.xml
 */

public class PageManager {
	
	private HttpServletRequest request;
	
	Map<String,String> cookies = new HashMap<>();
	
	public PageManager( HttpServletRequest req ) {
		
		this.request = req;
		
		Cookie[] cookies = request.getCookies();
		
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				try {
					this.cookies.put( cookie.getName(), java.net.URLDecoder.decode(cookie.getValue(), "UTF-8") );
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	public String getCookie( String name, String def ) {
		String ret = cookies.get( name );
		if( ret == null ) ret = def;
		return ret;
	}
	
	public String getToken() {
		return getCookie( CookieNames.TOKEN_KEY, "" );
	}
	
	public String getDomain() {
		return getCookie( CookieNames.DOMAIN_KEY, "" );
	}
	
	public boolean isAccessAllowed() {
		
		RiscossDatabase db = null;
		
		try {
			db = DBConnector.openDatabase( getToken() );
			
			SiteManager sm = db.getSiteManager();
			
			String url = request.getServletPath();
			url = url.substring( url.lastIndexOf( "/" ) +1 );
			
			return sm.isUrlAllowed( url, getDomain() );
			
		}
		catch( Exception ex ) {
			return false;
		}
		finally {
			if( db != null )
				db.close();
		}
	}
	
	public String getJS() {
		
		String mod = getParameter( "page", "RiscossWebApp" );
		
		return mod + "/" + mod + ".nocache.js";
		
	}
	
	public String getParameter( String name, String def ) {
		
		String mod = request.getParameter( name );
		
		if( mod == null ) mod = def;
		
		return mod;
	}
	
}
