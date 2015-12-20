package eu.riscoss.client.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.riskanalysis.JsonRiskAnalysis;
import eu.riscoss.shared.Pair;

public class MainPageModule implements EntryPoint {

	
	VerticalPanel page = new VerticalPanel();
	HorizontalPanel v = new HorizontalPanel();
	Image logo;
	
	@Override
	public void onModuleLoad() {
		page.setWidth("100%");
		page.setStyleName("mainViewLayer");
		
		v = new HorizontalPanel();
		v.setWidth("100%");
		v.setStyleName("mainImage");
		logo = new Image( "resources/main_view.jpg" );
		logo.setStyleName("mainImageLogo");
		v.add(logo);
		
		page.add(v);
		
		getInfo();
		
		RootPanel.get().add(page);
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
	    	  @Override
	    	  public void execute() {
	    		  int totalWidth = v.getOffsetWidth();
	    		  int imageWidth = logo.getOffsetWidth();
	    		  int height = logo.getOffsetHeight();
	    		  
	    		  int spacers = (totalWidth - imageWidth)/2;
	    		  if (spacers > totalWidth/5) {
	    			  execute();
	    			  return;
	    		  }
	    		  
	    		  HorizontalPanel h1 = new HorizontalPanel();
	    		  HorizontalPanel h2 = new HorizontalPanel();
	    		  
	    		  h1.setStyleName("mainImageLogo");
	    		  h2.setStyleName("mainImageLogo");
	    		  
	    		  h1.setWidth(spacers + "px");
	    		  h1.setHeight(height + "px");
	    		  h2.setHeight(height + "px");
	    		  h2.setWidth(spacers + "px");
	    		  
	    		  v.clear();
	    		  v.add(h1);
	    		  v.add(logo);
	    		  v.add(h2);
	    	  }
	    });
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
					
					RiscossJsonClient.listRCs(response.isArray().get(i).isObject().get("name").isString().stringValue(), new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert(exception.getMessage());
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							//RiscossJsonClient.listRiskAnalysisSessions(entity, rc, cb);
						}
					});
				}
			}
		});
	}
	
	public void generateRiskSessionsChart() {
		table = new CellTable<>(15, (Resources) GWT.create(TableResources.class));
		
		Column<JsonRiskAnalysis, String> name = new Column<JsonRiskAnalysis, String>(new TextCell()) {
			@Override
			public String getValue(JsonRiskAnalysis arg0) {
				return arg0.getName();
			}
		};
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
						Window.Location.replace("riskanalysis.jsp?id" + l.get(i).getID());
					}
				}
			}
	    });
		
	}

}
