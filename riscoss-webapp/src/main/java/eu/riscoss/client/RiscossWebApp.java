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

package eu.riscoss.client;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.riscoss.client.ui.ClickWrapper;
import eu.riscoss.client.ui.FramePanel;
import eu.riscoss.client.ui.TreeWidget;
import eu.riscoss.shared.CookieNames;

public class RiscossWebApp implements EntryPoint {
	
	DockPanel dock;
	
	FramePanel currentPanel = null;
	
	public void onModuleLoad() {
		String domain = Cookies.getCookie( CookieNames.DOMAIN_KEY );
		Log.println( "Current domain: " + domain );
		new Resource( GWT.getHostPageBaseURL() + "api/domains/selected" )
		.addQueryParam( "domain", domain )
		.post().send( new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				Log.println( "Domain check response: " + response );
				if( response == null ) showDomainSelectionDialog();
				else if( response.isString() == null ) showDomainSelectionDialog();
				else showUI( response.isString().stringValue() );
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	void showUI() {
		showUI( "" );
	}
	
	void showUI( String domain ) {
		
		Log.println( "Loading UI for domain " + domain );
		
		TreeWidget root = new TreeWidget();
		
		TreeWidget item;
		
		item = root.addChild( new TreeWidget( new Label( "Select" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Domain (" + domain + ")", new ClickHandler() {
			@Override
			public void onClick( ClickEvent event ) {
				showDomainSelectionDialog();
			}} ) ) );
		
		item = root.addChild( new TreeWidget( new Label( "Configure" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Layers", "layers.html" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Entities", "entities.html" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Models", "models.html" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Risk Configurations", "riskconfs.html" ) ) );
		
		item = root.addChild( new TreeWidget( new Label( "Run" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "One-layer Analysis", "analysis.html" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Multi-layer Analysis", "riskanalysis.html" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "What-If Analysis", "whatifanalysis.html" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "AHP Session", "rma.html" ) ) );
		
		item = root.addChild( new TreeWidget( new Label( "Browse" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Risk Data Repository", "rdr.html" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Risk Analysis Sessions", "ras.html" ) ) );
		
//		item = root.addChild( new TreeWidget( new Label( "Admin" ) ) );
//		item.addChild( new TreeWidget( new OutlineLabel( "Users and Roles", "admin.html" ) ) );
		
		VerticalPanel left = new VerticalPanel();
		left.add( new Image( "logo3.png" ) );
		left.setHeight("20%"); // any value here seems to resolve the firefox problem of showing only a small frame on the right side
		left.add( root );
		
		dock = new DockPanel();
		dock.setWidth( "100%" );
		dock.add( left, DockPanel.WEST );
		dock.setCellWidth( left, "222px" );
		dock.setHeight( "90%" ); // <- not 100% to allow the "recompile" icon of iframes to appear
		
		RootPanel.get().add( dock );
		
	}
	
	static class DomainSelectionDialog {
		DialogBox dialog = new DialogBox( true, false );
		VerticalPanel panel = new VerticalPanel();
		
		DomainSelectionDialog() {
		}
		
		private void addDomainOption( String name ) {
			Anchor a = new Anchor( name );
			a.addClickHandler( new ClickWrapper<String>( name ) {
				@Override
				public void onClick( ClickEvent event ) {
					selectDomain( getValue() );
				}} );
			panel.add( a );
		}
		
		protected void selectDomain( String value ) {
			dialog.hide();
			new Resource( GWT.getHostPageBaseURL() + "api/domains/selected" )
				.addQueryParam( "domain", value )
					.post().send( new JsonCallback() {
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					if( response == null ) return;
					if( response.isString() == null ) return;
//					if( !"Ok".equals( response.isString().stringValue() ) ) return;
					Log.println( "Domain set to " + response.isString().stringValue() );
					Cookies.setCookie( CookieNames.DOMAIN_KEY, response.isString().stringValue() );
					Window.Location.reload();
				}
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
			});
		}

		public void show() {
			HorizontalPanel h = new HorizontalPanel();
			h.add( new Button( "Cancel", new ClickHandler() {
				@Override
				public void onClick( ClickEvent event ) {
					dialog.hide();
				}} ) );
			panel.add( h );
			dialog.setWidget( panel );
			dialog.setText( "Select Domain" );
			dialog.setPopupPositionAndShow( new PositionCallback() {
				@Override
				public void setPosition( int offsetWidth, int offsetHeight ) {
					dialog.setPopupPosition( offsetWidth /2, offsetHeight /2 );
				}
			} );
		}
		
	}
	
	
	protected void showDomainSelectionDialog() {
		new Resource( GWT.getHostPageBaseURL() + "api/domains/predefined/list" ).get().send( new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				DomainSelectionDialog dsDialog = null;
				if( dsDialog == null ) {
					dsDialog = new DomainSelectionDialog();
				}
				for( int i = 0; i < response.isArray().size(); i++ ) {
					dsDialog.addDomainOption( response.isArray().get( i ).isString().stringValue() );
				}
				dsDialog.show();
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}

	class OutlineLabel extends Anchor {
		String panelUrl;
		public OutlineLabel( String label, String panelName, ClickHandler h ) {
			super( label );
			this.panelUrl = panelName;
			if( h != null ) {
				addClickHandler( h );
			}
			else {
				addClickHandler( new ClickHandler() {
					public void onClick(ClickEvent event) {
						loadPanel( OutlineLabel.this.panelUrl );
					}
				});
			}
		}
		public OutlineLabel( String label, String panelName ) {
			this( label, panelName, null );
		}
		public OutlineLabel(String label, ClickHandler clickHandler) {
			this( label, null, clickHandler );
		}
	}
	
	protected void loadPanel( String url ) {
		
		if( currentPanel != null ) {
			dock.remove( currentPanel.getWidget() );
			currentPanel = null;
		}
		
		currentPanel = new FramePanel( url );
		
		if( currentPanel != null ) {
			dock.add( currentPanel.getWidget(), DockPanel.CENTER );
//			currentPanel.getWidget().getParent().setHeight( "100%" );
			currentPanel.activate();
		}
	}

}
