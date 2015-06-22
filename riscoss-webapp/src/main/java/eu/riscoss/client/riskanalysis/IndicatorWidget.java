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

package eu.riscoss.client.riskanalysis;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.kiouri.sliderbar.client.event.BarValueChangedEvent;
import com.kiouri.sliderbar.client.event.BarValueChangedHandler;
import com.kiouri.sliderbar.client.solution.adv.AdvancedSliderBar;
import com.kiouri.sliderbar.client.view.SliderBar;

import eu.riscoss.client.JsonDistribution;
import eu.riscoss.client.Log;
import eu.riscoss.client.Range;

public class IndicatorWidget implements IsWidget {
	
	static class Rectangle {
		double x, y, w, h;

		public Rectangle(double xi, double yi, double wi, double hi) {
			x = xi; y = yi; w = wi; h = hi;
		}

		public double getMaxX() {
			return x + w;
		}
		
		public String toString() {
			return "rect(" + x + "," + y + "," + w + "," + h + ")";
		}
	}
	
	abstract class AbstractInputField implements IsWidget {
		
		abstract String getValue();

		public abstract void setValue( JSONObject v );
		
	}
	
	class NumberInput extends AbstractInputField {
		
		HorizontalPanel panel = new HorizontalPanel();
		
		Range		range;
		SliderBar	sb = new AdvancedSliderBar();
		Label		label = new Label();
		
		NumberInput( JSONObject o ) {
			sb.setNotSelectedInFocus();
			
			sb.addBarValueChangedHandler( new BarValueChangedHandler() {
				public void onBarValueChanged(BarValueChangedEvent event) {
					label.setText( "" + sb.getValue() );
					for( Listener l : listeners ) {
						l.IndicatorValueChanged();
					}
				}
			});
			
			label.getElement().getStyle().setMarginLeft( 12, Unit.PX );
			
			panel.add( sb );
			panel.add( label );
			
			setValue( o );
		}
		
		@Override
		public void setValue( JSONObject o ) {
			
			double min = Double.parseDouble( o.get( "min" ).isString().stringValue() );
			double max = Double.parseDouble( o.get( "max" ).isString().stringValue() );
			
			int intervals = 100;
			
			String datatype = o.get( "datatype" ).isString().stringValue();
//			REAL, INTEGER, STRING, EVIDENCE, DISTRIBUTION, NaN
			if( "REAL".equals( datatype ) )
				intervals = (int)(100 * (max - min));
			else if( "INTEGER".equals( datatype ) )
				intervals = (int)max - (int)min;
//			else if( "STRING".equals( datatype ) )
//				intervals = 100;
//			else if( "EVIDENCE".equals( datatype ) )
//				intervals = 100;
//			else if( "DISTRIBUTION".equals( datatype ) )
//				intervals = 100;
//			else if( "NaN".equals( datatype ) )
//				intervals = 100;
			
			range = new Range( min, max, intervals );
			sb.setMaxValue( (int)range.getSliderMax() );
		}

		@Override
		public Widget asWidget() {
			return panel;
		}

		@Override
		String getValue() {
			return "" + range.getValue( sb.getValue() );
		}

	}
	
	class DistributionInput extends AbstractInputField {
		
		Canvas canvas = Canvas.createIfSupported();
		
		Rectangle[] bars;
		String[] colors;
		
		int mleft = 0, mtop = 0, mright = 0, mbottom = 0;
		
		JsonDistribution distribution;
		
		public DistributionInput(JSONObject o) {
			this( o, 100, 50, null );
		}

		public DistributionInput( JSONObject o, int w, int h, String[] colors ) {
			canvas.setSize( w + "px", h + "px" );
			canvas.getElement().setAttribute( "width", w + "px" );
			canvas.getElement().setAttribute( "height", h + "px" );
			setValue( o );
		}
		
		@Override
		public void setValue( JSONObject v ) {
			setValue( v, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight(), null );
		}
		
		public void setValue( JSONObject o, int w, int h, String[] colors ) {
			
			distribution = new JsonDistribution( o );
			
			if( colors == null ) {
				colors = new String[distribution.getValues().size()];
				for( int i = 0; i < distribution.getValues().size(); i++ ) {
					colors[i] =  mkColor( i, distribution.getValues().size() );
				}
			}
			this.colors = colors;
			
			this.bars = mkBars( distribution, mleft, mtop, w -(mleft + mright), h -(mtop + mbottom) );
			
			Context2d g = canvas.getContext2d();
			g.clearRect( 0, 0, w, h );
			paint( distribution, g, mleft, mtop, w -(mleft + mright), h -(mtop + mbottom) );
			
			canvas.addMouseDownHandler( new MouseDownHandler() {
				@Override
				public void onMouseDown(MouseDownEvent event) {
					mousePressed( event );
				}} );
			canvas.addMouseMoveHandler( new MouseMoveHandler() {
				@Override
				public void onMouseMove(MouseMoveEvent event) {
					if( event.getNativeButton() == NativeEvent.BUTTON_LEFT ) {
						mouseDragged( event );
					}
				}
			});
			canvas.addMouseUpHandler( new MouseUpHandler() {
				@Override
				public void onMouseUp(MouseUpEvent event) {
					if( event.getNativeButton() == NativeEvent.BUTTON_LEFT ) {
						mouseReleased( event );
					}
				}
			});
		}
		
