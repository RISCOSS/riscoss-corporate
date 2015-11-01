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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
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
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.googlecode.gflot.client.Axis;
import com.googlecode.gflot.client.DataPoint;
import com.googlecode.gflot.client.PlotModel;
import com.googlecode.gflot.client.Series;
import com.googlecode.gflot.client.SeriesHandler;
import com.googlecode.gflot.client.SimplePlot;
import com.googlecode.gflot.client.Tick;
import com.googlecode.gflot.client.options.AbstractAxisOptions;
import com.googlecode.gflot.client.options.AxisOptions;
import com.googlecode.gflot.client.options.BarSeriesOptions;
import com.googlecode.gflot.client.options.GlobalSeriesOptions;
import com.googlecode.gflot.client.options.GridOptions;
import com.googlecode.gflot.client.options.LegendOptions;
import com.googlecode.gflot.client.options.PlotOptions;

import eu.riscoss.client.JsonRiskResult;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecRASInfo;
import eu.riscoss.client.riskanalysis.JsonRiskAnalysis;
import eu.riscoss.client.ui.ClickWrapper;
import eu.riscoss.client.ui.GaugeImage;
import eu.riscoss.client.ui.TreeWidget;
import eu.riscoss.shared.JArgument;
import eu.riscoss.shared.JArgumentation;
import eu.riscoss.shared.JRASInfo;

public class RiskAnalysisReport implements IsWidget {
	
	public interface Codec extends JsonEncoderDecoder<JArgumentation>{}
	
	VerticalPanel panel = new VerticalPanel();
	
	JSONArray			response;
	JArgumentation		argumentation = new JArgumentation();
	JsonRiskAnalysis 	summary;
	
	@Override
	public Widget asWidget() {
		return panel;
	}
	
	HorizontalPanel		mainChartPanel = new HorizontalPanel();
	VerticalPanel 		descriptions   = new VerticalPanel();
	
	public void showResults( JSONArray response, JSONValue jsonArgumentation ) {
		Codec codec = GWT.create( Codec.class );
		
		if( jsonArgumentation != null ) {
			argumentation = codec.decode( jsonArgumentation );
		}
		
		Grid grid = new Grid();
		grid.setCellPadding(0);
		grid.setCellSpacing(0);
		this.response = response;
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
					b.setStyleName("button");
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
					b.setStyleName("button");
					
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
		
		panel.add( grid );
	}
	
	ArrayList<SimplePanel> 	panels;
	ArrayList<String> 		riskSessions;
	ArrayList<String>		dates;
	int 					count;
	int						k;
	ArrayList< ArrayList<Double> > l;
	int 					counting;
	ArrayList<Info> 		dataList;
	
	public void initializeParameters() {
		panels = new ArrayList<>();
		riskSessions = new ArrayList<>();
		dates = new ArrayList<>();
		k = 0;
		l = new ArrayList<>();
		counting = 0;
		dataList = new ArrayList<>();
	}
	
