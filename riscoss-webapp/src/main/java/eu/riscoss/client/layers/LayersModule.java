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
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.riscoss.client.EntityInfo;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.ui.ClickWrapper;
import eu.riscoss.client.ui.FramePanel;
import eu.riscoss.client.ui.LabeledWidget;
import eu.riscoss.client.ui.LinkHtml;
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
	
	VerticalPanel		page = new VerticalPanel();
	HorizontalPanel		mainView = new HorizontalPanel();
	VerticalPanel		leftPanel = new VerticalPanel();
	TextBox				layerName = new TextBox();
	ListBox				parentName = new ListBox();
	VerticalPanel		rightPanel = new VerticalPanel();
	Button 				newEntity = new Button();
	Button				newLayer = new Button();
	
	String				nextParent;
	
	String 				selectedLayer;
	
	public void onModuleLoad() {
		
		dock.setSize( "100%", "100%" );
		//bottom.getWidget().setSize( "100%", "100%" );
		parentName.addItem("[top]");
		
		mainView.setStyleName("mainViewLayer");
		mainView.setWidth("100%");
		leftPanel.setStyleName("leftPanelLayer");
		leftPanel.setWidth("400px");
		//leftPanel.setHeight("100%");
		rightPanel.setStyleName("rightPanelLayer");
		//rightPanel.setWidth("60%");
		//rightPanel.setHeight("100%");
		
		RiscossJsonClient.listLayers( new JsonCallback() {
			
			public void onSuccess(Method method, JSONValue response) {
				if( response.isArray() != null ) {
					TreeWidget parent = tree;
					tree.asWidget().setWidth( "100%" );
					for (int i = 0; i < response.isArray().size(); i++) {
						JSONObject o = (JSONObject) response.isArray().get(i);
						parentName.addItem(o.get("name").isString().stringValue());
						if (i > 0) 
							nextParent = ((JSONObject) response.isArray().get(i - 1)).get("name").isString().stringValue();
						else 
							nextParent = "[top]";
						HorizontalPanel p = new HorizontalPanel();
						p.setStyleName("layerListElement");

						Anchor anchor = new Anchor(o.get("name").isString().stringValue());
						anchor.setStyleName("layerListElement");
						anchor.addClickHandler(new ClickWrapper<String>(o.get("name").isString().stringValue()) {
							String parent = nextParent;

							@Override
							public void onClick(ClickEvent event) {
								bottom.setUrl("entities.html?layer=" + getValue());
								ppg.setParent(parent);
								//ppg.setSelectedLayer(getValue());
								selectedLayer = getValue();
								newEntity.setEnabled(true);
								loadRightPanel();
							}
						});

						p.add( anchor );
						
						p.setWidth( "100%" );
						
						TreeWidget lw = new TreeWidget( p );
						parent.addChild( lw );
						parent = lw;
					}
//					Anchor anchor = new Anchor( "[none]" );
//					anchor.addClickHandler( new ClickHandler() {
//						@Override
//						public void onClick(ClickEvent event) {
//							bottom.setUrl( "entities.html?layer=-" );
//						}
//					});
//					TreeWidget lw = new TreeWidget( anchor );
//					parent.addChild( lw );
				}
			}
			
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
		
		Label title = new Label("Layer management");
		title.setStyleName("title");
		page.add(title);
		
		HorizontalPanel layerData = new HorizontalPanel();
		layerData.setStyleName("layerData");
		Label name = new Label("Name");
		name.setStyleName("text");
		layerData.add(name);
		layerName.setWidth("120px");
		layerName.setStyleName("layerNameField");
		layerData.add(layerName);
		Label parent = new Label("Parent");
		parent.setStyleName("text");
		layerData.add(parent);
		parentName.setStyleName("parentNameField");
		layerData.add(parentName);
		leftPanel.add(layerData);
		
		HorizontalPanel buttons = new HorizontalPanel();
		
		newLayer = new Button("NEW LAYER");
		newLayer.setStyleName("button");
		RiscossJsonClient.listLayers( new JsonCallback() {
			@Override
			public void onSuccess(Method method, JSONValue response) {
				newLayer.addClickHandler( new ClickWrapper<JSONArray>( (JSONArray)response.isArray() ) {
					@Override
					public void onClick(ClickEvent event) {
						String name = layerName.getText().trim();
						
						if (name == null || name.equals("") ) 
							return;
						
						//String s = RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
						if (!RiscossUtil.sanitize(name).equals(name)){
							//info: firefox has some problem with this window, and fires assertion errors in dev mode
							Window.alert("Name contains prohibited characters (##,@,\") \nPlease re-enter name");
							return;
						}
						
						for(int i=0; i<getValue().size(); i++){
							JSONObject o = (JSONObject)getValue().get(i);
							if (name.equals(o.get( "name" ).isString().stringValue())){
								//info: firefox has some problem with this window, and fires assertion errors in dev mode
								Window.alert("Layer name already in use.\nPlease re-enter name.");
								return;
							}
						}
						
						String parent = parentName.getItemText( parentName.getSelectedIndex() );
//						Window.alert( "'" + parent + "': " + "[top]".equals( parent ) );
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
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				
			}
		});
		
		
		newEntity = new Button("NEW ENTITY");
		newEntity.setStyleName("button");
		newEntity.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				//TODO
			}
			
		});
		newEntity.setEnabled(false);
		buttons.add(newLayer);
		//buttons.add(newEntity);
		leftPanel.add(buttons);
		
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
	    hpanel.add( tree );
	    hpanel.add( ppg );
		dock.add( tree, DockPanel.CENTER );
		dock.add( ppg, DockPanel.EAST );
		dock.setCellWidth( ppg, "100%" );
		
		dock.setCellWidth( tree, "50%" );
		dock.setCellHeight( tree, "50%" );
		dock.setCellHeight( bottom.getWidget(), "50%" );
		
		hpanel.add(dock);
		
		SimplePanel sp = new SimplePanel();
		sp.setWidget(tree);
		sp.setStyleName("layerList");
		leftPanel.add(sp);
		mainView.add(leftPanel);
		mainView.add(rightPanel);
		page.add(mainView);
		page.setWidth("100%");
		
		//RootPanel.get().add(dock);
		RootPanel.get().add( page );
		//RootPanel.get().setHeight( "100%" );
			
	}
	
	private void loadRightPanel() {
		mainView.remove(rightPanel);
		rightPanel = new VerticalPanel();
		rightPanel.setStyleName("rightPanelLayer");
		rightPanel.setWidth("90%");
		rightPanel.setHeight("auto");
		//rightPanel.setHeight("100%");
		
		Label l = new Label(selectedLayer);
		l.setStyleName("subtitle");
		rightPanel.add(l);
		
		HorizontalPanel buttons = new HorizontalPanel();
		Button delete = new Button("DELETE");
		delete.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							Window.alert("Attention: entities associated to this layer cannot be re-associated to another layer and need to be deleted manually!");
							RiscossJsonClient.deleteLayer( selectedLayer, new JsonCallback() {
								@Override
								public void onFailure(Method method,Throwable exception) {
									Window.alert( exception.getMessage() );
								}
								@Override
								public void onSuccess(Method method,JSONValue response) {
									Window.Location.reload();
								}} );
						}
					} ) ;
		delete.setStyleName("button");
		buttons.add(delete);
		Button save = new Button("SAVE");
		save.setStyleName("button");
		//buttons.add(save);
		rightPanel.add(buttons);
		ppg.setSelectedLayer(selectedLayer);
		save.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				
			}
			
		});
		rightPanel.add(ppg);
		SimplePanel sp = new SimplePanel();
		sp.setHeight("350px");
		sp.setWidget(bottom.getWidget());
		sp.setStyleName("entityProperties");
		rightPanel.add(sp);
		
		mainView.add(rightPanel);
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
							Window.alert("Attention: entities associated to this layer cannot be re-associated to another layer and need to be deleted manually!");
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
					popup.setSize( "400px", "150px" );
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
							
							if (name == null || name.equals("") ) 
								return;
							
							//String s = RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
							if (!RiscossUtil.sanitize(name).equals(name)){
								//info: firefox has some problem with this window, and fires assertion errors in dev mode
								Window.alert("Name contains prohibited characters (##,@,\") \nPlease re-enter name");
								return;
							}
							
							for(int i=0; i<getValue().size(); i++){
								JSONObject o = (JSONObject)getValue().get(i);
								if (name.equals(o.get( "name" ).isString().stringValue())){
									//info: firefox has some problem with this window, and fires assertion errors in dev mode
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
					popup.setSize( "400px", "150px" );
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
								//info: firefox has some problem with this window, firing assertion errors in dev mode
								Window.alert("Name contains prohibited characters (##,@,\") \nPlease re-enter name");
								return;
							}

							for(int i=0; i<getValue().size(); i++){
								JSONObject o = (JSONObject)getValue().get(i);
								if (newName.equals(o.get( "name" ).isString().stringValue())){
									//info: firefox has some problem with this window, firing assertion errors in dev mode
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
					popup.setSize( "400px", "150px" );
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
