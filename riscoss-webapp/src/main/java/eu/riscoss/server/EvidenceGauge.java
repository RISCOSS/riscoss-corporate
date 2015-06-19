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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import eu.riscoss.reasoner.Evidence;

public class EvidenceGauge extends BufferedImage {
	
	Color right_color;
	Color left_color;
	
	Point2D origin;
	
	public EvidenceGauge( Evidence e, int w, int h ) {
		this( e, w, h, Color.red, Color.green );
	}
	
	public EvidenceGauge( Evidence e, int w, int h, Color right_color, Color left_color ) {
		super( w, h, BufferedImage.TYPE_INT_ARGB );
		
		this.right_color = right_color;
		this.left_color = left_color;
		
		Graphics2D g = (Graphics2D)getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( Color.white );
		g.fillRect( 0, 0, w, h );
		paint( e, g, 5, 5, w -10, h -10 );
	}
	
	private void paint( Evidence e, Graphics2D g, int x, int y, int w, int h ) {
		
		g.setColor( Color.black );
		
		origin = pt( x + (w /2), y + h );
		
		double xofs = ((w) /2) /4;
		
		Paint paint = new LinearGradientPaint( 
				new Point2D.Double( x, y + h / 2 ),
				new Point2D.Double( x + w, y + h / 2 ),
				new float[] { 0.1f, 0.9f },
				new Color[] { this.left_color, this.right_color } );
		g.setPaint(paint);
		g.fill( mkArc( origin, 
				rotate( pt( x, y + h ), 
						origin, (90 * (1 - e.getNegative())) ), 
				rotate( pt( x + w, y + h ), 
						origin, -(90 - (90 * e.getPositive())) ), Arc2D.PIE ) );
		
		g.setPaint( null );
		
		g.setColor( Color.black );
		for( int i = 0; i < 4; i++ ) {
			g.draw( mkArc( origin, 
					pt( x + (xofs * i), y + h ), 
					pt( x + w - (xofs * i), y + h ), Arc2D.CHORD ) );
		}
		
		// Create the arros
		GeneralPath path = new GeneralPath();
		
		int top = -(h + y);
		
		path.moveTo( 0, top);
		path.lineTo( 5, top +5 );
		path.lineTo( 2, top +5 );
		path.lineTo( 2, 0 );
		path.lineTo( -2, 0 );
		path.lineTo( -2, top +5 );
		path.lineTo( -5, top +5 );
		path.closePath();
		
		// Rotate the arrow and move it at the right position
		AffineTransform at = new AffineTransform();
		at.rotate(
				Math.toRadians( 90 * e.getDirection() ) );
		path.transform( at );
		at.setToIdentity();
		at.translate( x + (w /2), y + h );
		path.transform( at );
		
		// Draw the arrow
		g.setColor( Color.white );
		g.fill( path );
		g.setColor( Color.black );
		g.draw( path );
	}

	static Point2D pt( double x, double y ) {
		return new Point2D.Double(x, y);
	}
	
	static public Point2D rotate(Point2D pt, Point2D center, double angleDeg) {
		double angleRad = (angleDeg/180)*Math.PI;
		double cosAngle = Math.cos(angleRad );
		double sinAngle = Math.sin(angleRad );
		double dx = (pt.getX()-center.getX());
		double dy = (pt.getY()-center.getY());
		
		return new Point2D.Double( 
				center.getX() + (int) (dx*cosAngle-dy*sinAngle),
				center.getY() + (int) (dx*sinAngle+dy*cosAngle) );
	}
	
	static double dist0( Point2D origin, double x, double y) {
		return Math.sqrt(sqr(x - origin.getX()) + sqr(y - origin.getY()));
	}
	
	// Return polar angle of any point relative to arc center.
	static double angle0( Point2D origin, double x, double y ) {
		return Math.toDegrees(Math.atan2( origin.getY() - y, x - origin.getX() ));
	}
	
	static float sqr(float x) { return x * x; }
	
	static double sqr(double x) { return x * x; }
	
	static double angleDiff(double a, double b) {
		double d = b - a;
		while (d >= 180f) { d -= 360f; }
		while (d < -180f) { d += 360f; }
		return d;
	}
	
	static Arc2D mkArc( Point2D origin, Point2D left, Point2D right, int type ) {
		
		// Get radii of anchor and det point.
		double ra = dist0( origin, left.getX(), left.getY());
		double rd = dist0( origin, right.getX(), right.getY() );
		
		// If either is zero there's nothing else to draw.
		if (ra == 0 || rd == 0) { return new Arc2D.Double(); }
		
		// Get the angles from center to points.
		double aa = angle0( origin, left.getX(), left.getY() );
		double ad = angle0( origin, right.getX(), right.getY() );
		
		// Draw the arc and other dots.
		return new Arc2D.Double( 
				origin.getX() - ra, origin.getY() - ra, // box upper left
				2 * ra, 2 * ra,                  // box width and height
				aa, angleDiff(aa, ad),           // angle start, extent 
				type );
	}
}
