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
	
	public String getPage( String title, String module ) {
		
		String s = "";
		
		s += "<!doctype html>\n";
		s += "<html style=\"height:100%\">\n";
		s += "<head>\n";
		s += "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">\n";
		s += "<link type=\"text/css\" rel=\"stylesheet\" href=\"RiscossWebApp.css\">\n";
		s += "<title>" + title + "</title>\n";
		if( isAccessAllowed() ) { 
			s += "<script type=\"text/javascript\" language=\"javascript\" src=\"" + module + "/" + module + ".nocache.js\"></script>\n";
		}
		s += "</head>\n";
		s += "<body style=\"height:100%\">\n";
		s += "<iframe src=\"javascript:''\" id=\"__gwt_historyFrame\" tabIndex='-1' style=\"position:absolute;width:0;height:0;border:0\"></iframe>\n";
		s += "<noscript>";
		s += "<div style=\"width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif\">\n";
		s += "Your web browser must have JavaScript enabled in order for this application to display correctly.\n";
		s += "</div>\n";
		s += "</noscript>\n";
		if( !isAccessAllowed() ) { 
			s += "Unauthorized access.\n";
		}
		s += "</body>\n";
		s += "</html>\n";
		
		return s;

	}
}
