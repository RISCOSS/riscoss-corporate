package eu.riscoss.client.ras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.TextAlign;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
import com.googlecode.gflot.client.options.Marking;
import com.googlecode.gflot.client.options.Markings;
import com.googlecode.gflot.client.options.PlotOptions;
import com.googlecode.gflot.client.options.Range;

import eu.riscoss.client.JsonRiskResult;
import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.ui.GaugeImage;

public abstract class ComparisonPanel {

	//UI elements
	VerticalPanel 		ppg;
	VerticalPanel		mView;
	
	//Logic elements
	List<String> riskSessions;
	
	public ComparisonPanel(List<String> comparison) {
		
		this.riskSessions = comparison;
		
		ppg = new VerticalPanel();
		ppg.setWidth("100%");
		mView = new VerticalPanel();
		
		Label title = new Label("Risk analysis session comparisons");
		title.setStyleName("title");
		ppg.add(title);
		ppg.add(mView);
		
		Button back = new Button("Back");
		back.setStyleName("deleteButton");
		back.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				back();
			}
		});
		
		mView.setStyleName("leftPanelLayer");
		mView.add(back);
		
		computeResults();
		
	}

	/**
	 * Back method to override when instantiating the class
	 */
	public abstract void back();
	
	public Widget getWidget() {
		return this.ppg;
	}
	
	private String[] getRandomColors(int n) {
		
        String colors[] = new String[n];
        Random rand = new Random(0);
        for (int i = 0; i < n; ++i) {
        	int r = rand.nextInt() % 256;
        	if (r < 0) r = r*(-1);
        	int g = rand.nextInt() % 256;
        	if (g < 0) g = g*(-1);
        	int b = rand.nextInt() % 256;
        	if (b < 0) b = b*(-1);
        	if (r > 15 && g > 15 && b > 15) colors[i] = ("#" + 
        			Integer.toHexString(r) + 
        			Integer.toHexString(g) + 
        			Integer.toHexString(b));
        	else --i;
        }
        return colors;
    }
	
	Map<String, JSONArray> entityResults;
	
	Map<String, Map<String, JSONObject> > risks;
	Map<String, Map<String, JSONObject> > goals;
	Map<String, Map<String, JSONObject> > distributions;
	
	protected void computeResults() {
		RiscossJsonClient.getSessionListResults(riskSessions, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				entityResults = new HashMap<>();
				for (int i = 0; i < response.isArray().size(); ++i) {
					JSONValue r = response.isArray().get(i).isObject().get( "res" );
					JSONValue obj = JSONParser.parseLenient(r.isString().stringValue());
					entityResults.put(response.isArray().get(i).isObject().get("name").isString().stringValue(), obj.isObject().get("results").isArray());
				}
				
				risks = new HashMap<>();
				goals = new HashMap<>();
				distributions = new HashMap<>();
				
				for (String ses : entityResults.keySet()) {
					JSONArray array = entityResults.get(ses);
					for (int i = 0; i < array.size(); ++i) {
						if (array.get(i).isObject().get("datatype").isString().stringValue().equals("evidence")) {
							if (array.get(i).isObject().get("type") == null) {
								if (!risks.containsKey(array.get(i).isObject().get("label").isString().stringValue())) {
									Map<String, JSONObject> m = new HashMap<>();
									m.put(ses, array.get(i).isObject());
									risks.put(array.get(i).isObject().get("label").isString().stringValue(), m);
								}
								else {
									risks.get(array.get(i).isObject().get("label").isString().stringValue()).put(ses, array.get(i).isObject());
								}
							}
							else if (array.get(i).isObject().get("type").isString().stringValue().equals("Risk")) {
								if (!risks.containsKey(array.get(i).isObject().get("label").isString().stringValue())) {
									Map<String, JSONObject> m = new HashMap<>();
									m.put(ses, array.get(i).isObject());
									risks.put(array.get(i).isObject().get("label").isString().stringValue(), m);
								}
								else {
									risks.get(array.get(i).isObject().get("label").isString().stringValue()).put(ses, array.get(i).isObject());
								}
							}
							else if (array.get(i).isObject().get("type").isString().stringValue().equals("Goal")){
								if (!goals.containsKey(array.get(i).isObject().get("label").isString().stringValue())) {
									Map<String, JSONObject> m = new HashMap<>();
									m.put(ses, array.get(i).isObject());
									goals.put(array.get(i).isObject().get("label").isString().stringValue(), m);
								}
								else {
									goals.get(array.get(i).isObject().get("label").isString().stringValue()).put(ses, array.get(i).isObject());
								}
							}
						}
						else if (array.get(i).isObject().get("datatype").isString().stringValue().equals("distribution")){
							if (!distributions.containsKey(array.get(i).isObject().get("id").isString().stringValue())) {
								Map<String, JSONObject> m = new HashMap<>();
								m.put(ses, array.get(i).isObject());
								distributions.put(array.get(i).isObject().get("id").isString().stringValue(), m);
							}
							else {
								distributions.get(array.get(i).isObject().get("id").isString().stringValue()).put(ses, array.get(i).isObject());
							}
						}
					}
				}		
				//printData();
		        colorsArray = getRandomColors(entityResults.size());
				if (risks.size() > 0 || goals.size() > 0) generateMainChart();
				generateComparisonGrid();
			}
		});
	}
	
	protected void printData() {
		for (String risk : risks.keySet()) {
			Log.println(risk);
			for (String ses : risks.get(risk).keySet()) Log.println(ses);
		}
		for (String goal : goals.keySet()) {
			Log.println(goal);
			for (String ses : goals.get(goal).keySet()) Log.println(ses);
		}
		
	}
	
	SimplePlot mainPlot;
	String[] colorsArray;
	VerticalPanel legend;
	
	protected void generateMainChart() {
		
		int nbSeries = entityResults.size();
		legend = new VerticalPanel();
		legend.setStyleName("legend");
		
		JsArrayString colors = JavaScriptObject.createArray().cast();
        for (String s : colorsArray) {
        	colors.push(s);
        }

        PlotModel model = new PlotModel();
        PlotOptions plotOptions = PlotOptions.create();
		
        GlobalSeriesOptions globalSeriesOptions = GlobalSeriesOptions.create();
        double width = 1 / (double) nbSeries - 0.01;
        globalSeriesOptions.setBarsSeriesOptions( BarSeriesOptions.create().setShow( true ).setBarWidth( width ).setLineWidth( 1 ) );
        plotOptions.addXAxisOptions( AxisOptions.create().setTicks( new AbstractAxisOptions.TickGenerator()
        {
            @Override
            public JsArray<Tick> generate( Axis axis )
            {
                JsArray<Tick> array = JsArray.createArray().cast();
                double tick = 0;
                for (String goal : goals.keySet()) {
                	array.push(Tick.of(tick, goal));
                	++tick;
                }
                for (String risk : risks.keySet()) {
                	array.push(Tick.of(tick, risk));
                	++tick;
                }
                return array;
            }
        } ) );
        
        plotOptions.addYAxisOptions( AxisOptions.create().setMaximum(1.0));
        plotOptions.addYAxisOptions( AxisOptions.create().setMinimum(0.0));
        
        plotOptions.setGlobalSeriesOptions( globalSeriesOptions );
        plotOptions.setLegendOptions( LegendOptions.create().setShow(false));
        // create a series
        List<SeriesHandler> series = new ArrayList<SeriesHandler>();
        int j = 0;
        for (String ras : entityResults.keySet()) {
        	series.add( model.addSeries( Series.of( ras ).setBarsSeriesOptions( BarSeriesOptions.create().setOrder( j ) ) ) );
        	generateLegend(j, ras);
        	++j;
        }

        Markings markings = Markings.create();

        // add data
        int i = 0;
        for ( String goal : goals.keySet() ) {
        	int k = 0;
            for ( String ras : entityResults.keySet() ) {
            	double val = 0.;
            	if (goals.get(goal).containsKey(ras)) {
            		val = goals.get(goal).get(ras).get( "e" ).isObject().get( "e" ).isNumber().doubleValue();
            	}
                series.get(k).add( DataPoint.of( i, val ) );
            	++k;
            }
            if ( i % 2 != 0 ) {
                markings.addMarking( Marking.create().setX( Range.of( i - 0.5, i + 0.5 ) ).setColor( "rgba(153, 153, 153, 0.3)" ) );
            }
            ++i;
        }
        for ( String risk : risks.keySet() ) {
        	int k = 0;
            for ( String ras : entityResults.keySet() ) {
            	double val = 0.;
            	if (risks.get(risk).containsKey(ras)) {
            		val = risks.get(risk).get(ras).get( "e" ).isObject().get( "e" ).isNumber().doubleValue();
            	}
                series.get(k).add( DataPoint.of( i, val ) );
            	++k;
            }
            if ( i % 2 != 0 ) {
               markings.addMarking( Marking.create().setX( Range.of( i-0.5, i+0.5) ).setColor( "rgba(153, 153, 153, 0.3)" ) );
            }
            ++i;
        }
        plotOptions.setDefaultColorTheme(colors);
        plotOptions.setGridOptions( GridOptions.create().setMarkings( markings ) );

        // create the plot
        mainPlot = new SimplePlot( model, plotOptions );
        mainPlot.setWidth(1250);
        mainPlot.getElement().getStyle().setMarginTop(12, Unit.PX);
        //mView.add(legend);
        mView.add(mainPlot);
        mView.setWidth("100%");
        
	}
	
	protected void generateLegend(int i, String ras) {
		String c = "col" + i;
		HTMLPanel html = new HTMLPanel("<span id='" + c + "'></span>");
		SimplePanel p = new SimplePanel();
		p.setSize("20px", "20px");
		p.getElement().getStyle().setBackgroundColor(colorsArray[i]);
		p.setStyleName("colorBox");
		HorizontalPanel hp = new HorizontalPanel();
		hp.setStyleName("margin-bottom");
		hp.add(p);
		String htmlString = "<b>" + ras + "</b>";
		HTMLPanel htm = new HTMLPanel( htmlString );
		hp.add(htm);
		html.add(hp, c);
		legend.add(html);
	}
	
	protected void generateComparisonGrid() {
		Grid g = new Grid( risks.size() + goals.size() + distributions.size() + 1, entityResults.size() + 1);
		g.setWidth("97%");
		g.getElement().getStyle().setMarginTop(26, Unit.PX);
		g.setCellSpacing(0);
		g.setBorderWidth(1);
		g.getElement().getStyle().setBorderColor("#c8c8c8");
		g.getCellFormatter().getElement(0, 0).getStyle().setBackgroundColor("#e8e8e8");
		g.setStyleName("table");
		
		int i = 1;
		double width = 100/entityResults.size() - entityResults.size();
		for (String ras : entityResults.keySet()) {
			Label l = new Label(ras);
			l.setStyleName("bold");
			l.getElement().getStyle().setColor(colorsArray[i-1]);
			g.setWidget(0, i, l);
			g.getCellFormatter().setHorizontalAlignment(0, i, HasHorizontalAlignment.ALIGN_CENTER);
			g.getCellFormatter().getElement(0, i).getStyle().setPaddingTop(4, Unit.PX);
			g.getCellFormatter().getElement(0, i).getStyle().setPaddingBottom(4, Unit.PX);
			g.getCellFormatter().getElement(0, i).getStyle().setBackgroundColor("#e8e8e8");
			String w = width + "%";
		    g.getColumnFormatter().setWidth(i, w);
			++i;
		}
		
		i = 1;
		for (String goal : goals.keySet()) {
			Label l = new Label(goal);
			l.setStyleName("bold");
			g.setWidget(i, 0, l);
			g.getCellFormatter().getElement(i, 0).getStyle().setBackgroundColor("#e8e8e8");
			g.getCellFormatter().getElement(i, 0).getStyle().setPaddingLeft(8, Unit.PX);
			int k = 1;
			for (String ras : entityResults.keySet()) {
				if (goals.get(goal).containsKey(ras)) {
					g.setWidget(i, k, generateIndicatorWidget(goals.get(goal).get(ras)));
					g.getCellFormatter().setHorizontalAlignment(i, k, HasHorizontalAlignment.ALIGN_CENTER);
				}
				k++;
			}
			i++;
		}
		for (String risk : risks.keySet()) {
			Label l = new Label(risk);
			l.setStyleName("bold");
			g.setWidget(i, 0, l);
			g.getCellFormatter().getElement(i, 0).getStyle().setBackgroundColor("#e8e8e8");
			g.getCellFormatter().getElement(i, 0).getStyle().setPaddingLeft(8, Unit.PX);
			int k = 1;
			for (String ras : entityResults.keySet()) {
				if (risks.get(risk).containsKey(ras)) {
					g.setWidget(i, k, generateIndicatorWidget(risks.get(risk).get(ras)));
					g.getCellFormatter().setHorizontalAlignment(i, k, HasHorizontalAlignment.ALIGN_CENTER);
				}
				k++;
			}
			i++;
		}
		for (String distribution : distributions.keySet()) {
			Label l = new Label(distribution);
			l.setStyleName("bold");
			g.setWidget(i, 0, l);
			g.getCellFormatter().getElement(i, 0).getStyle().setBackgroundColor("#e8e8e8");
			g.getCellFormatter().getElement(i, 0).getStyle().setPaddingLeft(8, Unit.PX);
			int k = 1;
			for (String ras : entityResults.keySet()) {
				if (distributions.get(distribution).containsKey(ras)) {
					g.setWidget(i, k, generateDistributionWidget(distributions.get(distribution).get(ras)));
					g.getCellFormatter().setHorizontalAlignment(i, k, HasHorizontalAlignment.ALIGN_CENTER);
				}
				k++;
			}
			i++;
		}

		mView.add(g);
	}
	
	protected Widget generateIndicatorWidget(JSONObject v) {
		VerticalPanel panel = new VerticalPanel();
		GaugeImage img = new GaugeImage();
		img.setEvidence( v.get( "p" ).isString().stringValue(), v.get( "m" ).isString().stringValue() );
		SimplePanel pp = new SimplePanel();
		pp.setWidget(img);
		pp.setStyleName("contentResultsTable");
		panel.add(pp);
		panel.add( new HTML( 
				"Exposure: <font color='red'>" + 
						v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue() +
				"</font>") );
		panel.setStyleName("headerTable");
		panel.setHeight("100%");
		return panel;
	}
	
	protected Widget generateDistributionWidget(JSONObject v) {
		VerticalPanel panel = new VerticalPanel();
		JsonRiskResult result = new JsonRiskResult( v );
		
		GaugeImage img = new GaugeImage();
		img.setDistribution( result.getDistributionString() );
		panel.add(img);
		
		return panel;
	}
	
}