	public void showResults( JsonRiskAnalysis summary, JSONArray response, JSONValue jsonArgumentation ) {
		initializeParameters();
		/*if( panel.getWidget() != null ) {
			panel.getWidget().removeFromParent();
		}*/
		this.summary = summary;
		panel.clear();
		mainChartPanel.clear();
		descriptions.clear();
		mainChartPanel.setStyleName("margin-top");
		this.response = response;
		
		Codec codec = GWT.create( Codec.class );
		
		if( jsonArgumentation != null ) {
			argumentation = codec.decode( jsonArgumentation );
		}
		
		generateMainChart();
		getSessionsInformation();
		
		Grid grid = new Grid();
		grid.setStyleName("margin-grid");
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
					b.setStyleName("button");
					
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
				
				JArgument arg = argumentation.arguments.get( result.getChunkId() );
				if( arg != null ) {
					Button b = new Button( "Why?" );
					b.setStyleName("button");
					
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
					
					panel.add( b );
				}
				
				grid.setWidget( i, 0, panel );
				
				if( "evidence".equals( v.get( "datatype" ).isString().stringValue() ) ) {
					GaugeImage img = new GaugeImage();
					img.setEvidence( v.get( "p" ).isString().stringValue(), v.get( "m" ).isString().stringValue() );
					SimplePanel p = new SimplePanel();
					p.setWidget(img);
					p.setStyleName("contentResultsTable");
					grid.setWidget( i, 1, p );
				}
				SimplePanel s = new SimplePanel();
				s.setStyleName("chart");
				panels.add(s);
				grid.setWidget( i, 2, s );
				
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
	
	String nextDate;
	
	protected void getSessionsInformation() {
		RiscossJsonClient.listRiskAnalysisSessions(summary.getTarget(), summary.getRC(), new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				response = response.isObject().get( "list" );
				count = response.isArray().size();
				CodecRASInfo codec = GWT.create( CodecRASInfo.class );
				if( response.isArray() != null ) {
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JRASInfo info = codec.decode( response.isArray().get( i ) );
						riskSessions.add(info.getId());
						++k;
						RiscossJsonClient.getSessionSummary(info.getId(), new JsonCallback() {
							int i = k;
							@Override
							public void onFailure(Method method,
									Throwable exception) {
								Window.alert(exception.getMessage());
							}
							@Override
							public void onSuccess(Method method,
									JSONValue response) {
								JsonRiskAnalysis r = new JsonRiskAnalysis(response);
								nextDate = r.getDate();
								RiscossJsonClient.getSessionResults( r.getID(), new JsonCallback() {
									String d = nextDate;
									@Override
									public void onFailure(Method method, Throwable exception) {
										Window.alert(exception.getMessage());
									}
									@Override
									public void onSuccess(Method method, JSONValue response) {
										JSONArray results = response.isObject().get( "results" ).isArray();
										ArrayList<Double> values = new ArrayList<>();
										for (int s = 0; s < results.isArray().size(); ++s) {
											JSONObject v = results.isArray().get( s ).isObject();
											JsonRiskResult result = new JsonRiskResult( v );
//											
											switch( result.getDataType() ) {
												case EVIDENCE: {
													values.add(v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue());
												}
												default: break;
											}
										}
										Date date = getDate(d);
										Info inf = new Info(date, values);
										dataList.add(inf);
										++counting;
										if (counting == count) {
											sortByDate();
										}
									}
								});
							}
						});
					}
				}
			}
		});
	}
	
	private class Info {
		
		Date date;
		ArrayList<Double> values;
		
		public Info(Date date, ArrayList<Double> values) {
			this.date = date;
			this.values = values;
		}
		
		public Date getDate() {
			return date;
		}
		
		public ArrayList<Double> getValues() {
			return values;
		}
		
		public String getStringDate() {
			String s = date.getDate() + "-" + date.getMonth() + "-" + date.getYear() + " " + date.getHours() + "."  + date.getMinutes() + "." + date.getSeconds();
			return s;
		}
		
	}
	
	private void sortByDate() {
		
		Collections.sort(dataList, new Comparator<Info>() {
			@Override
			public int compare(Info o1, Info o2) {
				return o1.getDate().compareTo(o2.getDate());
			}
		});
		
		if (dataList.size() > 1) createComparisonCharts();
	}
	
	protected void createComparisonCharts() {
		for (int i = 0; i < panels.size(); ++i) {
			ArrayList<Double> compare = new ArrayList<>();
			for (int j = 0; j < dataList.size(); ++j) {
				compare.add(dataList.get(j).getValues().get(i));
			}
			//Window.alert(String.valueOf(i));
			PlotModel model = new PlotModel();
	        PlotOptions plotOptions = PlotOptions.create();
	        plotOptions.setLegendOptions( LegendOptions.create().setShow(false));
	        plotOptions.setGridOptions( GridOptions.create().setMargin( 5 ) );
	        //plotOptions.addXAxisOptions( AxisOptions.create().setFont( FontOptions.create().setColor("black").setWeight( "bold" ).setStyle( "italic" ) ) );
	       // plotOptions.addYAxisOptions( AxisOptions.create().setFont( FontOptions.create().setColor( "black" ).setWeight( "bold" ).setStyle( "italic" ) ) );
	        plotOptions.addYAxisOptions( AxisOptions.create().setMaximum(1).setMinimum(0));
	        plotOptions.addXAxisOptions( AxisOptions.create().setTicks(new AbstractAxisOptions.TickGenerator() {
				@Override
				public JsArray<Tick> generate(Axis axis) {
					JsArray<Tick> array = JsArray.createArray().cast();
	                for ( int i = 0; i < dataList.size(); i++ )
	                {
	                    array.push( Tick.of( i, dataList.get(i).getStringDate() ) );
	                }
	                return array;
				}
	        }));
	        // create the plot
	        SimplePlot plot = new SimplePlot( model, plotOptions );
	        plot.getModel().addSeries( Series.of( "Exposure" ) );
	        for ( int j = 0; j < compare.size(); j++ ) {
	            for ( SeriesHandler series : plot.getModel().getHandlers() ) {
	                series.add( DataPoint.of( j, compare.get(j) ) );
	            }
	        }
	        JsArrayString colors = JavaScriptObject.createArray().cast();
	        colors.push(colorsArray[i]);
	        plotOptions.setDefaultColorTheme(colors);
	        plot.setHeight("200px");
	        panels.get(i).setWidget(plot);
		}
	}
	
	private Date getDate (String date) {
		String info[] = date.split(" ");
		String day[] = info[0].split("-");
		String hour[] = info[1].split("\\.");
		Date d = new Date();
		d.setDate(Integer.parseInt(day[0]));
		d.setMonth(Integer.parseInt(day[1]));
		d.setYear(Integer.parseInt(day[2]));
		d.setHours(Integer.parseInt(hour[0]));
		d.setMinutes(Integer.parseInt(hour[1]));
		d.setSeconds(Integer.parseInt(hour[2]));
		return d;
		
	}
	
	private String[] getRandomColors(int n) {
		
        String colors[] = new String[n];
        Random rand = new Random();
        for (int i = 0; i < n; ++i) {
        	int r = rand.nextInt() % 256;
        	if (r < 0) r = r*(-1);
        	int g = rand.nextInt() % 256;
        	if (g < 0) g = g*(-1);
        	int b = rand.nextInt() % 256;
        	if (b < 0) b = b*(-1);
        	if (r > 15 && g > 15 && b > 15) colors[i] = ("#" + Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b));
        	else --i;
        }
        return colors;
    }
	
	protected void generateMainChart() {
		drawCh();
	}
	
	String colorsArray[];
	
	protected void drawCh() {
		int nbSeries = response.isArray().size();
		//Window.alert(String.valueOf(nbSeries));

        PlotModel model = new PlotModel();
        PlotOptions plotOptions = PlotOptions.create();

        GlobalSeriesOptions globalSeriesOptions = GlobalSeriesOptions.create();
        globalSeriesOptions.setBarsSeriesOptions( BarSeriesOptions.create().setShow( true ).setBarWidth( 0.2 ).setLineWidth( 1 ).setFill(0.8) );
        plotOptions.addXAxisOptions( AxisOptions.create().setTicks( new AbstractAxisOptions.TickGenerator()
        {

			@Override
			public JsArray<Tick> generate(Axis axis) {
				JsArray<Tick> array = JsArray.createArray().cast();
                for ( int i = 0; i < 1; i++ )
                {
                    array.push( Tick.of( i, "Exposure" ) );
                }
                return array;
			}
        } ) );
        plotOptions.addYAxisOptions( AxisOptions.create().setMaximum(1.0));
        plotOptions.addYAxisOptions( AxisOptions.create().setMinimum(0.0));

        plotOptions.setGlobalSeriesOptions( globalSeriesOptions );

        plotOptions.setLegendOptions( LegendOptions.create().setMargin( -150, 0 ) );
        
        JsArrayString colors = JavaScriptObject.createArray().cast();
        colorsArray = getRandomColors(nbSeries);
        for (String s : colorsArray) {
        	colors.push(s);
        }
        
        ArrayList<Double> values = new ArrayList<>();
        // create a series
        List<SeriesHandler> series = new ArrayList<SeriesHandler>();
        for ( int i = 0; i < nbSeries; i++ ) {
        	JSONObject v = response.isArray().get( i ).isObject();
			JsonRiskResult result = new JsonRiskResult( v );
			String id = "";
			switch( result.getDataType() ) {
			case EVIDENCE: {
				id = v.get( "id" ).isString().stringValue();
				Double value = v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue();
				series.add( model.addSeries( Series.of( id ).setBarsSeriesOptions( BarSeriesOptions.create().setOrder( i ) ) ) );
				values.add(value);
				String s = "arg" + i;
				String c = "col" + i;
				HTMLPanel html = new HTMLPanel("<span id='" + c + "'></span>");
				JArgument arg = argumentation.arguments.get( result.getChunkId() );
				SimplePanel p = new SimplePanel();
				p.setSize("20px", "20px");
				p.getElement().getStyle().setBackgroundColor(colorsArray[i]);
				p.setStyleName("colorBox");
				
				HorizontalPanel hp = new HorizontalPanel();
				hp.setStyleName("margin-bottom");
				hp.add(p);
				
				String htmlString = "<b>" + v.get( "id" ).isString().stringValue() + "</b>";
				if( v.get( "description" ) != null ) {
					htmlString += ": " + v.get( "description" ).isString().stringValue();
				}
				HTMLPanel htm = new HTMLPanel( htmlString );
				hp.add(htm);
				
				html.add(hp, c);
				
				html.setWidth("100%");
				html.setStyleName("descriptionPanel");
				descriptions.add(html);
				break;
			}
			default:
				break;
			}
			
		}

        // add data
        for ( int i = 0; i < series.size(); i++ ) {
        	series.get(i).add(DataPoint.of( 0, values.get(i)));
        }

        plotOptions.setLegendOptions( LegendOptions.create().setShow(false));
        plotOptions.setDefaultColorTheme(colors);
        // create the plot
        SimplePlot plot2 = new SimplePlot( model, plotOptions );
        //HEIGHT value in % has problems, needs to be fixed
        //plot2.setHeight("100%");
        int size = nbSeries*40;
        if (size < 350) size = 350;
        //plot2.setHeight(size + "px");
	    mainChartPanel.add(plot2);
	    mainChartPanel.add(descriptions);
	}

	
}
