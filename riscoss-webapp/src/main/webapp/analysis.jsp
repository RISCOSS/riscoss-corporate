<%@page import="eu.riscoss.db.*"%>
<%@page import="eu.riscoss.server.*"%>
<%@ page import="eu.riscoss.shared.*" %>

<!doctype html>

<%
	PageManager pg = new PageManager( request );
	
	String mod = pg.getParameter( "type", "analysis" );
		System.out.println( mod );
	mod = mod + "/" + mod + ".nocache.js";
 %>
		
<html style="height:100%">
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <link type="text/css" rel="stylesheet" href="RiscossWebApp.css">
    <title>RISCOSS Web App</title>
    <% if( pg.isAccessAllowed() ) { %> <script type="text/javascript" language="javascript" src="<%=mod%>"></script> <% } %>
  </head>

  <body style="height:100%">

    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex='-1' style="position:absolute;width:0;height:0;border:0"></iframe>

    <noscript>
      <div style="width: 22em; position: absolute; left: 50%; margin-left: -11em; color: red; background-color: white; border: 1px solid red; padding: 4px; font-family: sans-serif">
        Your web browser must have JavaScript enabled
        in order for this application to display correctly.
      </div>
    </noscript>

    <% if( !pg.isAccessAllowed() ) { %> Unauthorized access. <% } %>
    
  </body>
</html>

