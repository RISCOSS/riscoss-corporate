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

import org.fusesource.restygwt.client.JsonEncoderDecoder;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gwt.charts.client.ChartLoader;
import com.googlecode.gwt.charts.client.ChartPackage;
import com.googlecode.gwt.charts.client.ColumnType;
import com.googlecode.gwt.charts.client.DataTable;
import com.googlecode.gwt.charts.client.corechart.ColumnChart;

import eu.riscoss.client.JsonRiskResult;
import eu.riscoss.client.Log;
import eu.riscoss.client.ui.ClickWrapper;
import eu.riscoss.client.ui.GaugeImage;
import eu.riscoss.client.ui.TreeWidget;
import eu.riscoss.shared.JArgumentation;
import eu.riscoss.shared.JArgument;

public class RiskAnalysisReport implements IsWidget {
	
	public interface Codec extends JsonEncoderDecoder<JArgumentation>{}
	
	VerticalPanel panel = new VerticalPanel();
	
	JSONArray		response;
	JArgumentation	argumentation = new JArgumentation();
	
	@Override
	public Widget asWidget() {
		return panel;
	}
	
	HorizontalPanel		mainChartPanel = new HorizontalPanel();
	VerticalPanel 		descriptions   = new VerticalPanel();
	
	public void showResults( JSONArray response, JSONValue jsonArgumentation ) {
		
		/*if( panel.getWidget() != null ) {
			panel.getWidget().removeFromParent();
		}*/
		panel.clear();
		mainChartPanel.clear();
		descriptions.clear();
		this.response = response;
		
		Codec codec = GWT.create( Codec.class );
		
		if( jsonArgumentation != null ) {
			argumentation = codec.decode( jsonArgumentation );
		}
		
		//generateMainChart();
		
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
				
				HorizontalPanel hp = new HorizontalPanel();
				
				hp.add( new Label( result.getDescription() ) );
				
				JArgument arg = argumentation.arguments.get( result.getChunkId() );
				if( arg != null ) {
					Button b = new Button( "Why?" );
					
					b.addClickHandler( new ClickWrapper<JArgument>( arg ) {
						@Override
						public void onClick( ClickEvent event ) {
							DialogBox d = new DialogBox( true );
							JArgument arg = getValue();
							TreeWidget w = load( arg );
							d.add( w );
							d.center();
						}
						
						private TreeWidget load( JArgument arg ) {
							TreeWidget w = new TreeWidget( new Label( arg.summary ) );
							for( JArgument subArg : arg.subArgs ) {
								w.addChild( load( subArg ) );
							}
							return w;
						}} );
					
					hp.add( b );
				}
				
				grid.setWidget( i, 2, hp );
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
				
				HorizontalPanel hp = new HorizontalPanel();
				
				hp.add( sp );
				
				JArgument arg = argumentation.arguments.get( result.getChunkId() );
				if( arg != null ) {
					Button b = new Button( "Why?" );
					
					b.addClickHandler( new ClickWrapper<JArgument>( arg ) {
						@Override
						public void onClick( ClickEvent event ) {
							DialogBox d = new DialogBox( true );
							d.setText( "Argumentation" );
							JArgument arg = getValue();
							TreeWidget w = load( arg );
							d.add( w );
							d.center();
						}
						
						private TreeWidget load( JArgument arg ) {
							TreeWidget w = new TreeWidget( new Label( arg.summary ) );
							for( JArgument subArg : arg.subArgs ) {
								w.addChild( load( subArg ) );
							}
							return w;
						}} );
					
					hp.add( b );
				}
				
				grid.setWidget( i, 2, hp );
//				grid.setWidget( i, 2, sp);
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
		panel.add( mainChartPanel );
		panel.add( grid );
	}
	
	ColumnChart 		chart;
	
	private void generateMainChart() {
		ChartLoader chartLoader = new ChartLoader(ChartPackage.CORECHART);
		chartLoader.loadApi(new Runnable() {

			@Override
			public void run() {
				// Create and attach the chart
				chart = new ColumnChart();
				mainChartPanel.add(chart);
				mainChartPanel.add(descriptions);
				draw();
			}
		});
	}
	
	protected void draw() {
		DataTable colOldData = DataTable.create();
		colOldData.addColumn(ColumnType.STRING, "Risk");
		colOldData.addColumn(ColumnType.NUMBER, "Exposure");
		
		for (int i = 0; i < response.isArray().size(); ++i) {
			JSONObject v = response.isArray().get( i ).isObject();
			JsonRiskResult result = new JsonRiskResult( v );
			
			switch( result.getDataType() ) {
				case EVIDENCE: {
					String id = v.get( "id" ).isString().stringValue();
					Double value = v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue();
					colOldData.addRow(id, value);
					break;
				}
				default:
					break;
			}
			descriptions.add(new Label(v.get( "id" ).isString().stringValue() + ":" 
									+ v.get( "description" ).isString().stringValue()));
		}		
		colOldData.setTableProperty("min", 0.0);
		colOldData.setTableProperty("max", 1.0);
		chart.draw(colOldData);
		
	}
}
