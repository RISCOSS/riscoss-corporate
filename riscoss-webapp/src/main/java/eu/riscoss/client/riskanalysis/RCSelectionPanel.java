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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.ModelInfo;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.ui.LinkHtml;

class RCSelectionPanel implements IsWidget {
	
	CellTable<ModelInfo>		table;
	ListDataProvider<ModelInfo>	modelDataProvider;
	String						selectedRC;
	
	VerticalPanel tablePanel = new VerticalPanel();
	
	public RCSelectionPanel() {
		
		exportJS();
		
		table = new CellTable<ModelInfo>(15, (Resources) GWT.create(TableResources.class));
		
		table.addColumn( new Column<ModelInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(ModelInfo object) {
				return new LinkHtml( object.getName(), "javascript:setSelectedRC(\"" + object.getName() + "\")" ); };
		}, "Available Risk Configurations");
		
		modelDataProvider = new ListDataProvider<ModelInfo>();
		modelDataProvider.addDataDisplay(table);
		
		SimplePager pager = new SimplePager();
	    pager.setDisplay( table );
	    
		tablePanel.add( table );
		tablePanel.add( pager );
		
	}
	
	public native void exportJS() /*-{
	var that = this;
	$wnd.setSelectedRC = $entry(function(amt) {
		that.@eu.riscoss.client.riskanalysis.RCSelectionPanel::setSelectedRC(Ljava/lang/String;)(amt);
	});
	}-*/;
	
	public void setSelectedRC( String rc ) {
		this.selectedRC = rc;
	}
	
	void loadRCs( String entity ) {
		
		modelDataProvider.getList().clear();
		
		//Resource resource = new Resource( GWT.getHostPageBaseURL() + "api/rcs/" + RiscossJsonClient.getDomain() + "/list");
		RiscossJsonClient.listRCs(entity, new JsonCallback() {
			
			public void onSuccess(Method method, JSONValue response) {
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

	@Override
	public Widget asWidget() {
		return tablePanel;
	}

	public String getSelectedRC() {
		return this.selectedRC;
	}
}