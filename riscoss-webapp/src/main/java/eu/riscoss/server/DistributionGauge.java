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
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import eu.riscoss.reasoner.Distribution;

public class DistributionGauge extends BufferedImage {
	
	Distribution distribution;
	
	Color[] colors = new Color[] {};
	
	int mleft = 0, mtop = 0, mright = 0, mbottom = 0;
	
	Rectangle2D[] bars;
	
	public DistributionGauge( Distribution d, int w, int h, Color[] colors ) {
		
		super( w, h, BufferedImage.TYPE_INT_ARGB );
		
		if( colors == null ) {
			colors = new Color[d.getValues().size()];
			for( int i = 0; i < d.getValues().size(); i++ ) {
				colors[i] =  mkColor( i, d.getValues().size() );
			}
		}
		
		assert( d.getValues().size() == colors.length );
		
		this.distribution = d;
		this.colors = colors;
		
		this.bars = mkBars( d, mleft, mtop, w -(mleft + mright), h -(mtop + mbottom) );
		
		Graphics2D g = (Graphics2D)getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( Color.white );
		g.fillRect( 0, 0, w, h );
		paint( d, g, mleft, mtop, w -(mleft + mright), h -(mtop + mbottom) );
	}

	private void paint( Distribution d, Graphics2D g, int x, int y, int w, int h ) {
		
		for( int i = 0; i < bars.length; i++ ) {
			Rectangle2D r = bars[i];
			g.setColor( colors[i] );
			g.fill( r );
			g.setColor( Color.black );
			g.draw( r );
		}
	}
	
	private Color mkColor( int degree, int max ) {
		int red = (int)(((double)degree / (double)max) * (double)255);
		int green = (int)(((double)(max - degree) / (double)max) * (double)255);
		return new Color( red, green, 0 );
	}
	
	public int findBar( int x, int y ) {
		return findBar( distribution, x, y, mleft, mtop, getWidth() -(mleft + mright), getHeight() -(mtop + mbottom) );
	}
	
	private int findBar( Distribution d, int mx, int my, int x, int y, int w, int h ) {
		
		for( int i = 0; i < bars.length; i++ ) {
			Rectangle2D r = bars[i];
			if( r.getX() < mx )
				if( mx < r.getMaxX() )
					return i;
		}
		
		return -1;
	}
	
	public double findValue( int bar, int y ) {
		double maxy = getHeight() - mtop - mbottom;
		double ry = y - mtop;
		return ((maxy - ry) / maxy);
	}
	
	private Rectangle2D[] mkBars( Distribution d, int x, int y, int w, int h ) {
		Rectangle2D[] rects = new Rectangle2D[d.getValues().size()];
		double step = ((double)w) / d.getValues().size();
		for( int i = 0; i < d.getValues().size(); i++ ) {
			
			double val = d.getValues().get( i );
			
			double xi = x + (step * i);
			double yi = y + h - (val * h);
			double hi = (val * h);
			double wi = step -2;
			
			rects[i] = new Rectangle2D.Double( xi, yi, wi, hi );
		}
		return rects;
	}

	public Distribution getDistribution() {
		return this.distribution;
	}
}
