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

package eu.riscoss.client.report;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.JsonRiskResult;
import eu.riscoss.client.ui.GaugeImage;

public class RiskAnalysisReport implements IsWidget {
	
	SimplePanel panel = new SimplePanel();
	
	@Override
	public Widget asWidget() {
		return panel;
	}
	
	public void showResults( JSONArray response ) {
		
		if( panel.getWidget() != null ) {
			panel.getWidget().removeFromParent();
		}
		
		Grid grid = new Grid();
		grid.setCellPadding(0);
		grid.setCellSpacing(0);
		for( int i = 0; i < response.isArray().size(); i++ ) {
			JSONObject v = response.isArray().get( i ).isObject();
			JsonRiskResult result = new JsonRiskResult( v );
//			grid.insertRow( grid.getRowCount() );
			switch( result.getDataType() ) {
			case DISTRIBUTION: {
				grid.resize( grid.getRowCount() +1, 3 );
				
				VerticalPanel panel = new VerticalPanel();
				
				String label = v.get( "id" ).isString().stringValue();
				if( v.get( "name" ) != null ) {
					if( v.get( "name" ).isString() != null ) {
						label = v.get( "name" ).isString().stringValue();
					}
				}
				Label l = new Label( label );
				l.setStyleName("bold");
				panel.add( l );
				panel.setStyleName("headerTable");
				grid.setWidget( i, 0, panel );
				
				GaugeImage img = new GaugeImage();
				img.setDistribution( result.getDistributionString() );
				grid.setWidget( i, 1, img );
				
				grid.setWidget( i, 2, new Label( result.getDescription() ) );
			}
				break;
			case EVIDENCE: {
				grid.resize( grid.getRowCount() +1, 3 );
				VerticalPanel panel = new VerticalPanel();
				Label l = new Label(v.get( "id" ).isString().stringValue());
				l.setStyleName("bold");
				panel.add( l );
				panel.add( new HTML( 
						"Exposure: <font color='red'>" + 
								v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue() +
						"</font>") );
				panel.setStyleName("headerTable");
				panel.setHeight("100%");
				grid.setWidget( i, 0, panel );
				
				if( "evidence".equals( v.get( "datatype" ).isString().stringValue() ) ) {
					GaugeImage img = new GaugeImage();
					img.setEvidence( v.get( "p" ).isString().stringValue(), v.get( "m" ).isString().stringValue() );
					SimplePanel p = new SimplePanel();
					p.setWidget(img);
					p.setStyleName("contentResultsTable");
					grid.setWidget( i, 1, p );
				}
				Label d = new Label(v.get( "description" ).isString().stringValue());
				SimplePanel sp = new SimplePanel();
				sp.setWidget(d);
				sp.setStyleName("contentResultsTable");
				grid.setWidget( i, 2, sp);
			}
				break;
			case INTEGER:
			case NaN:
			case REAL:
			case STRING:
			default:
				break;
			}
		}
		
		panel.setWidget( grid );
	}
}
