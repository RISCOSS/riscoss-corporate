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

package eu.riscoss.client.layers;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.ui.ClickWrapper;
import eu.riscoss.client.ui.FramePanel;
import eu.riscoss.client.ui.LabeledWidget;
import eu.riscoss.client.ui.TreeWidget;
import eu.riscoss.shared.RiscossUtil;

public class LayersModule implements EntryPoint {
	
	static class LayerInfo {
		public LayerInfo( String id, String name ) {
			this.id = id;
			this.name = name;
		}
		String id;
		String name;
	}
	
	DockPanel			dock = new DockPanel();
	HorizontalPanel		hpanel = new HorizontalPanel();
	TreeWidget			tree = new TreeWidget();
	FramePanel			bottom = new FramePanel( "" );
	LayerPropertyPage	ppg = new LayerPropertyPage();
	
	public void onModuleLoad() {
		
		dock.setSize( "100%", "100%" );
		bottom.getWidget().setSize( "100%", "100%" );
		
		RiscossJsonClient.listLayers( new JsonCallback() {
			
			public void onSuccess(Method method, JSONValue response) {
				if( response.isArray() != null ) {
					TreeWidget parent = tree;
					tree.asWidget().setWidth( "100%" );
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						
						HorizontalPanel p = new HorizontalPanel();
						
						Anchor anchor = new Anchor(
								o.get( "name" ).isString().stringValue() );
						anchor.addClickHandler( new ClickWrapper<String>( o.get( "name" ).isString().stringValue() ) {
							
							@Override
							public void onClick(ClickEvent event) {
								bottom.setUrl( "entities.html?layer=" + getValue() );
							}
						});
						
						p.add( anchor );
						
						p.setWidth( "100%" );
						
						TreeWidget lw = new TreeWidget( p );
						parent.addChild( lw );
						parent = lw;
					}
					Anchor anchor = new Anchor( "[none]" );
					anchor.addClickHandler( new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							bottom.setUrl( "entities.html?layer=-" );
						}
					});
					TreeWidget lw = new TreeWidget( anchor );
					parent.addChild( lw );
				}
			}
			
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
		
		MenuBar menu = new MenuBar(true);
	    menu.addItem( "New Layer", new Command() {
			@Override
			public void execute() {
				onNewLayer();
			}});
	    menu.addItem( "Rename Layer", new Command() {
			@Override
			public void execute() {
				new RenameLayerPanel().show();
				//String s = RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
				//txt.setText(s);
				//TODO rename
				
			}});
	    menu.addItem( "Delete Layer", new Command() {
			@Override
			public void execute() {
				new DeleteLayerPanel().show();
			}});
//	    fooMenu.addItem("foo", cmd);
//	    fooMenu.addItem("menu", cmd);
	    
	    MenuBar menuBar = new MenuBar();
	    menuBar.addItem( "Edit structure", menu );
	    
	    dock.add( menuBar, DockPanel.NORTH );
	    dock.add( bottom.getWidget(), DockPanel.SOUTH );
//	    SimplePanel sp = new SimplePanel();
//	    sp.setWidget( tree );
//	    hpanel.add( tree );
//	    hpanel.add( ppg );
		dock.add( tree, DockPanel.CENTER );
//		dock.add( ppg, DockPanel.EAST );
//		dock.setCellWidth( ppg, "50%" );
		
		dock.setCellWidth( tree, "100%" );
		dock.setCellHeight( tree, "50%" );
		dock.setCellHeight( bottom.getWidget(), "50%" );
		
