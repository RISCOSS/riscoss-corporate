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

package eu.riscoss.client.entities;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.JsonRDCMap;
import eu.riscoss.client.JsonRDCValueMap;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.ui.ClickWrapper;

public class RDCConfigurationPage implements IsWidget {

	DockPanel	dock = new DockPanel();
	
	Grid		grid;
	
	JsonRDCValueMap	enabledMap = null;
	JsonRDCMap		rdcMap = null;
	
	String		entity = null;
	
	Boolean 	changedData = false;
	
	public RDCConfigurationPage() {
		
		dock.setSize( "100%", "100%" );
		dock.setVisible( false );
		RootPanel.get().add( dock );
		
		HorizontalPanel hpanel = new HorizontalPanel();
		
		dock.add( hpanel, DockPanel.NORTH );
	}
	
	public Boolean changedData() {
		return changedData;
	}
	
	public void setChangedData() {
		changedData = false;
	}
	
	public void runRDCs() {
		if( entity == null ) return;
		if( "".equals( entity ) ) return;
		
		RiscossJsonClient.runRDCs( entity, new JsonCallback() {
			@Override
			public void onSuccess(Method method, JSONValue response) {
//				Window.alert( response.toString() );
			}
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
	
	}
	
	public void setSelectedEntity( String entity ) {
		this.entity = entity;
		RiscossJsonClient.listRDCs( entity, new JsonCallback() {
			@Override
			public void onSuccess(Method method, JSONValue response ) {
//				Window.alert( "" + response );
				enabledMap = new JsonRDCValueMap( response );
				RiscossJsonClient.listRDCs( new JsonCallback() {
					@Override
					public void onSuccess(Method method, JSONValue response) {
						rdcMap = new JsonRDCMap( response );
						loadRDCs( response.isObject() );
					}
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert( exception.getMessage() );
					}
				});
			}
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		} );
		
	}

	protected void loadRDCs( JSONObject rdcMap ) {
		
		if( grid != null )
			grid.removeFromParent();
//			dock.remove( grid.asWidget() );
		
		grid = new Grid();
		dock.add( grid, DockPanel.EAST );
		
		int num = 0;
		for( String key : rdcMap.keySet() ) {
			JSONObject o = rdcMap.get( key ).isObject();
			JSONArray jpn = o.get( "params" ).isArray();
			grid.resize( grid.getRowCount() + jpn.size() +1, 3 );
			CheckBox chk = new CheckBox();
			if( enabledMap.isEnabled( key ) )
				chk.setValue( true );
			chk.addClickHandler( new ClickWrapper<String>( key ){
				@Override
				public void onClick(ClickEvent event) {
					enabledMap.enableRDC( getValue(), ((CheckBox) event.getSource()).getValue() );
					changedData = true;
				}
			});
			grid.setWidget( num, 1, chk );
			Label lbl = new Label( key );
			lbl.setStyleName("rdcTitle");
			grid.setWidget( num, 0, lbl );
			
			for( int p = 0; p < jpn.size(); p++ ) {
				num++;
				JSONObject par = jpn.get( p ).isObject();
				grid.setWidget( num, 0, 
						new Label( par.get( "name" ).isString().stringValue() ) );
				TextBox txt = new TextBox();
				txt.setName( "txt:" + key + ":" + par.get( "name" ).isString().stringValue() );
				txt.getElement().setId( "txt:" + key + ":" + par.get( "name" ).isString().stringValue() );
				txt.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> event) {
						changedData = true;	
					}
				});
				if( enabledMap.isEnabled( key ) )
					txt.setText( enabledMap.get( key, par.get( "name" ).isString().stringValue(), "" ) );
				else {
					if( par.get( "def" ).isString() != null )
						txt.setText( par.get( "def" ).isString().stringValue() );
				}
				grid.setWidget( num, 1, txt );
				if( (par.get( "desc" ).isString() != null) & (par.get( "ex" ).isString() != null ) ) {
					grid.setWidget( num, 2, new HTML(
							par.get( "desc" ).isString().stringValue() +
							"<br>" +
							"Example: " + par.get( "ex" ).isString().stringValue()
							) );
				}
			}
			num++;
		}
		
		dock.setVisible( true );
		
//		RootPanel.get().add( hpanel );
//		RootPanel.get().add( grid );
		
	}

	@Override
	public Widget asWidget() {
		return dock;
	}

	//never used?!
//	public String getSelectedEntity() {
//		return this.entity;
//	}
	//never used?!	
//	public void saveRDCs( JsonCallback cb ) {
//		RiscossJsonClient.saveRDCs( getJson(), entity, cb );
//	}
	
	public JSONObject getJson() {
		for( String key : RDCConfigurationPage.this.rdcMap.keySet() ) {
			boolean enabled = enabledMap.isEnabled( key );
			enabledMap.enableRDC( key, enabled );
			if( enabled ) {
				JSONArray parameters = RDCConfigurationPage.this.rdcMap.parameters( key );
				for( int i = 0; i < parameters.size(); i++ ) {
					String parName = 
							RDCConfigurationPage.this.rdcMap.getParamterName( key, i );
					Element e = Document.get().getElementById( "txt:" + key + ":" + parName );
					if( e == null ) continue;
					String value = e.getPropertyString( "value" ) ;
					enabledMap.set( key, parName, value );
				}
			}
		}
		return enabledMap.getJson();
	}
	
}
