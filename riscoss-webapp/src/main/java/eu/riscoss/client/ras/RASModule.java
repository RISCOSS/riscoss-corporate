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
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;

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

public class RASModule implements EntryPoint {

	SimplePanel					panel = new SimplePanel();
	
	CellTable<Pair<JsonRiskAnalysis, Boolean>>			table;
	ListDataProvider<Pair<JsonRiskAnalysis, Boolean>>	dataProvider;
	SimplePager					pager = new SimplePager();
	
	VerticalPanel		page = new VerticalPanel();
	HorizontalPanel		mainView = new HorizontalPanel();
	VerticalPanel		leftPanel = new VerticalPanel();
	VerticalPanel		rightPanel = new VerticalPanel();
	Label title;
	
	VerticalPanel 		tablePanel = new VerticalPanel();
	
	public native void exportJS() /*-{
	var that = this;
	$wnd.setSelectedRAS = $entry(function(amt) {
		that.@eu.riscoss.client.ras.RASModule::setSelectedRAS(Ljava/lang/String;)(amt);
	});
	}-*/;
	
	List<String> comparison = new ArrayList<>();
	Button compare;
	
	@Override
	public void onModuleLoad() {
		exportJS();
		
		table = new CellTable<Pair<JsonRiskAnalysis, Boolean>>(15, (Resources) GWT.create(TableResources.class));
		
		compare = new Button("Compare");
		compare.setStyleName("button");
		compare.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				generateComparison();
			}
		});
		
		Column<Pair<JsonRiskAnalysis, Boolean>,Boolean> column = new Column<Pair<JsonRiskAnalysis, Boolean>, Boolean> (new CheckboxCell()) {
			@Override
			public Boolean getValue(Pair<JsonRiskAnalysis, Boolean> object) {
				return object.getRight();
			}
		};
		column.setFieldUpdater(new FieldUpdater<Pair<JsonRiskAnalysis, Boolean>, Boolean>() {
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
		
		table.addColumn(column);
		table.addColumn( new Column<Pair<JsonRiskAnalysis, Boolean>,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(Pair<JsonRiskAnalysis, Boolean> object) {
				return new LinkHtml( object.getLeft().getName(), "javascript:setSelectedRAS(\"" + object.getLeft().getID() + "\")" ); };
		}, "Session name");
		table.addColumn( new Column<Pair<JsonRiskAnalysis, Boolean>,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(Pair<JsonRiskAnalysis, Boolean> object) {
				return new LinkHtml( object.getLeft().getTarget(), "javascript:setSelectedRAS(\"" + object.getLeft().getID() + "\")" ); };
		}, "Entity");
		table.addColumn( new Column<Pair<JsonRiskAnalysis, Boolean>,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(Pair<JsonRiskAnalysis, Boolean> object) {
				return new LinkHtml( object.getLeft().getRC(), "javascript:setSelectedRAS(\"" + object.getLeft().getID() + "\")" ); };
		}, "Risk configuration");
		table.addColumn( new Column<Pair<JsonRiskAnalysis, Boolean>,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(Pair<JsonRiskAnalysis, Boolean> object) {
				return new LinkHtml( object.getLeft().getDate(), "javascript:setSelectedRAS(\"" + object.getLeft().getID() + "\")" ); };
		}, "Execution time");
		
		
		
		dataProvider = new ListDataProvider<Pair<JsonRiskAnalysis, Boolean>>();
		dataProvider.addDataDisplay( table );
		
		SimplePager pager = new SimplePager();
	    pager.setDisplay( table );
	    
		tablePanel = new VerticalPanel();
		tablePanel.add( table );
		tablePanel.add( pager );
		
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
		leftPanel.add(panel);
		mainView.add(leftPanel);
		page.add(mainView);
		
		//RootPanel.get().add( panel );
		RootPanel.get().add( page );
		
		loadRASList();
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

	public void loadRASList() {
		
		dataProvider.getList().clear();
		
		RiscossJsonClient.listRiskAnalysisSessions("", "", new JsonCallback(){
			public void onSuccess(Method method, JSONValue response) {
				if( response == null ) return;
				if( response.isObject() == null ) return;
				response = response.isObject().get( "list" );
				if( response.isArray() != null ) {
					CodecRASInfo codec = GWT.create( CodecRASInfo.class );
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JRASInfo info = codec.decode( response.isArray().get( i ) );
						RiscossJsonClient.getSessionSummary(info.getId(), new JsonCallback() {
							@Override
							public void onFailure(Method method,
									Throwable exception) {
								Window.alert(exception.getMessage());
							}
							@Override
							public void onSuccess(Method method,
									JSONValue response) {
								Pair p = new Pair(new JsonRiskAnalysis(response), false);
								dataProvider.getList().add( p);
							}
						});
					}
				}
			}
			
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
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
				// TODO Auto-generated method stub
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				/*rightPanel.clear();
				JsonRiskAnalysis json = new JsonRiskAnalysis( response );
				Label title = new Label(json.getName());
				title.setStyleName("subtitle");
				rightPanel.add(title);
				rightPanel.add(rasPanel);*/
				page.clear();
				page.setStyleName("leftPanelLayer");
				JsonRiskAnalysis json = new JsonRiskAnalysis( response );
				/*Label title = new Label(json.getName());
				title.setStyleName("subtitle");
				page.add(title);*/
				page.add(rasPanel);
			}
		});
	}
	
	protected void deleteRAS( JRASInfo info ) {
		//new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + RiscossJsonClient.getDomain() + "/session/" + info.getId() + "/delete" ).delete().send( new JsonCallbackWrapper<JRASInfo>( info ) 
			
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
