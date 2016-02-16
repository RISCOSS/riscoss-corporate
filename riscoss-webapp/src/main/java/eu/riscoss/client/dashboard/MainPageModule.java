package eu.riscoss.client.dashboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
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
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.entities.TableResources;

public class MainPageModule implements EntryPoint {

	
	private class RiskAnalysisSessionInfo {
		String target;
		String rc;
		String timestamp;
		String id;
		String results;
		
		public RiskAnalysisSessionInfo(String target, String rc, String timestamp, String id, String results) {
			this.target = target;
			this.rc = rc;
			this.timestamp = timestamp;
			this.id = id;
			this.results = results;
		}
	}
	
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
		
		getSessionsInfo();
		
		//getInfo();
		
		RootPanel.get().add(page);
		
	}
	
	CellTable<RiskAnalysisSessionInfo>			table;
	ListDataProvider<RiskAnalysisSessionInfo>	dataProvider;
	SimplePager pager;
	
	List<RiskAnalysisSessionInfo> sessions = new ArrayList<>();
	String nextEntity;
	String nextRC;
	
	public void getSessionsInfo() {
		RiscossJsonClient.getLastRiskAnalysisSessions(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				for (int i = 0; i < response.isArray().size(); ++i) {
					JSONValue r = response.isArray().get(i).isObject().get( "res" );
					JSONValue obj = JSONParser.parseLenient(r.isString().stringValue());
					JSONValue results = obj.isObject().get("results");
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
					sessions.add(new RiskAnalysisSessionInfo(
							response.isArray().get(i).isObject().get( "target" ).isString().stringValue(),
							response.isArray().get(i).isObject().get( "rc" ).isString().stringValue(),
							response.isArray().get(i).isObject().get( "timestamp" ).isString().stringValue(),
							response.isArray().get(i).isObject().get( "id" ).isString().stringValue(),
							res));
				}
				generateRiskSessionsChart();
			}
		});
	}
	
	public void generateRiskSessionsChart() {
//		Window.alert("BEGIN");
		table = new CellTable<>(15, (Resources) GWT.create(TableResources.class));
//		Iterator it = risksList.entrySet().iterator();
//	    while (it.hasNext()) {
//	        Map.Entry pair = (Map.Entry)it.next();
//	        Window.alert(pair.getKey() + "///" + pair.getValue());
//	    }

		Column<RiskAnalysisSessionInfo, String> entity = new Column<RiskAnalysisSessionInfo, String>(new TextCell()) {
			@Override
			public String getValue(RiskAnalysisSessionInfo arg0) {
				return arg0.target;
			}
		};
		Column<RiskAnalysisSessionInfo, String> risk = new Column<RiskAnalysisSessionInfo, String>(new TextCell()) {
			@Override
			public String getValue(RiskAnalysisSessionInfo arg0) {
				return arg0.rc;
			}
		};
		Column<RiskAnalysisSessionInfo, String> riskList = new Column<RiskAnalysisSessionInfo, String>(new TextCell()) {
			@Override
			public String getValue(RiskAnalysisSessionInfo arg0) {
				return arg0.results;
			}
		};
		Column<RiskAnalysisSessionInfo, String> date = new Column<RiskAnalysisSessionInfo, String>(new TextCell()) {
			@Override
			public String getValue(RiskAnalysisSessionInfo arg0) {
				return arg0.timestamp;
			}
		};
		table.setRowData(0, sessions);
		table.setColumnWidth(3, "180px");
		
		final SelectionModel<RiskAnalysisSessionInfo> selectionModel = new SingleSelectionModel<RiskAnalysisSessionInfo>();
	    table.setSelectionModel(selectionModel);
	    selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent arg0) {
				List<RiskAnalysisSessionInfo> l = dataProvider.getList();
				for (int i = 0; i < l.size(); ++i) {
					if (selectionModel.isSelected(l.get(i))) {
						Window.Location.replace("riskanalysis.jsp?or=main&id=" + l.get(i).id);
					}
				}
			}
	    });
	    
	    table.addColumn(entity, "Entity");
	    table.addColumn(risk, "Risk configuration");
	    table.addColumn(riskList, "Risks");
	    table.addColumn(date, "Last execution time");
		
		dataProvider = new ListDataProvider<RiskAnalysisSessionInfo>();
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
