<%@page import="eu.riscoss.db.RiscossDatabase"%>
<%@page import="eu.riscoss.server.DBConnector"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<%@ page import="eu.riscoss.db.RiscossDatabase" %>
<%@ page import="eu.riscoss.shared.CookieNames" %>

<% 
	
String token = "";

Cookie[] cookies = request.getCookies();

if (cookies != null) {
 for (Cookie cookie : cookies) {
   if (cookie.getName().equals(CookieNames.TOKEN_KEY)) {
	token = cookie.getValue();
    }
  }
}
	
	try {
		RiscossDatabase db = DBConnector.openDatabase( token );
		db.close();
		%><jsp:include page="RiscossWebApp.html" flush="true" /><%
	}
	catch( Exception ex ) { %>
		<jsp:include page="auth.html" flush="true" />
	<%}
	
%>

