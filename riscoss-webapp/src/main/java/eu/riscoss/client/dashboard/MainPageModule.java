package eu.riscoss.client.dashboard;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import eu.riscoss.client.JsonRiskResult;
import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.ras.ComparisonPanel;
import eu.riscoss.shared.Pair;
import eu.riscoss.shared.RiscossUtil;

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
	Label title;
	
	@Override
	public void onModuleLoad() {
		page.setWidth("100%");
		page.setStyleName("mainViewLayer");
		
		tablePanel.setWidth("100%");
		tablePanel.setStyleName("leftPanelLayer");
		
		title = new Label("Latest risk analysis results");
		title.setStyleName("title");
		page.add(title);
		title.setWidth("100%");

		page.add(searchFields());

		page.add(tablePanel);
		
		getSessionsInfo();
		
		//getInfo();
		
		RootPanel.get().add(page);
		
	}
	
	CellTable<Pair<RiskAnalysisSessionInfo, Boolean>>			table;
	ListDataProvider<Pair<RiskAnalysisSessionInfo, Boolean>>	dataProvider;
	SimplePager pager;
	
	Column<Pair<RiskAnalysisSessionInfo, Boolean>, String> entity;
	Column<Pair<RiskAnalysisSessionInfo, Boolean>, String> rc;
	Column<Pair<RiskAnalysisSessionInfo, Boolean>, String> riskList;
	Column<Pair<RiskAnalysisSessionInfo, Boolean>, String> date;
	Column<Pair<RiskAnalysisSessionInfo, Boolean>, Boolean> checked;
	
	List<String> comparison = new ArrayList<>();
	
	List<Pair<RiskAnalysisSessionInfo, Boolean>> sessions = new ArrayList<>();
	
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
					Log.println(i+"");
					try {
						JSONValue obj = JSONParser.parseLenient(r.isString().stringValue());
						JSONValue results = obj.isObject().get("results");
						String res = "";
						List<String> risks = new ArrayList<>();
						for (int s = 0; s < results.isArray().size(); ++s) {
							JSONObject v = results.isArray().get( s ).isObject();
							JsonRiskResult result = new JsonRiskResult( v );
							switch( result.getDataType() ) {
								case EVIDENCE: {
									if (v.get("type") == null && v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue() >= 0.1) {
										if (res.equals("")) res += v.get("id").isString().stringValue();
										else res += ", " + v.get("id").isString().stringValue();
										risks.add(v.get("id").isString().stringValue());
									}
									else if (v.get("type").isString().stringValue().equals("Risk") && v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue() >= 0.1) {
										if (res.equals("")) res += v.get("id").isString().stringValue();
										else res += ", " + v.get("id").isString().stringValue();
										risks.add(v.get("id").isString().stringValue());
									}
									break;
								}
								case DISTRIBUTION: {
									if (res.equals("")) res += v.get("id").isString().stringValue();
									else res += ", " + v.get("id").isString().stringValue();
									risks.add(v.get("id").isString().stringValue());
									break;
								}
								default: break;
							}
						}
						if (res.equals("")) res = "-";
						sessions.add(new Pair<RiskAnalysisSessionInfo, Boolean>(new RiskAnalysisSessionInfo(
								response.isArray().get(i).isObject().get( "target" ).isString().stringValue(),
								response.isArray().get(i).isObject().get( "rc" ).isString().stringValue(),
								response.isArray().get(i).isObject().get( "timestamp" ).isString().stringValue(),
								response.isArray().get(i).isObject().get( "id" ).isString().stringValue(),
								res), false));
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				generateRiskSessionsChart();
			}
		});
	}
	
	Button compare;
	
	public void generateRiskSessionsChart() {
		table = new CellTable<>(15, (Resources) GWT.create(TableResources.class));
		
		compare = new Button("Compare");
		compare.setStyleName("button");
		compare.getElement().getStyle().setMarginLeft(30, Unit.PX);
		compare.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				generateComparison();
			}
		});
		
		checked = new Column<Pair<RiskAnalysisSessionInfo, Boolean>, Boolean> (new CheckboxCell()) {
			@Override
		    public Boolean getValue(Pair<RiskAnalysisSessionInfo, Boolean> object)
		    {
		        return object.getRight();
		    }
		};
		checked.setFieldUpdater(new FieldUpdater<Pair<RiskAnalysisSessionInfo, Boolean>, Boolean>() {
			@Override
			public void update(int index,
					Pair<RiskAnalysisSessionInfo, Boolean> object, Boolean value) {
				object.setRight(value);
				if (value) comparison.add(object.getLeft().id);
				else comparison.remove(object.getLeft().id);
				if (comparison.size() >= 2) page.add(compare);
				else page.remove(compare);
			}
		});
		
		entity = new Column<Pair<RiskAnalysisSessionInfo, Boolean>, String>(new TextCell()) {
			@Override
			public String getValue(Pair<RiskAnalysisSessionInfo, Boolean> arg0) {
				return arg0.getLeft().target;
			}
		};
		rc = new Column<Pair<RiskAnalysisSessionInfo, Boolean>, String>(new TextCell()) {
			@Override
			public String getValue(Pair<RiskAnalysisSessionInfo, Boolean> arg0) {
				return arg0.getLeft().rc;
			}
		};
		riskList = new Column<Pair<RiskAnalysisSessionInfo, Boolean>, String>(new TextCell()) {
			@Override
			public String getValue(Pair<RiskAnalysisSessionInfo, Boolean> arg0) {
				return arg0.getLeft().results;
			}
		};
		date = new Column<Pair<RiskAnalysisSessionInfo, Boolean>, String>(new TextCell()) {
			@Override
			public String getValue(Pair<RiskAnalysisSessionInfo, Boolean> arg0) {
				return arg0.getLeft().timestamp;
			}
		};
		
		table.setColumnWidth(4, "180px");
		
		final SelectionModel<Pair<RiskAnalysisSessionInfo, Boolean>> selectionModel = new SingleSelectionModel<Pair<RiskAnalysisSessionInfo, Boolean>>();
	    table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Pair<RiskAnalysisSessionInfo, Boolean>>createBlacklistManager(0));
	    selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent arg0) {
				List<Pair<RiskAnalysisSessionInfo, Boolean>> l = dataProvider.getList();
				for (int i = 0; i < l.size(); ++i) {
					if (selectionModel.isSelected(l.get(i))) {
						Window.Location.replace("riskanalysis.jsp?or=main&id=" + l.get(i).getLeft().id);
					}
				}
			}
	    });
	    
	    table.addColumn(checked);
	    table.addColumn(entity, "Entity");
	    table.addColumn(rc, "Risk configuration");
	    table.addColumn(riskList, "Risks");
	    table.addColumn(date, "Last execution time");
	    
	    reloadTable(sessions);
		
	}
	
	protected void generateComparison() {
		page.clear();
		ComparisonPanel cp = new ComparisonPanel(comparison) {
			@Override
			public void back() {
				page.clear();
				page.add(title);
				page.add(searchFields());
				page.add(tablePanel);
			}
		};
		page.add(cp.getWidget());
	}

	private void reloadTable(List<Pair<RiskAnalysisSessionInfo, Boolean>> sessions) {
		table.setRowData(0, sessions);
		
		dataProvider = new ListDataProvider<Pair<RiskAnalysisSessionInfo, Boolean>>();
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
	
	ListBox targetFilterQuery = new ListBox();
	ListBox rcFilterQuery = new ListBox();
	TextBox riskFilterQuery = new TextBox();
	
	String targetQueryString = "";
	String rcQueryString = "";
	String riskQueryString = "";
	
	private HorizontalPanel searchFields() {
		HorizontalPanel h = new HorizontalPanel();	
		h.getElement().getStyle().setMarginTop(12, Unit.PX);
		
		Label targetlabel = new Label("Entity: ");
		targetlabel.setStyleName("bold");
		targetlabel.getElement().getStyle().setMarginLeft(36, Unit.PX);
		h.add(targetlabel);
		
		targetFilterQuery.setWidth("200px");
		targetFilterQuery.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				targetQueryString = targetFilterQuery.getItemText(targetFilterQuery.getSelectedIndex());
				if (targetQueryString.equals("all")) targetQueryString = "";
				searchRAS();
			}
		});
		h.add(targetFilterQuery);
		
		Label rclabel = new Label("Risk configuration: ");
		rclabel.setStyleName("bold");
		rclabel.getElement().getStyle().setMarginLeft(50, Unit.PX);
		h.add(rclabel);
		
		rcFilterQuery.setWidth("200px");
		rcFilterQuery.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				rcQueryString = rcFilterQuery.getItemText(rcFilterQuery.getSelectedIndex());
				if (rcQueryString.equals("all")) rcQueryString = "";
				searchRAS();
			}
		});
		h.add(rcFilterQuery);
		
		Label filterlabel = new Label("Search by risks: ");
		filterlabel.setStyleName("bold");
		filterlabel.getElement().getStyle().setMarginLeft(50, Unit.PX);
		h.add(filterlabel);
		
		riskFilterQuery.setWidth("200px");
		riskFilterQuery.setStyleName("layerNameField");
		h.add(riskFilterQuery);
		
		riskFilterQuery.addKeyUpHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (riskFilterQuery.getText() != null){
					String tmp = RiscossUtil.sanitize(riskFilterQuery.getText());
					if (!tmp.equals(riskQueryString)) {
						riskQueryString = tmp;
						searchRAS();
					}
				}
			}
		});
		
		loadEntities();
		loadRCs();
		
		return h;
	}
	
	private void loadRCs() {
		RiscossJsonClient.listRCs(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				rcFilterQuery.addItem("all");
				for (int i = 0; i < response.isArray().size(); ++i) {
					rcFilterQuery.addItem(response.isArray().get(i).isObject().get("name").isString().stringValue());
				}
			}
		});
	}

	private void loadEntities() {
		RiscossJsonClient.listEntities(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				targetFilterQuery.addItem("all");
				for (int i = 0; i < response.isArray().size(); ++i) {
					String s = response.isArray().get(i).isObject().get("name").isString().stringValue();
					if (!s.equals("-"))
						targetFilterQuery.addItem(s);
				}
			}
		});
	}

	protected void searchRAS() {
		List<Pair<RiskAnalysisSessionInfo, Boolean>> searchedSessions = new ArrayList<>();
		for (Pair<RiskAnalysisSessionInfo, Boolean> ras : sessions) {
			Boolean b = true;
			if (!targetQueryString.equals("") && !targetQueryString.equals(ras.getLeft().target)) b = false;
			if (!rcQueryString.equals("") && !rcQueryString.equals(ras.getLeft().rc)) b = false;
			if (!riskQueryString.equals("") && !ras.getLeft().results.toLowerCase().contains(riskQueryString.toLowerCase())) b = false;
			if (b) searchedSessions.add(ras);
		}
		reloadTable(searchedSessions);
	}

}
