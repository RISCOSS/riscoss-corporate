<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<%@page import="eu.riscoss.db.RiscossDatabase"%>
<%@page import="eu.riscoss.server.DBConnector"%>

<%@ page import="eu.riscoss.db.RiscossDatabase"%>
<%@ page import="eu.riscoss.shared.CookieNames"%>

<%@ page import="javax.script.Invocable"%>
<%@ page import="javax.script.ScriptContext"%>
<%@ page import="javax.script.ScriptEngine"%>
<%@ page import="javax.script.ScriptEngineManager"%>

<% 
	
String token = null;

Cookie[] cookies = request.getCookies();

if (cookies != null) {
 for (Cookie cookie : cookies) {
   if (cookie.getName().equals(CookieNames.TOKEN_KEY)) {
	    token = java.net.URLDecoder.decode(cookie.getValue(), "UTF-8");
    }
  }
}
	
	if( token != null ) {
		try {
			RiscossDatabase db = DBConnector.openDatabase( token );
			db.close();
			%>
				<jsp:include page="home.jsp" flush="true"/>
			<% return;
		}
		catch( Exception ex ) { 
			System.err.println( ex.getMessage() );
			Cookie cookie = new Cookie( CookieNames.TOKEN_KEY, "" );
			cookie.setMaxAge(0);
			cookie.setValue("");
			cookie.setPath("/");
			response.addCookie(cookie);
		}
	}
		
		// Authentication error or no token at all: send login page
		%> <jsp:include page="auth.jsp" flush="true" />

