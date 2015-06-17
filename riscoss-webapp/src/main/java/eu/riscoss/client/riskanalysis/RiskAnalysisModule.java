package eu.riscoss.client.riskanalysis;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.EntityInfo;
import eu.riscoss.client.ModelInfo;
import eu.riscoss.client.ui.LinkHtml;

public class RiskAnalysisModule implements EntryPoint {

	DockPanel dock;

	CellTable<EntityInfo> entityTable;
	CellTable<ModelInfo> modelTable;

	ListDataProvider<EntityInfo> entityDataProvider;
	ListDataProvider<ModelInfo> modelDataProvider;

	Label entityLabel = new Label("-");
	Label rcLabel = new Label("-");
	
	EntityInfo selection = null;
	
	public void onModuleLoad() {

		exportJS();

		entityTable = new CellTable<EntityInfo>();
		modelTable = new CellTable<ModelInfo>();
		
		entityTable.addColumn( new Column<EntityInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(EntityInfo object) {
				return new LinkHtml( object.getName(), "javascript:setSelectedEntity(\"" + object.getName() + "\")" ); };
			}
		, "Entity");
		
		modelTable.addColumn( new Column<ModelInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(ModelInfo object) {
				return new LinkHtml( object.getName(), "javascript:setSelectedRC(\"" + object.getName() + "\")" ); };
			}
		, "Risk Configuration");
		//	    
		entityDataProvider = new ListDataProvider<EntityInfo>();
		entityDataProvider.addDataDisplay(entityTable);
		modelDataProvider = new ListDataProvider<ModelInfo>();
		modelDataProvider.addDataDisplay(modelTable);
		
		Grid grid = new Grid( 3, 2 );
		Button btn = new Button( "Run Analysis", new ClickHandler() {
			public void onClick(ClickEvent event) {
				runAnalysis();
			}
		});
		grid.setWidget( 0, 0, new Label( "Selected entity:" ) );
		grid.setWidget( 1, 0, new Label( "Selected risk configuration:" ) );
		grid.setWidget( 0, 1, entityLabel );
		grid.setWidget( 1, 1, rcLabel );
		grid.setWidget( 2, 1, btn );
		
		dock = new DockPanel();

		dock.setSize( "100%", "100%" );

		dock.add( grid, DockPanel.SOUTH );
		dock.add( entityTable, DockPanel.WEST );
		dock.add( modelTable, DockPanel.EAST );

		RootPanel.get().add( dock );

		loadEntities();
	}

	void loadEntities() {
		Resource resource = new Resource( GWT.getHostPageBaseURL() + "api/entities/list");

		resource.get().send( new JsonCallback() {

			public void onSuccess(Method method, JSONValue response) {
				if( response.isArray() != null ) {
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						entityDataProvider.getList().add( new EntityInfo( 
								o.get( "name" ).isString().stringValue() ) );
					}
				}
				loadRiskConfs();
			}

			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
	}

	void loadRiskConfs() {
		Resource resource = new Resource( GWT.getHostPageBaseURL() + "api/rcs/list");

		resource.get().send( new JsonCallback() {

			public void onSuccess(Method method, JSONValue response) {
				GWT.log( response.toString() );
				if( response.isArray() != null ) {
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						modelDataProvider.getList().add( new ModelInfo( 
								o.get( "name" ).isString().stringValue() ) );
					}
				}
			}

			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
	}

	protected void runAnalysis() {
		
		String pagename = Window.Location.getPath().substring( 0, Window.Location.getPath().lastIndexOf( "/" ) );
		
		pagename += "/report.html";
		
		UrlBuilder ub = Window.Location.createUrlBuilder();
		
		ub.setPath( pagename );
		ub.setParameter( "target", entityLabel.getText() );
		ub.setParameter( "rc", rcLabel.getText() );
		Window.Location.replace( ub.buildString() );
	}

	public native void exportJS() /*-{
		var that = this;
		$wnd.setSelectedEntity = $entry(function(amt) {
  		that.@eu.riscoss.client.riskanalysis.RiskAnalysisModule::setSelectedEntity(Ljava/lang/String;)(amt);
		});
		$wnd.setSelectedRC = $entry(function(amt) {
  		that.@eu.riscoss.client.riskanalysis.RiskAnalysisModule::setSelectedRC(Ljava/lang/String;)(amt);
		});
	}-*/;
	
	public void setSelectedEntity( String entity ) {
		entityLabel.setText( entity );
	}
	
	public void setSelectedRC( String rc ) {
		rcLabel.setText( rc );
	}
}
