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
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.EntityInfo;
import eu.riscoss.client.ModelInfo;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.ui.LinkHtml;

public class RiskAnalysisModule implements EntryPoint {

	CellTable<EntityInfo> entityTable;
	CellTable<ModelInfo> modelTable;

	ListDataProvider<EntityInfo> entityDataProvider;
	ListDataProvider<ModelInfo> modelDataProvider;
	
	VerticalPanel		page = new VerticalPanel();
	HorizontalPanel		mainView = new HorizontalPanel();
	VerticalPanel		leftPanel = new VerticalPanel();
	VerticalPanel		rightPanel = new VerticalPanel();

	Label entityLabel = new Label("-");
	Label rcLabel = new Label("-");
	
	EntityInfo selection = null;
	
	public void onModuleLoad() {

		exportJS();

		entityTable = new CellTable<EntityInfo>(15, (Resources) GWT.create(TableResources.class));
		entityTable.setStyleName("leftPanel");
		modelTable = new CellTable<ModelInfo>(15, (Resources) GWT.create(TableResources.class));
		modelTable.setStyleName("rightPanel");
		
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
		Button btn = new Button( "RUN ANALYSIS", new ClickHandler() {
			public void onClick(ClickEvent event) {
				runAnalysis();
			}
		});
		btn.setStyleName("button");
		Label selEnt = new Label("Selected entity:");
		selEnt.setStyleName("tag");
		grid.setWidget( 0, 0, selEnt );
		Label selRc = new Label("Selected risk configuration:");
		selRc.setStyleName("tag");
		grid.setWidget( 1, 0, selRc );
		entityLabel.setStyleName("tagSelected");
		grid.setWidget( 0, 1, entityLabel );
		rcLabel.setStyleName("tagSelected");
		grid.setWidget( 1, 1, rcLabel );
		grid.setWidget( 2, 1, btn );
		grid.setStyleName("gridRisk");
		
		mainView.setStyleName("mainViewLayer");
		mainView.setWidth("100%");
		page.setWidth("100%");
		
		Label title = new Label("One-layer Analysis");
		title.setStyleName("title");
		page.add(title);
		
		entityTable.setWidth("100%");
		mainView.add(entityTable);
		modelTable.setWidth("50%");
		mainView.add(modelTable);
		page.add(mainView);
		
		page.add(grid);
		
		RootPanel.get().add(page);
		
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
