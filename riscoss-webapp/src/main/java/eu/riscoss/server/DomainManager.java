package eu.riscoss.server;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

@Path("domains")
public class DomainManager {
	
	@POST @Path("selected")
	public String setSessionSelectedDomain( @Context HttpServletRequest req, @QueryParam("domain") String domain ) {
		
		req.getSession( true ).setAttribute( "domain", domain );
		
		return new JsonPrimitive("Ok").toString();
	}
	
	@GET @Path("selected")
	public String getSessionSelectedDomain( @Context HttpServletRequest req ) {
		
		// If not domains have been configured, use a default one
		String initString = req.getServletContext().getInitParameter( "eu.riscoss.param.domains.list" );
		if( initString == null ) return new JsonPrimitive( "Playground" ).toString();
		
		Object o = req.getSession( true ).getAttribute( "domain" );
		if( o == null ) return null;
		return new JsonPrimitive( o.toString() ).toString();
	}
	
	@GET @Path("predefined/list")
	public String getSessionPredefinedList( @Context HttpServletRequest req ) {
		
		JsonArray array = new JsonArray();
		
		String initString = req.getServletContext().getInitParameter( "eu.riscoss.param.domains.list" );
		
		if( initString != null ) {
			String[] tokens = initString.split( "[,]" );
			for( String tok : tokens ) {
				array.add( new JsonPrimitive( tok ) );
			}
		}
		
		return array.toString();
	}
	
}
