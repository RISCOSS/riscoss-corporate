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

public class GaugeServlet extends HttpServlet {

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

		System.out.println( "GaugeServlet" );

		Params params = new Params( req.getQueryString() );

		if( "e".equals( params.get( "type" ) ) ) {
			try {
				String str_p = params.get( "p" );
				String str_m = params.get( "m" );
				double p = Double.parseDouble( str_p );
				double m = Double.parseDouble( str_m );
				OutputStream out = resp.getOutputStream();
				int w = 100; int h = 100;
				if( params.get( "w" ) != null ) try {
					w = Integer.parseInt( params.get( "w" ) );
				} catch( Exception ex ) {}
				if( params.get( "h" ) != null ) try {
					h = Integer.parseInt( params.get( "h" ) );
				} catch( Exception ex ) {}
				BufferedImage img = new EvidenceGauge( new Evidence( p, m ), w, h );
				ImageIO.write( img, "png", out );
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else if( "d".equals( params.get( "type" ) ) ) {
			try {
				String value = params.get( "f", "" );
				Distribution dist = new Distribution();
				OutputStream out = resp.getOutputStream();
				
				String[] parts = value.split( "[;]" );
				Double[] d = new Double[parts.length];
				for( int i = 0; i < parts.length; i++ ) {
					d[i] = Double.parseDouble( parts[i] );
				}
				dist.setValues( Arrays.asList( d ) );
				
				// ensures that the distribution sum is 1
				flatten( dist, -1 );
				
				BufferedImage img = new DistributionGauge( dist, 100, 100, null );
				ImageIO.write( img, "png", out );
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private double diff(Distribution d) {
		double diff = 1;
		for( Double val : d.getValues() ) {
			diff -= val;
		}
		return diff;
	}

	void flatten( Distribution d, int fixedBar ) {
		List<Double> v = d.getValues();
		double diff = diff( d );
		for( int i = 0; i < v.size(); i++ ) {
			if( diff == 0 ) break;
			if( i == fixedBar ) continue;
			double n = v.get( i ) + diff;
			if( n < 0 ) n = 0;
			if( n > 1 ) n = 1;
			v.set( i, n );
			diff = diff( d );
		}
	}

}
