package eu.riscoss.client.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayString;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
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

import eu.riscoss.client.JsonEntitySummary;
import eu.riscoss.client.JsonRiskResult;
import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecRASInfo;
import eu.riscoss.client.report.RiskAnalysisReport.Codec;
import eu.riscoss.client.riskanalysis.JsonRiskAnalysis;
import eu.riscoss.client.ui.ClickWrapper;
import eu.riscoss.client.ui.GaugeImage;
import eu.riscoss.client.ui.TreeWidget;
import eu.riscoss.shared.JArgument;
import eu.riscoss.shared.JArgumentation;
import eu.riscoss.shared.JRASInfo;

public class RiskAnalysisResults implements IsWidget {

	VerticalPanel		page = new VerticalPanel();
	HorizontalPanel		mainView = new HorizontalPanel();
	VerticalPanel		leftPanel = new VerticalPanel();
	VerticalPanel		rightPanel = new VerticalPanel();
	
	//EntityTree
	TreeWidget 			tree = new TreeWidget();
	String				selectedEntity;
	
	//Session data
	JsonRiskAnalysis summary;
	JSONValue jsonArgumentation;
	HashMap<String, JSONArray> results;
	JSONObject response;
	
	//Chart panels
	HorizontalPanel		mainChartPanel = new HorizontalPanel();
	VerticalPanel 		descriptions   = new VerticalPanel();
	
	//Argumentation
	JArgumentation		argumentation = new JArgumentation();
	
	//EVIDENCE
	Boolean 			evidence = true;
	
	@Override
	public Widget asWidget() {
		return page;
	}
	
	public RiskAnalysisResults() {
		mainView.setStyleName("mainViewLayer");
		mainView.setWidth("100%");
		leftPanel.setWidth("200px");
		rightPanel.setStyleName("rightPanelLayer");
		leftPanel.add(tree);
		
		mainView.add(leftPanel);
		mainView.add(rightPanel);
		page.add(mainView);
	}
	
	public Boolean getEvidence() {
		return evidence;
	}
	
	Label risksLabel;
	Label goalsLabel;
	
	public void showResults(JsonRiskAnalysis summary, JSONObject response) {
		risksLabel = new Label("Risks");
		risksLabel.setStyleName("smallTitle");
		goalsLabel = new Label("Goals");
		goalsLabel.setStyleName("smallTitle");
		
		this.response = response;
		this.summary = summary;
		this.jsonArgumentation = response.get("argumentation");
		Codec codec = GWT.create( Codec.class );
		
		if( jsonArgumentation != null ) {
			argumentation = codec.decode( jsonArgumentation );
		}
		results = new HashMap<>();
		
		tree.clear();
		if (response.get("hresults") != null ) generateTree(tree, response.get("hresults"));
		else {
			leftPanel.setWidth("0px");
			results.put(summary.getTarget(), response.get("results").isArray());
		}
		
		setSelectedEntity(summary.getTarget());
	}
	
	protected void setSelectedEntity(String entity) {
		rightPanel.clear();
		mainChartPanel.clear();
		descriptions.clear();
		
		selectedEntity = entity;
		
		evidence = true;
		//Check if evidence or distribution
		for (int i = 0; i < response.get("results").isArray().size(); ++i) {
			JSONObject v = response.get("results").isArray().get(i).isObject();
			JsonRiskResult result = new JsonRiskResult( v );
			if (result.getDatatype().equals("distribution")) evidence = false;
		}
		
		//Generate main chart (risks & goals)
		if (evidence) {
			Label title = new Label(selectedEntity);
			title.setStyleName("subtitle");
			rightPanel.add(title);
			generateMainChart(results.get(entity));
			rightPanel.add(mainChartPanel);
			getSessionsInformation();
			if (goals.size() > 0) {
				rightPanel.add(goalsLabel);
				rightPanel.add(gridGoals);
			}
			if (risks.size() > 0) {
				rightPanel.add(risksLabel);
				rightPanel.add(gridRisks);
			}
			if (risks.size() == 0 && goals.size() == 0) {
				rightPanel.remove(mainChartPanel);
				Label warning = new Label("There are no risk or goals related with entity " + selectedEntity + " for this risk analysis session.");
				rightPanel.add(warning);
			}
		}
		//Distribution (BN Analysis)
		else {
			generateBN(response.get("results").isArray());
		}
		
	}
	
