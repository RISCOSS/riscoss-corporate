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

package eu.riscoss.client.riskconfs;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.SimpleRiskCconf;
import eu.riscoss.client.ui.LinkHtml;
import eu.riscoss.shared.RiscossUtil;

public class RiskConfsModule implements EntryPoint {
	
	static class RCInfo {
		public RCInfo( String name ) {
			this.name = name;
		}
		String name;
	}
	
	DockPanel dock = new DockPanel();
	
	CellTable<RCInfo>			table;
	ListDataProvider<RCInfo>	dataProvider;
	
	SimplePanel					rightPanel;
	
	JSONObject					currentRC = null;
	String						selectedRC = null;
	
	public RiskConfsModule() {
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.client.ActivablePanel#getWidget()
	 */
	public Widget getWidget() {
		return dock;
	}
	
	private <C> Column<RCInfo, C> addColumn(Cell<C> cell,final GetValue<C> getter, FieldUpdater<RCInfo, C> fieldUpdater) {
		Column<RCInfo, C> column = new Column<RCInfo, C>(cell) {
			@Override
			public C getValue(RCInfo object) {
				return getter.getValue(object);
			}
		};
		column.setFieldUpdater(fieldUpdater);
		
		return column;
	}
	
	private static interface GetValue<C> {
		C getValue(RCInfo contact);
	}
	
	public native void exportJS() /*-{
	var that = this;
	$wnd.selectRC = $entry(function(amt) {
  	that.@eu.riscoss.client.riskconfs.RiskConfsModule::onRCSelected(Ljava/lang/String;)(amt);
	});
}-*/;
	
	/* (non-Javadoc)
	 * @see eu.riscoss.client.ActivablePanel#activate()
	 */
	public void onModuleLoad() {
		
		exportJS();
		
		table = new CellTable<RCInfo>();
		
		table.addColumn( new Column<RCInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(RCInfo object) {
				return new LinkHtml( object.name, "javascript:selectRC(\"" + object.name + "\")" ); };
		}, "Risk Configuration");
		Column<RCInfo,String> c = new Column<RCInfo,String>(new ButtonCell() ) {
			@Override
			public String getValue(RCInfo object) {
				return "Delete";
			}};c.setFieldUpdater(new FieldUpdater<RCInfo, String>() {
				@Override
				public void update(int index, RCInfo object, String value) {
					deleteRC( object );
				}
			});
			table.addColumn( c, "");
			
			dataProvider = new ListDataProvider<RCInfo>();
			dataProvider.addDataDisplay(table);
			
			RiscossJsonClient.listRCs( new JsonCallback() {
				
				public void onSuccess(Method method, JSONValue response) {
					GWT.log( response.toString() );
					if( response.isArray() != null ) {
						for( int i = 0; i < response.isArray().size(); i++ ) {
							JSONObject o = (JSONObject)response.isArray().get( i );
							dataProvider.getList().add( new RCInfo( 
									o.get( "name" ).isString().stringValue() ) );
						}
					}
				}
				
				public void onFailure(Method method, Throwable exception) {
					Window.alert( exception.getMessage() );
				}
			} );
			
			rightPanel = new SimplePanel();
			
			dock.add( new Button("Add", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					onAddNew();
				}
			}), DockPanel.NORTH );
			
			SimplePager pager = new SimplePager();
		    pager.setDisplay( table );
		    
			VerticalPanel tablePanel = new VerticalPanel();
			tablePanel.add( table );
			tablePanel.add( pager );
			
			dock.setSize( "100%", "100%" );
			dock.add( tablePanel, DockPanel.CENTER );
			dock.add( rightPanel, DockPanel.EAST );
			dock.setCellWidth( tablePanel, "40%" );
			dock.setCellHeight( rightPanel, "100%" );
			rightPanel.setSize( "100%", "100%" );
			table.setWidth( "100%" );
			
			RootPanel.get().add( dock );
	}
	
	protected void deleteRC(RCInfo object) {
		
		RiscossJsonClient.deleteRC( object.name, new JsonCallbackWrapper<RCInfo>( object ) {
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}

			@Override
			public void onSuccess( Method method, JSONValue response ) {
				onRCSelected( null );
				dataProvider.getList().remove( getValue() );
			}} );
	}
	
	protected void onAddNew() {
		String name = Window.prompt( "Name:", "" );
		if( name == null || "".equals( name ) ) 
			return;
		
		name = RiscossUtil.sanitize(name.trim());//attention:name sanitation is not directly notified to the user
		
		RiscossJsonClient.createRC( name, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
			
			@Override
			public void onSuccess(Method method, JSONValue response) {
				dataProvider.getList().add( new RCInfo( 
						response.isObject().get( "name" ).isString().stringValue() ) );
			}} );
	}
	
	public void onRCSelected( String item ) {
		selectedRC = item;
		rightPanel.clear();
		if( item == null ) return;
		if( "".equals( item ) ) return;
		RiscossJsonClient.getRCContent( item, new JsonCallback() {
			@Override
			public void onSuccess(Method method, JSONValue response) {
				if( rightPanel.getWidget() != null ) {
					rightPanel.getWidget().removeFromParent();
				}
				
				RCPropertyPage ppg = new RCPropertyPage( new SimpleRiskCconf( response ) );
				
				rightPanel.setWidget( ppg.asWidget() );

			}
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
}
