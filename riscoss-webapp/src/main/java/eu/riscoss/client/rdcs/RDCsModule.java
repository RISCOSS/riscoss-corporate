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

package eu.riscoss.client.rdcs;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

import eu.riscoss.client.RiscossJsonClient;

public class RDCsModule implements EntryPoint {
	
	DockPanel dock = new DockPanel();
	
	Grid grid = new Grid();
	
	public RDCsModule() {
		grid.resize( 0, 2 );
	}
	
	public void onModuleLoad() {
		
		dock.add( grid, DockPanel.CENTER );
		RootPanel.get().add( dock );
		
//		Resource resource = new Resource( GWT.getHostPageBaseURL() + "api/rdcs/list" );
//		resource.get().send( new JsonCallback() 
		
		RiscossJsonClient.listRDCs( new JsonCallback() {
			@Override
			public void onSuccess(Method method, JSONValue response) {
				Window.alert("XXXY"+response.toString());
				loadRDCs( response );
			}
			@Override
			public void onFailure(Method method, Throwable exception) {
				// TODO Auto-generated method stub
				
			}
		});
		
	}
	
	protected void loadRDCs( JSONValue response ) {
		
		if( response.isObject() == null ) return;
		
		int num = 0;
		for( String key : response.isObject().keySet() ) {
			JSONObject o = response.isObject().get( key ).isObject();
			JSONArray jpn = o.get( "params" ).isArray();
			grid.resize( grid.getRowCount() + jpn.size() +1, 3 );
//			grid.setWidget( num, 0, new CheckBox() );
			grid.setWidget( num, 1, new Label( key ) );
			
			for( int p = 0; p < jpn.size(); p++ ) {
				num++;
				JSONObject par = jpn.get( p ).isObject();
				grid.setWidget( num, 0, 
						new Label( par.get( "name" ).isString().stringValue() ) );
				TextBox txt = new TextBox();
				if( par.get( "def" ).isString() != null )
					txt.setText( par.get( "def" ).isString().stringValue() );
				grid.setWidget( num, 1, txt );
				if( (par.get( "desc" ).isString() != null) & (par.get( "ex" ).isString() != null ) ) {
//					txt.setTitle( 
//						par.get( "desc" ).isString().stringValue() + " "
//						+ "Example: " + par.get( "ex" ).isString().stringValue() );
					grid.setWidget( num, 2, new HTML(
							par.get( "desc" ).isString().stringValue() +
							"<br>" +
							"Example: " + par.get( "ex" ).isString().stringValue()
							) );
				}
			}
			num++;
		}
		
		RootPanel.get().add( grid );
		
	}

}
