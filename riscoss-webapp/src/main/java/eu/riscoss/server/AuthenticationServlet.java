/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/**
 * @author 	Alberto Siena
**/

package eu.riscoss.server;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
		
//		Params params = new Params( req.getQueryString() );
		
	}
	
}
