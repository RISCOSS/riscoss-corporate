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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.riscoss.client.codec.CodecSiteMap;
import eu.riscoss.client.ui.ClickWrapper;
import eu.riscoss.client.ui.FramePanel;
import eu.riscoss.shared.CookieNames;
import eu.riscoss.shared.JSiteMap;
import eu.riscoss.shared.JSiteMap.JSitePage;
import eu.riscoss.shared.JSiteMap.JSiteSection;

public class RiscossWebApp implements EntryPoint {
	
	abstract class MenuCommand implements Command {
		private String url;
		public MenuCommand( String url ) {
			this.url = url;
		}
		public String getUrl() {
			return this.url;
		}
	}
	
	VerticalPanel	main;
	
	FramePanel 		currentPanel = null;
	SimplePanel	 	background;
	SimplePanel		margin;
	
	String			username;
	
	
	public void onModuleLoad() {
		String domain = Cookies.getCookie( CookieNames.DOMAIN_KEY );
		Log.println( "Current domain: " + domain );
		
		RiscossCall.fromCookies().withDomain(null).auth().fx( "username" ).get( new JsonCallback() {
			
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				if( response != null ) {
					if( response.isString() != null )
						username = response.isString().stringValue();
				}
				if( username == null )
					username = "";
				Log.println( "username: " + username );
				
				RiscossJsonClient.selectDomain( RiscossCall.getDomain(), new JsonCallback() {
					@Override
					public void onSuccess( Method method, JSONValue response ) {
						Log.println( "Domain check response: " + response );
						if( response == null ) showDomainSelectionDialog();
						else if( response.isString() == null ) showDomainSelectionDialog();
						else loadSitemap();
					}
					@Override
					public void onFailure( Method method, Throwable exception ) {
						Window.alert( exception.getMessage() );
					}
				} );
			}
			
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	void loadSitemap() {
		RiscossCall.fromCookies().admin().fx( "sitemap" ).get( new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				CodecSiteMap codec = GWT.create( CodecSiteMap.class );
				JSiteMap sitemap = codec.decode( response );
				showUI( sitemap );
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	HorizontalPanel shortcuts = new HorizontalPanel();
	
	void showUI( JSiteMap sitemap) {
		
		Log.println( "Loading UI for domain " + sitemap.domain );
		
		MenuBar menu = new MenuBar();
		menu.setWidth(" 100% ");
		menu.setAnimationEnabled(true);
		menu.setStyleName("mainMenu");
		
		MenuBar account = new MenuBar(true);
		account.setStyleName("subMenu");
		account.setAnimationEnabled(true);
		menu.addItem( username + " (" + sitemap.domain + ")", account);
		account.addItem("Change domain", new Command() {
			@Override
			public void execute() {
				showDomainSelectionDialog();
			}
		});
		account.addItem("Logout", new Command() {
			@Override
			public void execute() {
				Cookies.removeCookie( CookieNames.TOKEN_KEY );
				Cookies.removeCookie( CookieNames.DOMAIN_KEY );
				Window.Location.reload();
			}
		});
		
		for( JSiteSection subsection : sitemap.getRoot().subsections() ) {
			if( subsection.pages().size() < 1 ) continue;
			MenuBar submenu = new MenuBar(true);
			submenu.setStyleName("subMenu");
			submenu.setAnimationEnabled(true);
			menu.addItem( subsection.getLabel(), submenu);
			for( JSitePage page : subsection.pages() ) {
				submenu.addItem( page.getLabel(), new MenuCommand( page.getUrl() ) {
					@Override
					public void execute() {
						loadPanel( getUrl() );
					}
				});
			}
		}
		
		MenuBar helpUs = new MenuBar(true);
		helpUs.setStyleName("subMenu");
		helpUs.setAnimationEnabled(true);
		menu.addItem("Help us", helpUs);
		helpUs.addItem("User feedback", new Command() {
			@Override
			public void execute() {
				Window.open( "http://www.essi.upc.edu/~e-survey/index.php?sid=356784&lang=en", "_self", ""); 
			}
		});
		helpUs.addItem("Expert feedback", new Command() {
			@Override
			public void execute() {
				Window.open( "http://www.essi.upc.edu/~e-survey/index.php?sid=91563&lang=en", "_self", ""); 
			}
		});

		HorizontalPanel hPanel = new HorizontalPanel();
		VerticalPanel vPanel = new VerticalPanel();
		
		VerticalPanel north = new VerticalPanel();
//		Image logo = new Image( "http://riscossplatform.ow2.org/riscoss/wiki/wiki1/download/ColorThemes/RISCOSS_2/logo_riscoss_DSP.png" );
		Image logo = new Image( "resources/logo_riscoss_DSP.png" );
		north.add( logo );
		north.setHeight("5%"); // any value here seems to resolve the firefox problem of showing only a small frame on the right side
		Label version = new Label("v0.3.0");
		version.setStyleName("version");
		north.add(version);
		//north.setWidth("100%");
		hPanel.add(north);
		//generateShortcuts();
		hPanel.add(shortcuts);
		hPanel.setWidth("100%");
		vPanel.add(hPanel);
		vPanel.add(menu);
		vPanel.setWidth("100%");
		
		RootPanel.get().add( vPanel );
		RootPanel.get().setStyleName("root");
		
	}
	
	public void generateShortcuts() {
		shortcuts.setStyleName("float-right");
		SimplePanel s1 = new SimplePanel();
		s1.setStyleName("shortcut-1");
		s1.setSize("200px", "90px");
		Anchor entities = new Anchor ("entities to be analysed updated");
		entities.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loadPanel("entities.jsp");
			}
		});
		Anchor layers = new Anchor ("defining a hierarchy of layers");
		layers.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loadPanel("layers.jsp");
			}
		});
		HTMLPanel l = new HTMLPanel("Before you can run risk analysis, you need to have the <span id='entities'></span>.<br/>Entities can be organized hierarchically <span id='layers'></span>.");
		l.add(entities, "entities");
		l.add(layers, "layers");
		s1.setWidget(l);
		
		SimplePanel s2 = new SimplePanel();
		s2.setSize("200px", "90px");
		s2.setStyleName("shortcut-2");
		Anchor riskconfs = new Anchor("defining risks configurations");
		riskconfs.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loadPanel("riskconfs.jsp");
			}
		});
		Anchor models = new Anchor("uploaded models");
		models.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loadPanel("models.jsp");
			}
		});
		HTMLPanel l2 = new HTMLPanel("The risk analysis needs to be configured <span id='riskconfs'></span>.<br/>The risk configurations use the <span id='models'></span>.");
		l2.add(riskconfs, "riskconfs");
		l2.add(models, "models");
		s2.setWidget(l2);
		
		SimplePanel s3 = new SimplePanel();
		s3.setSize("200px", "90px");
		s3.setStyleName("shortcut-3");
		Anchor riskanalysis = new Anchor("manage your risk analysis sessions");
		riskanalysis.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loadPanel("riskanalysis.jsp");
			}
		});
		Anchor ras = new Anchor("generate some comparisons");
		ras.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				loadPanel("ras.jsp");
			}
		});
		HTMLPanel l3 = new HTMLPanel("You are ready to <span id='riskanalysis'></span>.<br/>You can also <span id='ras'></span>.");
		l3.add(riskanalysis, "riskanalysis");
		l3.add(ras, "ras");
		s3.setWidget(l3);
		
		shortcuts.add(s1);
		shortcuts.add(s2);
		shortcuts.add(s3);
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

			RiscossJsonClient.selectDomain(value, new JsonCallback() {
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					if( response == null ) return;
					if( response.isString() == null ) return;
					Log.println( "Domain set to " + response.isString().stringValue() );
					Cookies.setCookie( CookieNames.DOMAIN_KEY, response.isString().stringValue() );
					Window.Location.reload();
				}
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
			} );
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
		RiscossCall.fromCookies().withDomain(null).admin().fx("domains").fx( "public" ).arg( "username", username ).get( new JsonCallback() {
		
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
		} );
		
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
			RootPanel.get().remove( currentPanel.getWidget() );
			currentPanel = null;
		}
		
		currentPanel = new FramePanel( url );
		currentPanel.getWidget().setStyleName("main");
		
		if( currentPanel != null ) {
			RootPanel.get().add( currentPanel.getWidget());
			currentPanel.activate();
		}
	}
	
}
