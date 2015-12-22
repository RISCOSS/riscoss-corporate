package eu.riscoss.client.dashboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import eu.riscoss.client.JsonRiskResult;
import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecRASInfo;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.riskanalysis.JsonRiskAnalysis;
import eu.riscoss.shared.JRASInfo;
import eu.riscoss.shared.Pair;

public class MainPageModule implements EntryPoint {

	
	VerticalPanel page = new VerticalPanel();
	HorizontalPanel v = new HorizontalPanel();
	Image logo;
	
	VerticalPanel tablePanel = new VerticalPanel();
	
	@Override
	public void onModuleLoad() {
		page.setWidth("100%");
		page.setStyleName("mainViewLayer");
		
		tablePanel.setWidth("100%");
		tablePanel.setStyleName("leftPanelLayer");
		
		Label title = new Label("Latest risk analysis results");
		title.setStyleName("title");
		page.add(title);
		title.setWidth("100%");
		
		page.add(tablePanel);
		
		getInfo();
		
		RootPanel.get().add(page);
		
//		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
//	    	  @Override
//	    	  public void execute() {
//	    		  int totalWidth = v.getOffsetWidth();
//	    		  int imageWidth = logo.getOffsetWidth();
//	    		  int height = logo.getOffsetHeight();
//	    		  
//	    		  int spacers = (totalWidth - imageWidth)/2;
//	    		  if (spacers > totalWidth/5) {
//	    			  execute();
//	    			  return;
//	    		  }
//	    		  if (totalWidth < Window.getClientWidth()) {
//	    			  execute();
//	    			  return;
//	    		  }
//	    		  
//	    		  HorizontalPanel h1 = new HorizontalPanel();
//	    		  HorizontalPanel h2 = new HorizontalPanel();
//	    		  
//	    		  h1.setStyleName("mainImageLogo");
//	    		  h2.setStyleName("mainImageLogo");
//	    		  
//	    		  h1.setWidth(spacers + "px");
//	    		  h1.setHeight(height + "px");
//	    		  h2.setHeight(height + "px");
//	    		  h2.setWidth(spacers + "px");
//	    		  
//	    		  v.clear();
//	    		  v.add(h1);
//	    		  v.add(logo);
//	    		  v.add(h2);
//	    	  }
//	    });
	}
	
	CellTable<JsonRiskAnalysis>			table;
	ListDataProvider<JsonRiskAnalysis>	dataProvider;
	SimplePager pager;
	
	List<JsonRiskAnalysis> sessions = new ArrayList<>();
	String nextEntity;
	String nextRC;
	