		RootPanel.get().add( dock );
		RootPanel.get().setHeight( "100%" );
		
	}
	
	class DeleteLayerPanel {
		ListBox combo = new ListBox();
		void show() {
			RiscossJsonClient.listLayers( new JsonCallback() {
				@Override
				public void onSuccess(Method method, JSONValue response) {
					VerticalPanel panel = new VerticalPanel();
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						combo.addItem( o.get( "name" ).isString().stringValue() );
					}
					panel.add( new LabeledWidget( "Layer to delete:", combo ) );
					panel.add( new Button( "Ok", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							RiscossJsonClient.deleteLayer( combo.getItemText( combo.getSelectedIndex() ), new JsonCallback() {
								@Override
								public void onFailure(Method method,Throwable exception) {
									Window.alert( exception.getMessage() );
								}
								@Override
								public void onSuccess(Method method,JSONValue response) {
									Window.Location.reload();
								}} );
						}
					} ) );
					panel.add( new Button( "Cancel", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Window.Location.reload();
						}
					} )) ;
					PopupPanel popup = new PopupPanel( true, true );
					popup.setWidget( panel );
					popup.setSize( "400px", "300px" );
					popup.show();
				}
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert( exception.getMessage() );
				}
			});
		}
	}
	
	class NewLayerPanel {
		TextBox txt = new TextBox();
		ListBox combo = new ListBox();
		void show() {
			RiscossJsonClient.listLayers( new JsonCallback() {
				@Override
				public void onSuccess(Method method, JSONValue response) {
					VerticalPanel panel = new VerticalPanel();
					combo.addItem( "[top]" );
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						combo.addItem( o.get( "name" ).isString().stringValue() );
					}
					combo.setSelectedIndex( combo.getItemCount() -1 );
					panel.add( new LabeledWidget( "Parent layer:", combo ) );
					txt.getElement().setId( "layers:new:name:textbox" );
					panel.add( new LabeledWidget( "Name:", txt ) );
					
					Button b = new Button( "Ok");
					b.addClickHandler( new ClickWrapper<JSONArray>( (JSONArray)response.isArray() ) {
						@Override
						public void onClick(ClickEvent event) {
							
							String name = txt.getText().trim();
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
							
							String parent = combo.getItemText( combo.getSelectedIndex() );
//							Window.alert( "'" + parent + "': " + "[top]".equals( parent ) );
							if( "[top]".equals( parent ) ) {
								parent = "$root";
							}
							
							RiscossJsonClient.createLayer( name, parent, 
									new JsonCallback() {
								@Override
								public void onFailure(Method method, Throwable exception) {
									Window.alert( exception.getMessage() );
								}

								@Override
								public void onSuccess(Method method, JSONValue response) {
									Window.Location.reload();
								}} );
						}
						
					} );
					
					panel.add(b);
					
					panel.add( new Button( "Cancel", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Window.Location.reload();
						}
					} )) ;
					
					PopupPanel popup = new PopupPanel( true, true );
					popup.setWidget( panel );
					popup.setSize( "400px", "300px" );
					popup.show();
				}
				
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert( exception.getMessage() );
				}
			});
		}
	}
	
	protected void onNewLayer() {
		new NewLayerPanel().show();
	}
	
	class RenameLayerPanel {
		ListBox combo = new ListBox();
		TextBox txt = new TextBox();
		void show() {
			RiscossJsonClient.listLayers( new JsonCallback() {
				@Override
				public void onSuccess(Method method, JSONValue response) {
					VerticalPanel panel = new VerticalPanel();
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						combo.addItem( o.get( "name" ).isString().stringValue() );
					}
					panel.add( new LabeledWidget( "Layer to rename:", combo ) );
					//txt.getElement().setId( "layers:edit:name:textbox" );
					
					panel.add( new LabeledWidget( "New name:", txt ) );
					
					Button b = new Button( "Ok");
					b.addClickHandler( new ClickWrapper<JSONArray>( (JSONArray)response.isArray() ) {
						
						@Override
						public void onClick(ClickEvent event) {
							//RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
							String newName = txt.getText().trim();
							if (newName == null || newName.equals("") ) 
								return;

							if (!RiscossUtil.sanitize(newName).equals(newName)){
								//info: firefox has some problem with this window, firing asssertion errors in dev mode
								Window.alert("Name contains prohibited characters (##,@,\") \nPlease re-enter name");
								return;
							}

							for(int i=0; i<getValue().size(); i++){
								JSONObject o = (JSONObject)getValue().get(i);
								if (newName.equals(o.get( "name" ).isString().stringValue())){
									//info: firefox has some problem with this window, firing asssertion errors in dev mode
									Window.alert("Layer name already in use.\nPlease re-enter name.");
									return;
								}
							}

							txt.setText(newName);
							RiscossJsonClient.editLayer( combo.getItemText( combo.getSelectedIndex() ), newName, new JsonCallback() {
								@Override
								public void onFailure(Method method,Throwable exception) {
									Window.alert( exception.getMessage() );
								}
								@Override
								public void onSuccess(Method method,JSONValue response) {
									Window.Location.reload();
								}} );
						}
					} );
					
					panel.add(b);
					
					panel.add( new Button( "Cancel", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Window.Location.reload();
						}
					} )) ;
					PopupPanel popup = new PopupPanel( true, true );
					popup.setWidget( panel );
					popup.setSize( "400px", "300px" );
					popup.show();
				}
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert( exception.getMessage() );
				}
			});
		}
	}
	
}