		protected void mousePressed(MouseDownEvent event) {
			int bar = findBar( event.getX(), event.getY() );
			if( bar == -1 ) return;
			double val = findValue( bar, event.getY() );
			List<Double> v = distribution.getValues();
			v.set( bar, val );
			distribution.flatten( bar );
			distribution.setValues( v ); // not needed because distribution.getValues() returns a reference
			this.bars = mkBars( distribution, mleft, mtop, canvas.getCoordinateSpaceWidth() -(mleft + mright), canvas.getCoordinateSpaceHeight() -(mtop + mbottom) );
			Context2d g = canvas.getContext2d();
			g.clearRect( 0, 0, canvas.getCoordinateSpaceWidth(), canvas.getCoordinateSpaceHeight() );
			paint( distribution, g, mleft, mtop, canvas.getCoordinateSpaceWidth() -(mleft + mright), canvas.getCoordinateSpaceHeight() -(mtop + mbottom) );
			for( Listener l : listeners ) {
				l.IndicatorValueChanged();
			}
		}
		
		protected void mouseDragged(MouseMoveEvent event) {
			// TODO Auto-generated method stub
			
		}
		
		protected void mouseReleased(MouseUpEvent event) {
			// TODO Auto-generated method stub
			
		}
		
		private String mkColor( int degree, int max ) {
			int red = (int)(((double)degree / (double)max) * (double)255);
			int green = (int)(((double)(max - degree) / (double)max) * (double)255);
			return "rgb(" + red + "," + green + ",0)";
		}
		
		public int findBar( int x, int y ) {
			return findBar( distribution, x, y, mleft, mtop, 
					canvas.getOffsetWidth() -(mleft + mright), 
					canvas.getOffsetHeight() -(mtop + mbottom) );
		}
		
		private int findBar( JsonDistribution d, int mx, int my, int x, int y, int w, int h ) {
			
			for( int i = 0; i < bars.length; i++ ) {
				Rectangle r = bars[i];
				if( r.x < mx )
					if( mx < r.getMaxX() )
						return i;
			}
			
			return -1;
		}
		
		public double findValue( int bar, int y ) {
			double maxy = canvas.getOffsetHeight() - mtop - mbottom;
			double ry = y - mtop;
			return ((maxy - ry) / maxy);
		}
		
		private Rectangle[] mkBars( JsonDistribution d, int x, int y, int w, int h ) {
			Rectangle[] rects = new Rectangle[d.getValues().size()];
			double step = ((double)w) / d.getValues().size();
			for( int i = 0; i < d.getValues().size(); i++ ) {
				double val = d.getValues().get( i );
				double xi = x + (step * i);
				double yi = y + h - (val * h);
				double hi = (val * h);
				double wi = step -2;
				
				rects[i] = new Rectangle( xi, yi, wi, hi );
			}
			return rects;
		}

		private void paint( JsonDistribution d, Context2d g, int x, int y, int w, int h ) {
			
			for( int i = 0; i < bars.length; i++ ) {
				Rectangle r = bars[i];
				g.setFillStyle( colors[i] );
				g.fillRect( r.x, r.y, r.w, r.h );
			}
		}

		@Override
		public Widget asWidget() {
			return canvas;
		}

		@Override
		String getValue() {
			if( bars.length == 5 )
				Log.println( distribution.toString() );
			return distribution.toString();
		}
		
	}
	
	class EvidenceInput extends AbstractInputField {
		
		VerticalPanel panel = new VerticalPanel();
		
		Image img;
		
		String pos = "0", neg = "0";
		
		public EvidenceInput( JSONObject o ) {
//			if( o.get( "label" ) != null )
//				panel.add( new Label( o.get( "label" ).isString().stringValue()  ));
//			else
//				panel.add( new Label( o.get( "id" ).isString().stringValue()  ));
			img = new Image();
			img.setSize( "100px", "50px" );
			panel.add( img );
			setValue( "0", "0" );
		}
		
		@Override
		public Widget asWidget() {
			return panel;
		}
		
		public void setValue( String p, String m ) {
			this.pos = p; this.neg = m;
			img.setUrl( GWT.getHostPageBaseURL() + "gauge?type=e&p=" + p + "&m=" + m + "&w=100&h=50" );
		}

		@Override
		String getValue() {
			return pos + ";" + neg;
		}

		@Override
		public void setValue( JSONObject v ) {
			setValue( 
					v.get( "p" ).isString().stringValue(), 
					v.get( "m" ).isString().stringValue() );
		}
	}
	
	public interface Listener {
		void IndicatorValueChanged();
	}
	
	VerticalPanel		panel = new VerticalPanel();
	HorizontalPanel		h = new HorizontalPanel();
	
	AbstractInputField	field;
	
	List<Listener>		listeners = new ArrayList<Listener>();
	
	
	
	public IndicatorWidget( JSONObject o ) {
		
		String datatype = o.get( "datatype" ).isString().stringValue();
		
		if( "REAL".equals( datatype ) )
			field = new NumberInput( o );
		else if( "INTEGER".equals( datatype ) )
			field = new NumberInput( o );
		else if( "DISTRIBUTION".equals( datatype ) )
			field = new DistributionInput( o );
		else if( "EVIDENCE".equals( datatype ) )
			field = new EvidenceInput( o );
		else
			field = new NumberInput( o );
		
		
		panel.add( new Label( o.get( "label" ).isString().stringValue() ) );
		h.add( field.asWidget() );
		
		panel.add( h );
		
	}
	
	public Widget asWidget() {
		return panel;
	}

	public void addListener(Listener listener) {
		listeners.add( listener );
	}
	
	public JSONObject getJson() {
		JSONObject o = new JSONObject();
		o.put( "value", new JSONString( "" +  field.getValue() ) );
		return o;
	}

	public void setValue( JSONObject v ) {
		try {
//			Log.println( "Setting value " + v );
			this.field.setValue( v );
		}
		catch( Exception ex ) {
			Window.alert( ex.getMessage() );
		}
	}
}
