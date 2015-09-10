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

import java.util.ArrayList;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.EntityInfo;
import eu.riscoss.client.JsonUtil;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.ui.HtmlString;
import eu.riscoss.client.ui.LinkHtml;

class EntitySelectionPanel implements IsWidget {
	
	public interface Listener {
		
		void onEntitySelected( String entity );
		
	}
	
	CellTable<EntityInfo>			table;
	ListDataProvider<EntityInfo>	entityDataProvider;
	ArrayList<Listener>				listeners = new ArrayList<Listener>();
	
	String							selectedEntity = "";
	Label 							entityLabel = new Label("-");
	
	VerticalPanel					tablePanel = new VerticalPanel();
	
	public EntitySelectionPanel() {
		
		exportJS();
		
		table = new CellTable<EntityInfo>(15, (Resources) GWT.create(TableResources.class));
		
		table.addColumn( new Column<EntityInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(EntityInfo object) {
				return new LinkHtml( object.getName(), "javascript:setSelectedEntity(\"" + object.getName() + "\")" ); };
		}, "Entities");
		table.addColumn( new Column<EntityInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(EntityInfo object) {
				return new HtmlString( "" + object.getLayer() ); };
		}, "Layer");
		
		entityDataProvider = new ListDataProvider<EntityInfo>();
		entityDataProvider.addDataDisplay(table);
		
		SimplePager pager = new SimplePager();
	    pager.setDisplay( table );
	    
		tablePanel.add( table );
		tablePanel.add( pager );
		
	}
	
	public void setSelectedEntity( String entity ) {
		this.selectedEntity = entity;
		entityLabel.setText(entity);
		for( Listener l : listeners ) {
			l.onEntitySelected( entity );
		}
		
	}
	
	public native void exportJS() /*-{
	var that = this;
	$wnd.setSelectedEntity = $entry(function(amt) {
		that.@eu.riscoss.client.riskanalysis.EntitySelectionPanel::setSelectedEntity(Ljava/lang/String;)(amt);
	});
	}-*/;
	
	void loadEntities() {
		//Resource resource = new Resource( GWT.getHostPageBaseURL() + "api/entities/" + RiscossJsonClient.getDomain() + "/list");
		RiscossJsonClient.listEntities( new JsonCallback() {
			
			public void onSuccess(Method method, JSONValue response) {
				if( response.isArray() != null ) {
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						EntityInfo info = new EntityInfo( o.get( "name" ).isString().stringValue() );
						info.setLayer( JsonUtil.getValue( o, "layer", "" ) );
						entityDataProvider.getList().add( info );
					}
				}
				
				// TODO notify container
				//					loadRiskConfs();
			}
			
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return tablePanel;
	}
	
	public void addSelectionListener( Listener listener ) {
		this.listeners.add( listener );
	}

	public String getSelectedEntity() {
		return this.selectedEntity;
	}
}