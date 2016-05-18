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

import java.util.ArrayList;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
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
import gwtupload.client.IUploader;
import gwtupload.client.SingleUploader;
import gwtupload.client.IFileInput.FileInputType;
import gwtupload.client.IUploader.OnFinishUploaderHandler;
import gwtupload.client.IUploader.UploadedInfo;

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
	
	boolean confFileLoaded = false;
	JSiteMap sitemap;
	
	void loadSitemap() {
		RiscossCall.fromCookies().admin().fx( "sitemap" ).get( new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				CodecSiteMap codec = GWT.create( CodecSiteMap.class );
				sitemap = codec.decode( response );
				RiscossJsonClient.checkImportFiles( new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert(exception.getMessage());
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						if (response.isObject().get("confFile").isBoolean().booleanValue())
							confFileLoaded = true;
						showUI( sitemap );
					}
				});
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	void showUI( JSiteMap sitemap) {
				
		Log.println( "Loading UI for domain " + sitemap.domain );
		
		VerticalPanel vPanel = new VerticalPanel();
		
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
			if ( subsection.getLabel().equals("untracked") ) continue;
			MenuBar submenu = new MenuBar(true);
			submenu.setStyleName("subMenu");
			submenu.setAnimationEnabled(true);
			menu.addItem( subsection.getLabel(), submenu);
			for( JSitePage page : subsection.pages() ) {
				access.add(page.getLabel());
				submenu.addItem( page.getLabel(), new MenuCommand( page.getUrl() ) {
					@Override
					public void execute() {
						loadPanel( getUrl() );
					}
				});
			}
			
			if (subsection.getLabel().equals("Configure")) {
				final Button b = new Button("ye");
				final SingleUploader upload = new SingleUploader(FileInputType.CUSTOM.with(b));
				upload.setTitle("Upload new entities document");
				upload.setAutoSubmit(true);
				upload.setServletPath(upload.getServletPath() + "?t=importentities&domain=" + RiscossJsonClient.getDomain()+"&token="+RiscossCall.getToken());
				upload.addOnFinishUploadHandler(new OnFinishUploaderHandler() {
					@Override
					public void onFinish(IUploader uploader) {
						Log.println("OnFinish");
						UploadedInfo info = uploader.getServerInfo();
						String name = info.name;
						String response = uploader.getServerMessage().getMessage();
						if (confFileLoaded) {
							RiscossJsonClient.importEntities(new JsonCallback() {
								@Override
								public void onFailure(Method method, Throwable exception) {
									Window.alert(exception.getMessage());
								}
			
								@Override
								public void onSuccess(Method method, JSONValue response) {
									Window.alert("Entity information imported correctly");
									loadPanel("entities.jsp");
								}
							});
						} else {
							Window.alert("Missing config xml file. Please, contact an administrator");
						}
					}
				});
				submenu.addSeparator();
				submenu.addItem("Import entities", new MenuCommand( "Import entities" ) {
					@Override
					public void execute() {
						upload.fireEvent(new ClickEvent() {});
						b.fireEvent(new ClickEvent() {});
					}
				});
				vPanel.add(upload);
				upload.setVisible(false);
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
		
		VerticalPanel north = new VerticalPanel();
//		Image logo = new Image( "http://riscossplatform.ow2.org/riscoss/wiki/wiki1/download/ColorThemes/RISCOSS_2/logo_riscoss_DSP.png" );
		Image logo = new Image( "resources/logo_riscoss_DSP.png" );
		logo.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				loadPanel("dashboard.jsp");
			}
		});
		logo.setStyleName("logo");
		north.add( logo );
		north.setHeight("5%"); // any value here seems to resolve the firefox problem of showing only a small frame on the right side
		Label version = new Label("v1.6.0");
		version.setStyleName("version");
		north.add(version);
		//north.setWidth("100%");
		hPanel.add(north);
		
		//Comment this line if you don't need shortcuts
		generateShortcuts();
		
		hPanel.add(shortcuts);
		hPanel.setWidth("100%");
		vPanel.add(hPanel);
		vPanel.add(menu);
		vPanel.setWidth("100%");
		
		RootPanel.get().add( vPanel );
		RootPanel.get().setStyleName("root");
		loadPanel("dashboard.jsp");
		
	}
	
	
	HorizontalPanel shortcuts = new HorizontalPanel();
	ArrayList<String> access = new ArrayList<>();
	
	public void generateShortcuts() {
		shortcuts.setStyleName("float-right");
		shortcuts.setHeight("100%");
		Boolean b1 = false;
		Boolean b2 = false;
		Boolean b3 = false;
		//Entity-layer shortcut
		SimplePanel s1 = new SimplePanel();
		s1.setStyleName("shortcut-1");
		s1.setWidth("200px");
		s1.setHeight("100px");
		VerticalPanel v = new VerticalPanel();
		
		if (access.contains("Entities")) {
			b1 = true;
			Anchor entities = new Anchor ("entities to be analysed updated");
			entities.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					loadPanel("entities.jsp");
				}
			});
			HTMLPanel l1 = new HTMLPanel("Before you can run risk analysis, you need to have the <span id='entities'></span>.");
			l1.add(entities, "entities");
			v.add(l1);
		}
		
		if (access.contains("Layers")) {
			b1 = true;
			Anchor layers = new Anchor ("defining a hierarchy of layers");
			layers.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					loadPanel("layers.jsp");
				}
			});
			HTMLPanel l2 = new HTMLPanel("Entities can be organized hierarchically <span id='layers'></span>.");
			l2.add(layers, "layers");
			v.add(l2);
		}
		s1.add(v);
		
		//Riskconf-model shortcut
		SimplePanel s2 = new SimplePanel();
		s2.setWidth("200px");
		s2.setHeight("100px");
		s2.setStyleName("shortcut-2");
		VerticalPanel vv = new VerticalPanel();
		
		if (access.contains("Risk Configurations")) {
			b2 = true;
			Anchor riskconfs = new Anchor("defining risks configurations");
			riskconfs.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					loadPanel("riskconfs.jsp");
				}
			});
			HTMLPanel l3 = new HTMLPanel("The risk analysis needs to be configured <span id='riskconfs'></span>.");
			l3.add(riskconfs, "riskconfs");
			vv.add(l3);
		}
		
		if (access.contains("Models")) {
			b2 = true;
			Anchor models = new Anchor("uploaded models");
			models.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					loadPanel("models.jsp");
				}
			});
			HTMLPanel l4 = new HTMLPanel("The risk configurations use the <span id='models'></span>.");
			l4.add(models, "models");
			vv.add(l4);
		}
		s2.setWidget(vv);
		
		//analysis-browse shortcut
		SimplePanel s3 = new SimplePanel();
		s3.setWidth("200px");
		s3.setHeight("100px");
		s3.setStyleName("shortcut-3");
		VerticalPanel vvv = new VerticalPanel();
		if (access.contains("Multi-layer Analysis")) {
			b3 = true;
			Anchor riskanalysis = new Anchor("manage your risk analysis sessions");
			riskanalysis.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					loadPanel("riskanalysis.jsp");
				}
			});
			HTMLPanel l5 = new HTMLPanel("You are ready to <span id='riskanalysis'></span>");
			l5.add(riskanalysis, "riskanalysis");
			vvv.add(l5);
		}
		if (access.contains("Risk Analysis Sessions")) {
			b3 = true;
			Anchor ras = new Anchor("generate some comparisons");
			ras.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					loadPanel("ras.jsp");
				}
			});
			HTMLPanel l6 = new HTMLPanel("You can also <span id='ras'></span>.");
			l6.add(ras, "ras");
			vvv.add(l6);
		}
		s3.setWidget(vvv);
		
		if (b1) shortcuts.add(s1);
		if (b2) shortcuts.add(s2);
		if (b3) shortcuts.add(s3);
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
