package eu.riscoss.client.ras;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.BlacklistEventTranslator;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.JsonRiskResult;
import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossCall;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecRASInfo;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.riskanalysis.JsonRiskAnalysis;
import eu.riscoss.client.riskanalysis.RASPanel;
import eu.riscoss.client.ui.LinkHtml;
import eu.riscoss.shared.JRASInfo;
import eu.riscoss.shared.Pair;
import eu.riscoss.shared.RiscossUtil;

public class RASModule implements EntryPoint {

	SimplePanel					panel = new SimplePanel();
	
	CellTable<Pair<JsonRiskAnalysis, Boolean>>			table;
	ListDataProvider<Pair<JsonRiskAnalysis, Boolean>>	dataProvider;
	SimplePager					pager = new SimplePager();
	SelectionModel< Pair<JsonRiskAnalysis, Boolean> > selectionModel;
	DefaultSelectionEventManager.BlacklistEventTranslator<Pair<JsonRiskAnalysis, Boolean>> blackList;
	
	Column<Pair<JsonRiskAnalysis, Boolean>,String> rasName;
	Column<Pair<JsonRiskAnalysis, Boolean>,String> target;
	Column<Pair<JsonRiskAnalysis, Boolean>,String> riskConf;
	Column<Pair<JsonRiskAnalysis, Boolean>,String> date;
	Column<Pair<JsonRiskAnalysis, Boolean>,Boolean> checked;
	
	VerticalPanel		page = new VerticalPanel();
	HorizontalPanel		mainView = new HorizontalPanel();
	VerticalPanel		leftPanel = new VerticalPanel();
	VerticalPanel		rightPanel = new VerticalPanel();
	Label title;
	
	VerticalPanel 		tablePanel = new VerticalPanel();
	
	List<String> comparison = new ArrayList<>();
	Button compare;
	