	public void getInfo() {
		RiscossJsonClient.listEntities(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				for (int i = 0; i < response.isArray().size(); ++i) {
					nextEntity = response.isArray().get(i).isObject().get("name").isString().stringValue();
					if (!nextEntity.equals("-"))
					RiscossJsonClient.listRCs(nextEntity, new JsonCallback() {
						String entity = nextEntity;
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert(exception.getMessage());
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							for (int i = 0; i < response.isArray().size(); ++i) {
								nextRC = response.isArray().get(i).isObject().get("name").isString().stringValue();
								RiscossJsonClient.listRiskAnalysisSessions(entity, nextRC, new JsonCallback() {
									@Override
									public void onFailure(Method method,
											Throwable exception) {
										Window.alert(exception.getMessage());
									}
									@Override
									public void onSuccess(Method method,
											JSONValue response) {
										getLastRiskSession(response);
									}
								});
							}
						}
					});
				}
			}
		});
	}
	
	HashMap<String, String> risksList = new HashMap<>();
	int totalC = 0;
	
	public void getLastRiskSession(JSONValue response) {
		if( response == null ) return;
		if( response.isObject() == null ) return;
		response = response.isObject().get( "list" );
		List<JRASInfo> riskSessions = new ArrayList<>();
		CodecRASInfo codec = GWT.create( CodecRASInfo.class );
		if( response.isArray() != null ) {
			for( int i = 0; i < response.isArray().size(); i++ ) {
				JRASInfo info = codec.decode( response.isArray().get( i ) );
				riskSessions.add( info );
			}
		}
		if (response.isArray().size() > 0 ) totalC += 1;
		for (int i = 0; i < riskSessions.size(); ++i) {
			RiscossJsonClient.getSessionSummary(riskSessions.get(i).getId(), new JsonCallback() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert(exception.getMessage());
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					JsonRiskAnalysis j = new JsonRiskAnalysis( response );
					boolean contains = false;
					for (int i = 0; i < sessions.size(); ++i) {
						if (sessions.get(i).getTarget().equals(j.getTarget())
								&& sessions.get(i).getRC().equals(j.getRC())
								&& getDate(sessions.get(i).getDate()).before(getDate(j.getDate()))) {
							sessions.remove(i);
							sessions.add(j);
							return;
						}
						else if (sessions.get(i).getTarget().equals(j.getTarget())
								&& sessions.get(i).getRC().equals(j.getRC())) {
							contains = true;
						}
					}
					if (!contains) sessions.add(j);
					if (totalC == sessions.size()) getRisks();
				}
			});
		}
	}
	
	String nextRAS;
	
	private void getRisks() {
		for (JsonRiskAnalysis j : sessions) {
			nextRAS = j.getID();
			RiscossJsonClient.getSessionResults(j.getID(), new JsonCallback() {
				String ras = nextRAS;
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert(exception.getMessage());
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					JSONArray results = response.isObject().get( "results" ).isArray();
					String res = "";
					for (int s = 0; s < results.isArray().size(); ++s) {
						JSONObject v = results.isArray().get( s ).isObject();
						JsonRiskResult result = new JsonRiskResult( v );
						switch( result.getDataType() ) {
							case EVIDENCE: {
								if (v.get("type") == null && v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue() >= 0.1) {
									if (res.equals("")) res += v.get("id").isString().stringValue();
									else res += ", " + v.get("id").isString().stringValue();
								}
								else if (v.get("type").isString().stringValue().equals("Risk") && v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue() >= 0.1) {
									if (res.equals("")) res += v.get("id").isString().stringValue();
									else res += ", " + v.get("id").isString().stringValue();
								}
								break;
							}
							case DISTRIBUTION: {
								if (res.equals("")) res += v.get("id").isString().stringValue();
								else res += ", " + v.get("id").isString().stringValue();
								break;
							}
							default: break;
						}
					}
					if (res.equals("")) res = "-";
					risksList.put(ras, res);
					generateRiskSessionsChart();
				}
			});
		}
	}
	
	public void generateRiskSessionsChart() {
		table = new CellTable<>(15, (Resources) GWT.create(TableResources.class));

		Column<JsonRiskAnalysis, String> entity = new Column<JsonRiskAnalysis, String>(new TextCell()) {
			@Override
			public String getValue(JsonRiskAnalysis arg0) {
				return arg0.getTarget();
			}
		};
		Column<JsonRiskAnalysis, String> risk = new Column<JsonRiskAnalysis, String>(new TextCell()) {
			@Override
			public String getValue(JsonRiskAnalysis arg0) {
				return arg0.getRC();
			}
		};
		Column<JsonRiskAnalysis, String> riskList = new Column<JsonRiskAnalysis, String>(new TextCell()) {
			@Override
			public String getValue(JsonRiskAnalysis arg0) {
				String s = arg0.getID();
				return risksList.get(s);
			}
		};
		Column<JsonRiskAnalysis, String> date = new Column<JsonRiskAnalysis, String>(new TextCell()) {
			@Override
			public String getValue(JsonRiskAnalysis arg0) {
				return arg0.getDate();
			}
		};
		table.setRowData(0, sessions);
		
		final SelectionModel<JsonRiskAnalysis> selectionModel = new SingleSelectionModel<JsonRiskAnalysis>();
	    table.setSelectionModel(selectionModel);
	    selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent arg0) {
				List<JsonRiskAnalysis> l = dataProvider.getList();
				for (int i = 0; i < l.size(); ++i) {
					if (selectionModel.isSelected(l.get(i))) {
						Window.Location.replace("riskanalysis.jsp?or=main&id=" + l.get(i).getID());
					}
				}
			}
	    });
	    
	    table.addColumn(entity, "Entity");
	    table.addColumn(risk, "Risk configuration");
	    table.addColumn(riskList, "Risks");
	    table.addColumn(date, "Last execution time");
		
		dataProvider = new ListDataProvider<JsonRiskAnalysis>();
		dataProvider.addDataDisplay( table );
		
		for( int i = 0; i < sessions.size(); i++ ) {
			dataProvider.getList().add( sessions.get(i) );
		}
		
		pager = new SimplePager();
	    pager.setDisplay( table );
	    
	    table.setWidth("100%");
	    
	    tablePanel.clear();
		tablePanel.add( table );
		tablePanel.add( pager );
		
//		table.setWidth("400px");
//		tablePanel.setWidth("400px");
		
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
