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

import java.util.HashSet;
import java.util.Set;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;

import eu.riscoss.client.JsonModelList;
import eu.riscoss.client.ModelInfo;
import eu.riscoss.client.RiscossJsonClient;

class NewModelDialog {
		
		DialogBox dialog;
		Set<String> selection = new HashSet<String>();
		String selectedRC;
		
//		Callback<RCModelPack> callback;
		
		public void show( String rcName ) {
			
//			this.callback = cb;
			this.selectedRC = rcName;
			
			RiscossJsonClient.listModels( new JsonCallback() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert( exception.getMessage() );
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					JsonModelList list = new JsonModelList( response );
					dialog = new DialogBox( true, true ); //, new HtmlCaption( "Add model" ) );
					Grid grid = new Grid();
					grid.resize( list.getModelCount(), 1 );
					for( int i = 0; i < list.getModelCount(); i++ ) {
						ModelInfo info = list.getModelInfo( i );
						CheckBox chk = new CheckBox( info.getName() );
						chk.setName( info.getName() );
						chk.addClickHandler( new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								CheckBox chk = (CheckBox)event.getSource();
								boolean value = chk.getValue();
								if( value == true ) {
									selection.add( chk.getName() );
								}
								else {
									selection.remove( chk.getName() );
								}
							}
						});
						grid.setWidget( i, 0, chk );
					}
					DockPanel dock = new DockPanel();
					dock.add( grid, DockPanel.CENTER );
					dock.add( new Button( "Ok", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
//							SimpleRiskCconf rc = new SimpleRiskCconf( selection );
//							RiscossJsonClient.setRCContent( selectedRC, rc, new JsonCallback() {
//								
//								@Override
//								public void onSuccess(Method method, JSONValue response) {
//									dialog.hide();
////									if( callback != null ) {
////										callback.onDone( null );
////									}
//								}
//								
//								@Override
//								public void onFailure(Method method, Throwable exception) {
//									Window.alert( exception.getMessage() );
//								}
//							});
						}} ), DockPanel.SOUTH );
					dialog.add( dock );
					dialog.show();
				}} );
		}
	}