	protected void generateBN(JSONArray entity) {
		rightPanel.clear();
		gridRisks = new Grid();
		gridRisks.setStyleName("margin-grid");
		gridRisks.setCellPadding(0);
		gridRisks.setCellSpacing(0);
		for (int i = 0; i < entity.size(); ++i) {
			JSONObject v = entity.get(i).isObject();
			JsonRiskResult result = new JsonRiskResult( v );
			gridRisks.resize( gridRisks.getRowCount() +1, 3 );
			
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
			gridRisks.setWidget( i, 0, panel );
			
			GaugeImage img = new GaugeImage();
			img.setDistribution( result.getDistributionString() );
			gridRisks.setWidget( i, 1, img );
			
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
			gridRisks.setWidget( i, 2, hp );
		}
		leftPanel.clear();
		rightPanel.clear();
		leftPanel.add(gridRisks);
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
        	if (r > 15 && g > 15 && b > 15) colors[i] = ("#" + Integer.toHexString(r) + Integer.toHexString(g) + Integer.toHexString(b));
        	else --i;
        }
        return colors;
    }
	
	String colorsArray[];
	SimplePlot plot2;
	List<JSONObject> risks;
	List<JSONObject> goals;
	
	Grid gridRisks;
	Grid gridGoals;
	
	protected void generateMainChart(JSONArray entityResults) {
		Log.println("Generating chart");
		int nbSeries = entityResults.size();
		
		risks = new ArrayList<>();
		goals = new ArrayList<>();
		
		for (int i = 0; i < nbSeries; ++i) {
			if (entityResults.get(i).isObject().get("datatype").isString().stringValue().equals("evidence")) {
				if (entityResults.get(i).isObject().get("type") == null) {
					risks.add(entityResults.get(i).isObject());
				}
				else if (entityResults.get(i).isObject().get("type").isString().stringValue().equals("Risk")) {
					risks.add(entityResults.get(i).isObject());
				}
				else if (entityResults.get(i).isObject().get("type").isString().stringValue().equals("Goal")){
					goals.add(entityResults.get(i).isObject());
				}
			}
		}
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
        
        Log.println("Generating colors");
        JsArrayString colors = JavaScriptObject.createArray().cast();
        colorsArray = getRandomColors(nbSeries);
        for (String s : colorsArray) {
        	colors.push(s);
        }
        
        ArrayList<Double> values = new ArrayList<>();
        // create a series
        List<SeriesHandler> series = new ArrayList<SeriesHandler>();
        Log.println("Iterating results");
       
        gridGoals = new Grid();
		gridGoals.setStyleName("margin-grid");
		gridGoals.setCellPadding(0);
		gridGoals.setCellSpacing(0);
		
		gridRisks = new Grid();
		gridRisks.setStyleName("margin-grid");
		gridRisks.setCellPadding(0);
		gridRisks.setCellSpacing(0);
		panels = new ArrayList<>();
        //RISKS & GOALS
        for (int i = 0; i < risks.size() + goals.size(); ++i) {
        	
        	JSONObject v;
        	if (i < goals.size()) 
        		v = goals.get(i);
        	else
        		v = risks.get(i - goals.size());
        	
        	JsonRiskResult result = new JsonRiskResult( v );
        	
			String id = "";
			id = v.get( "label" ).isString().stringValue();
			Double value = v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue();
			series.add( model.addSeries( Series.of( id ).setBarsSeriesOptions( BarSeriesOptions.create().setOrder( i ) ) ) );
			values.add(value);
			String s = "arg" + i;
			String c = "col" + i;
			HTMLPanel html = new HTMLPanel("<span id='" + c + "'></span>");
			SimplePanel p = new SimplePanel();
			p.setSize("20px", "20px");
			p.getElement().getStyle().setBackgroundColor(colorsArray[i]);
			p.setStyleName("colorBox");
			
			HorizontalPanel hp = new HorizontalPanel();
			hp.setStyleName("margin-bottom");
			hp.add(p);
			
			String htmlString = "<b>" + v.get( "label" ).isString().stringValue() + "</b>";
			if( v.get( "description" ) != null ) {
				htmlString += ": " + v.get( "description" ).isString().stringValue();
			}
			HTMLPanel htm = new HTMLPanel( htmlString );
			hp.add(htm);
			
			html.add(hp, c);
			
			html.setWidth("100%");
			html.setStyleName("descriptionPanel");
			descriptions.add(html);
			
			//Comparisons view
			
			VerticalPanel panel = new VerticalPanel();
			Label l = new Label(v.get( "label" ).isString().stringValue());
			l.setStyleName("bold");
			panel.add( l );
			VerticalPanel space = new VerticalPanel();
			space.setHeight("10px");
			panel.add(space);
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
			
			SimplePanel ss = new SimplePanel();
			ss.setStyleName("chart");
			panels.add(ss);
			if (i < goals.size()) {
				gridGoals.resize( gridGoals.getRowCount() +1, 2 );
				gridGoals.setWidget( i, 0, panel );
				gridGoals.setWidget( i, 1, ss );
			}
			else {
				gridRisks.resize( gridRisks.getRowCount() +1, 2 );
				gridRisks.setWidget( i-goals.size(), 0, panel );
				gridRisks.setWidget( i-goals.size(), 1, ss );
			}
			
        }
        Log.println("Adding data");
        // add data
        for ( int i = 0; i < series.size(); i++ ) {
        	series.get(i).add(DataPoint.of( 0, values.get(i)));
        }

        plotOptions.setLegendOptions( LegendOptions.create().setShow(false));
        plotOptions.setDefaultColorTheme(colors);
        // create the plot
        plot2 = new SimplePlot( model, plotOptions );
        //HEIGHT value in % has problems, needs to be fixed
        //plot2.setHeight("100%");
        plot2.setWidth("450px");
        //plot2.setHeight(size + "px");
	    mainChartPanel.add(plot2);
	    mainChartPanel.add(descriptions);
	    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
	    	  @Override
	    	  public void execute() {
	    		  int height = descriptions.getOffsetHeight();
		    	  plot2.setHeight(height + "px");
	    	  }
	    });
	}
	
	//DATA FOR COMPARISONS
	String nextDate;
	ArrayList<Info> 		dataList;
	int counting;
	int count;
	ArrayList<SimplePanel>	panels;
	
	protected void getSessionsInformation() {
		dataList = new ArrayList<>();
		RiscossJsonClient.listRiskAnalysisSessions(summary.getTarget(), summary.getRC(), new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());	
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				response = response.isObject().get( "list" );
				count = response.isArray().size();
				counting = 0;
				CodecRASInfo codec = GWT.create( CodecRASInfo.class );
				for( int i = 0; i < response.isArray().size(); i++ ) {
					JRASInfo info = codec.decode( response.isArray().get( i ) );
					RiscossJsonClient.getSessionSummary(info.getId(), new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert(exception.getMessage());
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							JsonRiskAnalysis r = new JsonRiskAnalysis(response);
							nextDate = r.getDate();
							RiscossJsonClient.getSessionResults(r.getID(), new JsonCallback() {
								String d = nextDate;
								@Override
								public void onFailure(Method method,
										Throwable exception) {
									Window.alert(exception.getMessage());
								}
								@Override
								public void onSuccess(Method method,
										JSONValue response) {
									JSONArray results;
									if (response.isObject().get("hresults") == null) {
										results = response.isObject().get( "results" ).isArray();
									}
									else {
										JSONValue entity = response.isObject().get( "hresults" );
										JSONValue v = getEntityResults(entity);
										results = v.isObject().get("results").isArray();
									}
									ArrayList<Double> risks = new ArrayList<>();
									ArrayList<Double> goals = new ArrayList<>();
									for (int s = 0; s < results.isArray().size(); ++s) {
										JSONObject v = results.isArray().get( s ).isObject();
										JsonRiskResult result = new JsonRiskResult( v );
										switch( result.getDataType() ) {
											case EVIDENCE: {
												if (v.get("datatype").isString().stringValue().equals("evidence")) {
													if (v.get("type") == null) {
														risks.add(v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue());
													}
													else if (v.get("type").isString().stringValue().equals("Risk")) {
														risks.add(v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue());
													}
													else if (v.get("type").isString().stringValue().equals("Goal")){
														goals.add(v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue());
													}
												}
											}
											default: break;
										}
									}
									Date date = getDate(d);
									Info inf = new Info(date, risks, goals);
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
		});
	}
	
	public JSONValue getEntityResults(JSONValue entity) {
		if (entity.isObject().get("entity").isString().stringValue().equals(selectedEntity)) return entity;
		else {
			for (int i = 0; i < entity.isObject().get("children").isArray().size(); ++i) {
				JSONValue v = getEntityResults(entity.isObject().get("children").isArray().get(i));
				if (v != null && v.isObject().get("entity").isString().stringValue().equals(selectedEntity)) return v;
			}
		}
		return null;
	}
	
	private class Info {
		
		Date date;
		ArrayList<Double> risks;
		ArrayList<Double> goals;
		
		public Info(Date date, ArrayList<Double> risks, ArrayList<Double> goals) {
			this.date = date;
			this.risks = risks;
			this.goals = goals;
		}
		
		public Date getDate() {
			return date;
		}
		
		public ArrayList<Double> getRisks() {
			return risks;
		}
		
		public ArrayList<Double> getGoals() {
			return goals;
		}
		
		public String getStringDate() {
			String p = String.valueOf(date.getMonth() + 1);
			String s = date.getDate() + "-" + p + "-" + date.getYear() + " " + date.getHours() + "."  + date.getMinutes() + "." + date.getSeconds();
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
		
//		String s = "";
//		for (int i = 0; i < dataList.size(); ++i) {
//			s += dataList.get(i).getStringDate() + "::";
//			for (int j = 0; j < dataList.get(i).getRisks().size(); ++j) {
//				s += dataList.get(i).getRisks().get(j) + "/";
//			}
//			s+= "////";
//			for (int j = 0; j < dataList.get(i).getGoals().size(); ++j) {
//				s += dataList.get(i).getRisks().get(j) + "/";
//			}
//			s+= "|||||";
//		}
//		Window.alert(s);
		
		if (dataList.size() > 1) createComparisonCharts();
	}
	
	protected void createComparisonCharts() {
		for (int i = 0; i < panels.size(); ++i) {
			ArrayList<Double> compare = new ArrayList<>();
			for (int j = 0; j < dataList.size(); ++j) {
				if (i < goals.size()) compare.add(dataList.get(j).getGoals().get(i));
				else compare.add(dataList.get(j).getRisks().get(i-goals.size()));
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
	
	String nextEntityName;
	List<String> entities = new ArrayList<>();
	
	private void generateTree(TreeWidget c, JSONValue entity) {
		nextEntityName = entity.isObject().get("entity").isString().stringValue();
		Anchor a = new Anchor(nextEntityName);
		a.setWidth("100%");
		a.setStyleName("font");
		a.addClickHandler(new ClickHandler() {
			String name = nextEntityName;
			@Override
			public void onClick(ClickEvent event) {
				setSelectedEntity(name);
			}
		});
		HorizontalPanel cPanel = new HorizontalPanel();
		cPanel.setStyleName("tree");
		cPanel.setWidth("100%");
		cPanel.add(a);
		TreeWidget cc = new TreeWidget(cPanel);
		c.addChild(cc);
		results.put(nextEntityName, entity.isObject().get("results").isArray());
		entities.add(nextEntityName);
		for (int i = 0; i < entity.isObject().get("children").isArray().size(); ++i) {
			generateTree(cc, entity.isObject().get("children").isArray().get(i));
		}
	}
	
	public List<String> getEntities() {
		return entities;
	}
	
	private Date getDate (String date) {
		String info[] = date.split(" ");
		String day[] = info[0].split("-");
		String hour[] = info[1].split("\\.");
		Date d = new Date();
		d.setDate(Integer.parseInt(day[0]));
		d.setMonth(Integer.parseInt(day[1])-1);
		d.setYear(Integer.parseInt(day[2]));
		d.setHours(Integer.parseInt(hour[0]));
		d.setMinutes(Integer.parseInt(hour[1]));
		d.setSeconds(Integer.parseInt(hour[2]));
		return d;
		
	}
	
}
