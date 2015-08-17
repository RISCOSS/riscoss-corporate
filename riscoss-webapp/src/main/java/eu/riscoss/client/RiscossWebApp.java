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
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.riscoss.client.ui.ClickWrapper;
import eu.riscoss.client.ui.FramePanel;
import eu.riscoss.client.ui.TreeWidget;
import eu.riscoss.shared.CookieNames;

public class RiscossWebApp implements EntryPoint {
	
	VerticalPanel	main;
	
	FramePanel 		currentPanel = null;
	SimplePanel	 	background;
	SimplePanel		margin;
	
	
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
		
		MenuBar menu = new MenuBar();
		menu.setWidth(" 100% ");
		menu.setAnimationEnabled(true);
		menu.setStyleName("mainMenu");
		
		menu.addItem("Select Domain (" + domain + ")", new Command() {
			@Override
			public void execute() {
				showDomainSelectionDialog();
			}
		});
		
		MenuBar configure = new MenuBar(true);
		configure.setStyleName("subMenu");
		configure.setAnimationEnabled(true);
		menu.addItem("Configure", configure);
		configure.addItem("Layers", new Command() {
			@Override
			public void execute() {
				loadPanel( "layers.html" );
			}
		});
		configure.addItem("Entities", new Command() {
			@Override
			public void execute() {
				loadPanel( "entities.html" );
			}
		});
		configure.addItem("Models", new Command() {
			@Override
			public void execute() {
				loadPanel( "models.html" );
			}
		});
		configure.addItem("Risk Configurations", new Command() {
			@Override
			public void execute() {
				loadPanel( "riskconfs.html" );
			}
		});
		
		MenuBar run = new MenuBar(true);
		run.setStyleName("subMenu");
		run.setAnimationEnabled(true);
		menu.addItem("Run", run);
		run.addItem("One-layer Analysis", new Command() {
			@Override
			public void execute() {
				loadPanel( "analysis.html" );
			}
		});
		run.addItem("Multi-layer Analysis", new Command() {
			@Override
			public void execute() {
				loadPanel( "riskanalysis.html" );
			}
		});
		run.addItem("What-If Analysis", new Command() {
			@Override
			public void execute() {
				loadPanel( "whatifanalysis.html" );
			}
		});
		run.addItem("AHP Session", new Command() {
			@Override
			public void execute() {
				loadPanel( "rma.html" );
			}
		});
		
		MenuBar browse = new MenuBar(true);
		browse.setStyleName("subMenu");
		browse.setAnimationEnabled(true);
		menu.addItem("Browse", browse);
		browse.addItem("Risk Data Repository", new Command() {
			@Override
			public void execute() {
				loadPanel( "rdr.html" );
			}
		});
		browse.addItem("Risk Analysis Sessions", new Command() {
			@Override
			public void execute() {
				loadPanel( "ras.html" );
			}
		});
		
		/*MenuBar admin = new MenuBar(true);
		 * admin.setAnimationEnabled(true);
		 * menu.addItem("Admin", admin);
		 * admin.addItem("Users and Roles", new Command() {
		 * 	@Override
		 * 	public void execute() {
		 * 		loadPanel( "admin.html" );
		 * 	}
		 *});
		 */
		
		
		VerticalPanel north = new VerticalPanel();
		north.add( new Image( "logo3.png" ) );
		north.setHeight("5%"); // any value here seems to resolve the firefox problem of showing only a small frame on the right side
		Label version = new Label("v1.5.0");
		version.setStyleName("version");
		north.add(version);
		north.add( menu );
		north.setWidth("100%");
		
		background = new SimplePanel();
		background.setWidth("95%");
		background.setHeight("95%");
		background.setStyleName("background");
		
		margin = new SimplePanel();
		margin.setWidth("100%");
		margin.setHeight("100%");
		margin.setStyleName("margin");
		margin.setWidget(background);
		
		RootPanel.get().add( north );
		RootPanel.get().add( margin );
		RootPanel.get().setStyleName("root");

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
			background.remove( currentPanel.getWidget() );
			currentPanel = null;
		}
		
		currentPanel = new FramePanel( url );
		currentPanel.getWidget().setHeight("100%");
		
		if( currentPanel != null ) {
			background.setWidget( currentPanel.getWidget());
//			currentPanel.getWidget().getParent().setHeight( "100%" );
			currentPanel.activate();
		}
	}

}
