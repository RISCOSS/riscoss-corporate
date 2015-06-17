package eu.riscoss.server;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import eu.riscoss.reasoner.Distribution;
import eu.riscoss.reasoner.Evidence;

public class AuthenticationServlet extends HttpServlet {
	
	private static final long serialVersionUID = 2410335502314521014L;
	
	/*
	Request URI=context path + servlet path + path info+query string

	Request_URI = Context_Path [1] + Servlet_Path [2] + Path_Info [3] + Query_String [4] 
	/catalog[2]/product[3]?mode=view[4]]http://server.com/my_app_context[1]/catalog[2]/product[3]?mode=view[4] 

	Context Path/AppName

	Servlet Path /ABC

	Path Info/Servlet

	Query String param1=value1&m2=value2

	the value of Servlet path and path info depends on the <url-pattern> subelement of <servlet-mapping> element so
	i think ur answer is partially correct(suaitable when the url-pattern defines a exact path match) but Connie has correctly mentioned both the scenario when the url pattern defines a)directory matching(eg. /sample/*) b)exact path matching(eg /sample/test)
	So the answer of the question really depends on how the url-pattern is configured in web.xml
	 */
	protected void doGet( HttpServletRequest req, HttpServletResponse resp ) {
		
		Params params = new Params( req.getQueryString() );
		
	}
	
}
