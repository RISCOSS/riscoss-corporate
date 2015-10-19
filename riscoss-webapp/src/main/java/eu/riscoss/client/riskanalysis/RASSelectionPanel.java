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

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecRASInfo;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.ui.LinkHtml;
import eu.riscoss.shared.JRASInfo;

public class RASSelectionPanel implements IsWidget {
	
	String selectedRAS;

	DockPanel panel = new DockPanel();
	
	CellTable<JRASInfo>		table;
	ListDataProvider<JRASInfo>	dataProvider;

	private String selectedEntity;

	private String selectedRC;
	
	CodecRASInfo codec = GWT.create( CodecRASInfo.class );
	
	
	public RASSelectionPanel() {
		
		exportJS();
		
		table = new CellTable<JRASInfo>(15, (Resources) GWT.create(TableResources.class));
		
		table.addColumn( new Column<JRASInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(JRASInfo object) {
				return new LinkHtml( object.getName(), "javascript:setSelectedRAS(\"" + object.getId() + "\")" ); };
		}, "Available Risk Analysis Sessions");
		Column<JRASInfo,String> c = new Column<JRASInfo,String>(new ButtonCell() ) {
			@Override
			public String getValue(JRASInfo object) {
				return "Delete";
			}};
			c.setFieldUpdater(new FieldUpdater<JRASInfo, String>() {
				@Override
				public void update(int index, JRASInfo object, String value) {
					deleteRAS( object );
				}
			});
			table.addColumn( c, "");
		
		dataProvider = new ListDataProvider<JRASInfo>();
		dataProvider.addDataDisplay( table );
		
		Button button = new Button( "Create New" );
		button.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( ClickEvent event ) {
				onNewSessionRequested();
			}
		});
		
		SimplePager pager = new SimplePager();
	    pager.setDisplay( table );
	    
		VerticalPanel tablePanel = new VerticalPanel();
		tablePanel.add( table );
		tablePanel.add( pager );
		
		panel.add( button, DockPanel.NORTH );
		panel.add( tablePanel, DockPanel.CENTER );
		
	}
	
	protected void deleteRAS( JRASInfo info ) {
		RiscossJsonClient.deleteRiskAnalysisSession(info.getId(), new JsonCallbackWrapper<JRASInfo>( info ) {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				
				dataProvider.getList().remove( getValue() );
				
				if( selectedRAS != null ) {
					selectedRAS = null;
				}
				
			}
			
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}

	public native void exportJS() /*-{
	var that = this;
	$wnd.setSelectedRAS = $entry(function(amt) {
		that.@eu.riscoss.client.riskanalysis.RASSelectionPanel::setSelectedRAS(Ljava/lang/String;)(amt);
	});
	}-*/;
	
	public void setSelectedRAS( String ras ) {
		this.selectedRAS = ras;
	}
	
	protected void onNewSessionRequested() {
		String name = Window.prompt( "Session name (leave empty for auto-generated name)", selectedRC + " - " + selectedEntity );
		if( name == null ) return;
		name = name.trim();
		if( "".equals( name ) ) return;
		
//		new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + RiscossJsonClient.getDomain() + "/session/new" )
//			.addQueryParam( "target", selectedEntity ).addQueryParam( "rc", selectedRC ).addQueryParam( "name", name )
		RiscossJsonClient.creteRiskAnalysisSession(name, selectedRC, selectedEntity, new JsonCallback() {
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					JRASInfo info = codec.decode( response );
					dataProvider.getList().add( info );
//					dataProvider.getList().add( 
//							new RASInfo( response ) );
				}} );
	}

	@Override
	public Widget asWidget() {
		return panel;
	}

	public void loadRASList( String entity, String rc ) {
		
		this.selectedEntity = entity;
		this.selectedRC = rc;
		
		dataProvider.getList().clear();
//		new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + RiscossJsonClient.getDomain() + "/session/list")
//			.addQueryParam( "entity", entity ).addQueryParam( "rc", rc )
			RiscossJsonClient.listRiskAnalysisSessions( entity, rc, new JsonCallback() {
			public void onSuccess(Method method, JSONValue response) {
				if( response == null ) return;
				if( response.isObject() == null ) return;
				response = response.isObject().get( "list" );
				if( response.isArray() != null ) {
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JRASInfo info = codec.decode( response.isArray().get( i ) );
						dataProvider.getList().add( info );
						
//						JSONObject o = (JSONObject)response.isArray().get( i );
//						dataProvider.getList().add( 
//								new RASInfo( o ) );
					}
				}
			}
			
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
		
	}

	public String getSelectedRAS() {
		return this.selectedRAS;
	}
	
}
