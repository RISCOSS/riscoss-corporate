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
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.EntityInfo;
import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.JsonUtil;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.SimpleRiskCconf;
import eu.riscoss.client.ui.ClickWrapper;
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
	
	SimplePanel					rightPanel2;
	
	VerticalPanel		page = new VerticalPanel();
	HorizontalPanel		mainView = new HorizontalPanel();
	VerticalPanel		leftPanel = new VerticalPanel();
	VerticalPanel		rightPanel = new VerticalPanel();
	TextBox				riskConfName = new TextBox();
	
	RCPropertyPage 		ppg;
	
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
			
			rightPanel2 = new SimplePanel();
			
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
			dock.add( rightPanel2, DockPanel.EAST );
			dock.setCellWidth( tablePanel, "40%" );
			dock.setCellHeight( rightPanel2, "100%" );
			rightPanel2.setSize( "100%", "100%" );
			table.setWidth( "100%" );
			
			mainView.setStyleName("mainViewLayer");
			mainView.setWidth("100%");
			leftPanel.setStyleName("leftPanelLayer");
			leftPanel.setWidth("400px");
			//leftPanel.setHeight("100%");
			rightPanel.setStyleName("rightPanelLayer");
			
			Label title = new Label("Risk configuration management");
			title.setStyleName("title");
			page.add(title);
			
			HorizontalPanel data = new HorizontalPanel();
			data.setStyleName("layerData");
			Label name = new Label("Name");
			name.setStyleName("text");
			data.add(name);
			riskConfName.setWidth("120px");
			riskConfName.setStyleName("layerNameField");
			data.add(riskConfName);
			leftPanel.add(data);
			
			Button newRisk = new Button("NEW CONFIGURATION");
			newRisk.setStyleName("button");
			newRisk.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent arg0) {
					String name = riskConfName.getText();
					while (!RiscossUtil.sanitize(name).equals(name)){
						name = Window.prompt( "Name contains prohibited characters (##,@,\") \nRe-enter name:", "" );
						if( name == null || "".equals( name ) ) 
							return;
					}
					
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
			});
			leftPanel.add(newRisk);
			
			leftPanel.add(tablePanel);
			
			mainView.add(leftPanel);
			mainView.add(rightPanel);
			page.add(mainView);
			page.setWidth("100%");
			
			//RootPanel.get().add( dock );
			RootPanel.get().add( page );
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
		
		
		/**Button bOk = new Button( "Ok" ); //, new ClickHandler() {

		bOk.addClickHandler( new ClickWrapper<JSONArray>( (JSONArray)response.isArray() ) {
			@Override
			public void onClick(ClickEvent event) {
				String name = txt.getText().trim();

				if (name == null || name.equals("") ) 
					return;

				//String s = RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
				if (!RiscossUtil.sanitize(name).equals(name)){
					//info: firefox has some problem with this window, and fires asssertion errors in dev mode
					Window.alert("Name contains prohibited characters (##,@,\") \nPlease re-enter name");
					return;
				}

				for(int i=0; i<getValue().size(); i++){
					JSONObject o = (JSONObject)getValue().get(i);
					if (name.equals(o.get( "name" ).isString().stringValue())){
						//info: firefox has some problem with this window, and fires asssertion errors in dev mode
						Window.alert("Layer name already in use.\nPlease re-enter name.");
						return;
					}
				}

				RiscossJsonClient.createEntity( getChosenName(), getChosenLayer(), getChosenParent(), new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert( exception.getMessage() );
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						String entity = response.isObject().get( "name" ).isString().stringValue();
						EntityInfo info = new EntityInfo( entity );
						info.setLayer( JsonUtil.getValue( response, "layer", "" ) );
						insertEntityIntoTable( info );
						dialog.hide();
					}
				} );
			}
		});
		grid.setWidget( 3, 1, bOk);	
		
		grid.setWidget( 3, 2, new Button( "Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//Window.Location.reload();
				dialog.hide();
			}
		} )) ; */
		
		
		
		
		while (!RiscossUtil.sanitize(name).equals(name)){
			name = Window.prompt( "Name contains prohibited characters (##,@,\") \nRe-enter name:", "" );
			if( name == null || "".equals( name ) ) 
				return;
		}
		
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
		rightPanel2.clear();
		if( item == null ) return;
		if( "".equals( item ) ) return;
		
		RiscossJsonClient.getRCContent( item, new JsonCallback() {
			@Override
			public void onSuccess(Method method, JSONValue response) {
				/*if( rightPanel2.getWidget() != null ) {
					rightPanel2.getWidget().removeFromParent();
				}*/
				
				ppg = new RCPropertyPage( new SimpleRiskCconf( response ) );

			}
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
		
		selectedRC = item;
		mainView.remove(rightPanel);
		rightPanel = new VerticalPanel();
		rightPanel.setStyleName("rightPanelLayer");
		rightPanel.setWidth("90%");
		
		Label riskName = new Label(selectedRC);
		riskName.setStyleName("subtitle");
		rightPanel.add(riskName);
		
		HorizontalPanel buttons = new HorizontalPanel();
		Button delete = new Button("DELETE");
		delete.setStyleName("button");
		delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				RiscossJsonClient.deleteRC( selectedRC, new JsonCallback() {
					@Override
					public void onFailure( Method method, Throwable exception ) {
						Window.alert( exception.getMessage() );
					}

					@Override
					public void onSuccess( Method method, JSONValue response ) {
						onRCSelected( null );
						Window.Location.reload();
					}} );	
			}
		});
		buttons.add(delete);
		
		Button save = new Button("SAVE");
		save.setStyleName("button");
		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				String newName = ppg.getName();
				
				//TODO: rename risk configuration
			}
		});
		buttons.add(save);
		rightPanel.add(buttons);
		
		rightPanel.add( ppg.asWidget() );
		
		mainView.add(rightPanel);
	}
	
}
