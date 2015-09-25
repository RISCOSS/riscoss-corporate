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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import com.google.gwt.user.client.ui.HorizontalPanel;

import eu.riscoss.client.Callback;
import eu.riscoss.client.JsonModelList;
import eu.riscoss.client.ModelInfo;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.SimpleRiskCconf;

public class ModelSelectionDialog {
		
		DialogBox dialog;
		Set<String> selection = new HashSet<String>();
		SimpleRiskCconf rc;
		String layer;
		
		Callback<List<String>> callback;
		
		//Used in WhatIfAnalysis, duplicated method, 
		//right now both are needed, needs to be adapted and leave
		//just one of them
		public void show( Callback<List<String>> cb ) {
			
			this.callback = cb;
			
			RiscossJsonClient.listModels( new JsonCallback() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert( exception.getMessage() );
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					JsonModelList list = new JsonModelList( response );
					dialog = new DialogBox( true, true ); //, new HtmlCaption( "Add model" ) );
					dialog.setText( "Model Selection" );
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
					Button ok = new Button( "Ok", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							dialog.hide();
							if( callback != null ) {
								callback.onDone( new ArrayList<String>( selection ) );
							}
						}
					});
					Button cancel = new Button ("Cancel", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							dialog.hide();
						}
					});
					ok.setStyleName("deleteButton");
					cancel.setStyleName("deleteButton");
					HorizontalPanel buttons = new HorizontalPanel();
					buttons.add(ok);
					buttons.add(cancel);
					dock.add( buttons , DockPanel.SOUTH );
					dialog.add( dock );
					dialog.getElement().getStyle().setZIndex( Integer.MAX_VALUE );
					dialog.show();
				}} );
		}
		
		//Used in RiskConf management
		public void show( String l, SimpleRiskCconf rconf,  Callback<List<String>> cb ) {
			
			this.callback = cb;
			this.rc = rconf;
			this.layer = l;
			
			RiscossJsonClient.listModels( new JsonCallback() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert( exception.getMessage() );
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					JsonModelList list = new JsonModelList( response );
					List<String> l = rc.getModelList(layer);
					dialog = new DialogBox( true, true ); //, new HtmlCaption( "Add model" ) );
					dialog.setText( "Model Selection" );
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
						if (l.contains(info.getName())) chk.setChecked(true);
						grid.setWidget( i, 0, chk );
					}
					DockPanel dock = new DockPanel();
					dock.add( grid, DockPanel.CENTER );
					Button ok = new Button( "Ok", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							dialog.hide();
							if( callback != null ) {
								callback.onDone( new ArrayList<String>( selection ) );
							}
						}
					});
					Button cancel = new Button ("Cancel", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							dialog.hide();
						}
					});
					ok.setStyleName("deleteButton");
					cancel.setStyleName("deleteButton");
					HorizontalPanel buttons = new HorizontalPanel();
					buttons.add(ok);
					buttons.add(cancel);
					dock.add( buttons , DockPanel.SOUTH );
					dialog.add( dock );
					dialog.getElement().getStyle().setZIndex( Integer.MAX_VALUE );
					dialog.show();
				}} );
		}
	}