	@Override
	public void onModuleLoad() {
		
		table = new CellTable<Pair<JsonRiskAnalysis, Boolean>>(15, (Resources) GWT.create(TableResources.class));
				
		compare = new Button("Compare");
		compare.setStyleName("button");
		compare.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				generateComparison();
			}
		});
		
		blackList = new DefaultSelectionEventManager.BlacklistEventTranslator<Pair<JsonRiskAnalysis, Boolean>>(0);
		
		selectionModel = new SingleSelectionModel<Pair<JsonRiskAnalysis, Boolean>>();
	    selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent arg0) {
				List<Pair<JsonRiskAnalysis, Boolean>> l = dataProvider.getList();
				for (int i = 0; i < l.size(); ++i) {
					if (selectionModel.isSelected(l.get(i))) {
						Window.Location.replace("riskanalysis.jsp?or=main&id=" + l.get(i).getLeft().getID());
					}
				}
			}
	    });
		
		checked = new Column<Pair<JsonRiskAnalysis, Boolean>, Boolean> (new CheckboxCell()) {
			@Override
		    public Boolean getValue(Pair<JsonRiskAnalysis, Boolean> object)
		    {
		        return object.getRight();
		    }
		};
		checked.setFieldUpdater(new FieldUpdater<Pair<JsonRiskAnalysis, Boolean>, Boolean>() {
			@Override
			public void update(int index,
					Pair<JsonRiskAnalysis, Boolean> object, Boolean value) {
				object.setRight(value);
				if (value) comparison.add(object.getLeft().getID());
				else comparison.remove(object.getLeft().getID());
				if (comparison.size() >= 2) leftPanel.add(compare);
				else leftPanel.remove(compare);
			}
		});
		
		rasName =  new Column<Pair<JsonRiskAnalysis, Boolean>,String>(new TextCell() ) {
			@Override
			public String getValue(Pair<JsonRiskAnalysis, Boolean> object) {
				return object.getLeft().getName();
			}
		};
		rasName.setSortable(true);
		target =  new Column<Pair<JsonRiskAnalysis, Boolean>,String>(new TextCell() ) {
			@Override
			public String getValue(Pair<JsonRiskAnalysis, Boolean> object) {
				return object.getLeft().getTarget();
			}
		};
		riskConf =  new Column<Pair<JsonRiskAnalysis, Boolean>,String>(new TextCell() ) {
			@Override
			public String getValue(Pair<JsonRiskAnalysis, Boolean> object) {
				return object.getLeft().getRC();
			}
		};
		date =  new Column<Pair<JsonRiskAnalysis, Boolean>,String>(new TextCell() ) {
			@Override
			public String getValue(Pair<JsonRiskAnalysis, Boolean> object) {
				return object.getLeft().getDate();
			}
		};
		
		table.addColumn(checked);
		table.addColumn(rasName, "Session name");
		table.addColumn(target, "Entity");
		table.addColumn(riskConf, "Risk configuration");
		table.addColumn(date, "Execution time");
		
		rasQueryString = "";
		rcQueryString = "";
		targetQueryString = "";
		
		searchRAS();
		
		mainView.setStyleName("mainViewLayer");
		//mainView.setWidth("100%");
		leftPanel.setStyleName("leftPanelLayer");
		//leftPanel.setHeight("100%");
		rightPanel.setStyleName("rightPanelLayer");
		page.setWidth("100%");
		
		title = new Label("Risk Analysis Sessions");
		title.setStyleName("title");
		
		panel.setWidget( tablePanel);
		panel.setStyleName("margin-left");
		page.add(title);
		leftPanel.add(searchFields());
		panel.getElement().getStyle().setMarginTop(10, Unit.PX);
		leftPanel.add(panel);
		leftPanel.setWidth("100%");
		mainView.setWidth("95%");
		mainView.add(leftPanel);
		page.add(mainView);
		
		//RootPanel.get().add( panel );
		RootPanel.get().add( page );
	}

	private void searchRAS() {
		RiscossJsonClient.searchRAS(rasQueryString, targetQueryString, rcQueryString, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				reloadTable(response);
				
			}
		});
	}
	
	private void reloadTable(JSONValue response) {

		List< Pair<JsonRiskAnalysis, Boolean> > results = new ArrayList<>();
		for (int i = 0; i < response.isArray().size(); ++i) {
			JsonRiskAnalysis obj = new JsonRiskAnalysis(response.isArray().get(i));
			Boolean b;
			if (comparison.contains(obj.getID())) b = true;
			else b = false;
			results.add(new Pair<>(obj, b));
		}
		
	    table.setSelectionModel(selectionModel, DefaultSelectionEventManager.<Pair<JsonRiskAnalysis, Boolean>>createBlacklistManager(0));
	    
	    table.setRowData(0, results);
	    
	    dataProvider = new ListDataProvider<Pair<JsonRiskAnalysis, Boolean>>();
		dataProvider.addDataDisplay( table );
		
		for( int i = 0; i < results.size(); i++ ) {
			dataProvider.getList().add( results.get(i) );
		}
		
		SimplePager pager = new SimplePager();
	    pager.setDisplay( table );
	    
	    tablePanel.clear();
		tablePanel.add( table );
		tablePanel.add( pager );
		table.setWidth("100%");
		tablePanel.setWidth("100%");
		panel.setWidth("100%");
		panel.setWidget(tablePanel);
	}
		
	protected void generateComparison() {
		page.clear();
		ComparisonPanel cp = new ComparisonPanel(comparison) {
			@Override
			public void back() {
				page.clear();
				page.add(title);
				page.add(mainView);
			}
		};
		page.add(cp.getWidget());
	}
	
	TextBox rasFilterQuery = new TextBox();
	ListBox targetFilterQuery = new ListBox();
	ListBox rcFilterQuery = new ListBox();
	
	String rasQueryString;
	String targetQueryString;
	String rcQueryString;
	
	private HorizontalPanel searchFields() {
		HorizontalPanel h = new HorizontalPanel();
		
		Label filterlabel = new Label("Search ras: ");
		filterlabel.setStyleName("bold");
		h.add(filterlabel);
		
		rasFilterQuery.setWidth("200px");
		rasFilterQuery.setStyleName("layerNameField");
		h.add(rasFilterQuery);
		
		rasFilterQuery.addKeyUpHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (rasFilterQuery.getText() != null){
					String tmp = RiscossUtil.sanitize(rasFilterQuery.getText());
					if (!tmp.equals(rasQueryString)) {
						rasQueryString = tmp;
						searchRAS();
					}
				}
			}
		});
		
		
		Label targetlabel = new Label("Entity: ");
		targetlabel.setStyleName("bold");
		targetlabel.getElement().getStyle().setMarginLeft(50, Unit.PX);
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

	public void back() {
		Window.Location.reload();
	}
	
	RASPanel rasPanel;
	
	public void setSelectedRAS( String ras ) {
		rasPanel = new RASPanel(null);
		rasPanel.setBrowse(this);
		rasPanel.loadRAS(ras);
		RiscossJsonClient.getSessionSummary(ras, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				page.clear();
				page.setStyleName("leftPanelLayer");
				JsonRiskAnalysis json = new JsonRiskAnalysis( response );
				page.add(rasPanel);
			}
		});
	}
	
	protected void deleteRAS( JRASInfo info ) {
			
		RiscossJsonClient.deleteRiskAnalysisSession(info.getId(), new JsonCallbackWrapper<JRASInfo>( info ){
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				dataProvider.getList().remove( getValue() );
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}

}
