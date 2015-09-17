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

import java.util.ArrayList;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.entities.EntityPropertyPage;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.ui.ClickWrapper;
import eu.riscoss.client.ui.FramePanel;
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
	HorizontalPanel 	space = new HorizontalPanel();
	
	VerticalPanel		page = new VerticalPanel();
	HorizontalPanel		mainView = new HorizontalPanel();
	VerticalPanel		leftPanel = new VerticalPanel();
	TextBox				layerName = new TextBox();
	ListBox				parentName = new ListBox();
	VerticalPanel		rightPanel = new VerticalPanel();
	Button 				newEntity = new Button();
	Button				newLayer = new Button();
	
	List<String>		entities;
	
	HorizontalPanel		mainViewEntity = new HorizontalPanel();
	VerticalPanel 		leftPanelEntity = new VerticalPanel();
	VerticalPanel		rightPanelEntity = new VerticalPanel();
	
	Grid				grid;
	TextBox				tb = new TextBox();
	ListBox				newParent;
	TextBox 			entityName;
	
	JSONValue			list;
	CellTable<String>	table = new CellTable<String>(15, (Resources) GWT.create(TableResources.class));
	
	String				nextParent;
	
	String				selectedEntity;
	String 				selectedLayer;
	String				selectedParent;
	
	TextBox				nameL;
	
	public void onModuleLoad() {
		dock.setSize( "100%", "100%" );
		parentName.addItem("[top]");
		
		mainView.setStyleName("mainViewLayer");
		mainView.setWidth("100%");
		mainViewEntity.setStyleName("mainViewLayer");
		mainViewEntity.setWidth("100%");
		leftPanel.setStyleName("leftPanelLayer");
		leftPanel.setWidth("400px");
		leftPanelEntity.setStyleName("margin-top");
		leftPanelEntity.setWidth("400px");
		rightPanel.setStyleName("rightPanelLayer");
		rightPanelEntity.setStyleName("rightPanelLayer");
		space.setWidth("80px");
		
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
								ppg = new LayerPropertyPage();
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
		name.setStyleName("bold");
		layerData.add(name);
		layerName.setWidth("120px");
		layerName.setStyleName("layerNameField");
		layerData.add(layerName);
		Label parent = new Label("Parent");
		parent.setStyleName("bold");
		layerData.add(parent);
		parentName.setStyleName("parentNameField");
		layerData.add(parentName);
		leftPanel.add(layerData);
		
		HorizontalPanel buttons = new HorizontalPanel();
		
		newLayer = new Button("New layer");
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
						
						selectedLayer = name;
						String parent = parentName.getItemText( parentName.getSelectedIndex() );
						selectedParent = parent;
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
								//Window.Location.reload();
								layerName.setText("");
								reloadPage();
							}} );
						
					}
					
				} );
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				
			}
		});
		
		buttons.add(newLayer);
		leftPanel.add(buttons);

		SimplePanel sp = new SimplePanel();
		sp.setWidget(tree);
		sp.setStyleName("layerList");
		leftPanel.add(sp);
		mainView.add(leftPanel);
		mainView.add(rightPanel);
		leftPanel.add(leftPanelEntity);
		page.add(mainView);
		//mainViewEntity.add(leftPanelEntity);
		//mainViewEntity.add(rightPanelEntity);
		//page.add(mainViewEntity);
		page.setWidth("100%");
		
		RootPanel.get().add( page );
	}
	
	private void reloadPage() {
		bottom.setUrl("entities.html?layer=" + selectedLayer);
		ppg.setParent(selectedParent);
		//layerName.setText("");
		loadRightPanel();
		
		RiscossJsonClient.listLayers( new JsonCallback() {
			
			public void onSuccess(Method method, JSONValue response) {
				if( response.isArray() != null ) {
					tree.clear();
					TreeWidget parent = tree;
					tree.asWidget().setWidth( "100%" );
					parentName = new ListBox();
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
								selectedParent = parent;
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
				}
			}
			
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
		
	}
	
	private void loadRightPanel() {
		mainView.remove(rightPanel);
		rightPanel = new VerticalPanel();
		rightPanel.setStyleName("rightPanelLayer");
		rightPanel.setWidth("90%");
		rightPanel.setHeight("auto");
		
		Label subtitle = new Label(selectedLayer);
		subtitle.setStyleName("subtitle");
		rightPanel.add(subtitle);
		mainViewEntity.remove(rightPanelEntity);
		
		grid = new Grid(5,1);
		grid.setStyleName("properties");
		grid.setWidth("100%");
		
		Grid properties = new Grid(1,5);
		
		Label name = new Label("Name");
		name.setStyleName("bold");
		properties.setWidget(0, 0, name);
		nameL = new TextBox();
		nameL.setText(selectedLayer);
		nameL.setStyleName("tag");
		nameL.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
					
					String name = nameL.getText().trim();
					
					if (name == null || name.equals("") ) 
						return;
					
					//String s = RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
					if (!RiscossUtil.sanitize(name).equals(name)){
						//info: firefox has some problem with this window, and fires assertion errors in dev mode
						Window.alert("Name contains prohibited characters (##,@,\") \nPlease re-enter name");
						return;
					}
					
					RiscossJsonClient.listLayers(new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {	
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							for(int i=0; i<response.isArray().size(); i++){
								JSONObject o = (JSONObject)response.isArray().get(i);
								if (nameL.getText().trim().equals(o.get( "name" ).isString().stringValue())){
									//info: firefox has some problem with this window, and fires assertion errors in dev mode
									Window.alert("Layer name already in use.\nPlease re-enter name.");
									return;
								}
							}
							RiscossJsonClient.editLayer(selectedLayer, nameL.getText().trim(), new JsonCallback() {
								@Override
								public void onFailure(Method method, Throwable exception) {
									Window.alert(exception.getMessage());
								}
								@Override
								public void onSuccess(Method method, JSONValue response) {
									selectedLayer = nameL.getText().trim();
									reloadPage();
								}
							});	
						}
					});
				}
			}
		});
		properties.setWidget(0, 1, nameL);
		
		properties.setWidget(0, 2, space);
		
		Label parent = new Label("Parent");
		parent.setStyleName("bold");
		properties.setWidget(0, 3, parent);
		selectedParent = ppg.getParent();
		Label parentL = new Label(selectedParent);
		parentL.setStyleName("tag");
		properties.setWidget(0, 4, parentL);
		
		grid.setWidget(0, 0, properties);
		grid.setWidget(1, 0, null);
		
		HorizontalPanel buttons = new HorizontalPanel();
		Button delete = new Button("Delete");
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
		
		grid.setWidget(2, 0, buttons);
		grid.setWidget(3, 0, null);
		
		ppg.setSelectedLayer(selectedLayer);
	
		grid.setWidget(4, 0, ppg);
		
		rightPanel.add(grid);
		
		RiscossJsonClient.listEntities(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				list = response;
				reloadEntityTable(response);
				
				leftPanelEntity.clear();
				
				HorizontalPanel layerData = new HorizontalPanel();
				layerData.setStyleName("layerData");
				Label name = new Label("Name");
				name.setStyleName("bold");
				layerData.add(name);
				entityName = new TextBox();
				entityName.setWidth("120px");
				entityName.setStyleName("layerNameField");
				layerData.add(entityName);
				leftPanelEntity.add(layerData);
				
				HorizontalPanel buttons = new HorizontalPanel();
				
				Button newEntityButton = new Button("New entity");
				newEntityButton.setStyleName("button");
				newEntityButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						String name = entityName.getText().trim();
						
						if (name == null || name.equals("") ) 
							return;
						
						//String s = RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
						if (!RiscossUtil.sanitize(name).equals(name)){
							//info: firefox has some problem with this window, and fires assertion errors in dev mode
							Window.alert("Name contains prohibited characters (##,@,\") \nPlease re-enter name");
							return;
						}

						for(int i=0; i<list.isArray().size(); i++){
							JSONObject o = (JSONObject)list.isArray().get(i);
							if (name.equals(o.get( "name" ).isString().stringValue())){
								//info: firefox has some problem with this window, and fires assertion errors in dev mode
								Window.alert("Layer name already in use.\nPlease re-enter name.");
								return;
							}
						}
						RiscossJsonClient.createEntity(name, selectedLayer, "", new JsonCallback() {
							@Override
							public void onFailure(Method method,
									Throwable exception) {
								Window.alert(exception.getMessage());
							}
							@Override
							public void onSuccess(Method method,
									JSONValue response) {
								selectedEntity = entityName.getText().trim();
								entityName.setText("");
								reloadEntityInfo();
								RiscossJsonClient.listEntities(new JsonCallback() {
									@Override
									public void onFailure(Method method,
											Throwable exception) {
									}
									@Override
									public void onSuccess(Method method,
											JSONValue response) {
										reloadEntityTable(response);
									}
								});
							}
						});
						
					}
				});
				buttons.add(newEntityButton);
				
				
				leftPanelEntity.add(buttons);
				leftPanelEntity.add(table);
				
				mainView.add(rightPanel);
				
				//table.addColumn(new Colum);
			}
		});
	}
	
	private void reloadEntityTable(JSONValue response) {
		list = response;
		entities = new ArrayList<>();
		for( int i = 0; i < response.isArray().size(); i++ ) {
			JSONObject o = (JSONObject)response.isArray().get( i );
				if (o.get("layer").isString().stringValue().equals(selectedLayer)) entities.add(o.get("name").isString().stringValue());
		}
		leftPanelEntity.clear();
		table = new CellTable<String>(15, (Resources) GWT.create(TableResources.class));
		TextColumn<String> t = new TextColumn<String>() {
			@Override
			public String getValue(String arg0) {
				return arg0;
			}
		};
		
		final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>();
	    table.setSelectionModel(selectionModel);
	    selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent arg0) {
				if (selectionModel.getSelectedObject() != "") {
					selectedEntity = selectionModel.getSelectedObject();
					reloadEntityInfo();
				}
			}
	    });
		
		table.addColumn(t, selectedLayer + " Entities");
		
		if (entities.size() > 0) table.setRowData(0, entities);
		else {
			entities.add("");
			table.setRowData(0, entities);
		}
		table.setStyleName("table");
		table.setWidth("100%");
		
		HorizontalPanel layerData = new HorizontalPanel();
		layerData.setStyleName("layerData");
		Label name = new Label("Name");
		name.setStyleName("bold");
		layerData.add(name);
		entityName = new TextBox();
		entityName.setWidth("120px");
		entityName.setStyleName("layerNameField");
		layerData.add(entityName);
		leftPanelEntity.add(layerData);
		
		HorizontalPanel buttons = new HorizontalPanel();
		
		Button newEntityButton = new Button("New entity");
		newEntityButton.setStyleName("button");
		newEntityButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				String name = entityName.getText().trim();
				
				if (name == null || name.equals("") ) 
					return;
				
				//String s = RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
				if (!RiscossUtil.sanitize(name).equals(name)){
					//info: firefox has some problem with this window, and fires assertion errors in dev mode
					Window.alert("Name contains prohibited characters (##,@,\") \nPlease re-enter name");
					return;
				}

				for(int i=0; i<list.isArray().size(); i++){
					JSONObject o = (JSONObject)list.isArray().get(i);
					if (name.equals(o.get( "name" ).isString().stringValue())){
						//info: firefox has some problem with this window, and fires assertion errors in dev mode
						Window.alert("Layer name already in use.\nPlease re-enter name.");
						return;
					}
				}
				RiscossJsonClient.createEntity(name, selectedLayer, "", new JsonCallback() {
					@Override
					public void onFailure(Method method,
							Throwable exception) {
						Window.alert(exception.getMessage());
					}
					@Override
					public void onSuccess(Method method,
							JSONValue response) {
						selectedEntity = entityName.getText().trim();
						entityName.setText("");
						reloadEntityInfo();
						RiscossJsonClient.listEntities(new JsonCallback() {
							@Override
							public void onFailure(Method method,
									Throwable exception) {
							}
							@Override
							public void onSuccess(Method method,
									JSONValue response) {
								reloadEntityTable(response);
							}
						});
					}
				});
				
			}
		});
		buttons.add(newEntityButton);
		
		
		leftPanelEntity.add(buttons);
		leftPanelEntity.add(table);
	}
	
	private void reloadEntityInfo() {
		if (selectedEntity != null) {
			mainView.remove(rightPanel);
			rightPanel = new VerticalPanel();
			rightPanel.setStyleName("rightPanelLayer");
			rightPanel.setWidth("90%");
			Label l = new Label(selectedEntity);
			l.setStyleName("subtitle");
			rightPanel.add(l);
			
			EntityPropertyPage ppgEnt = new EntityPropertyPage(null);
			ppgEnt.setSelectedEntity(selectedEntity);
			
			Grid grid = new Grid(5,1);
			grid.setStyleName("properties");
			grid.setWidth("100%");
			
			Grid properties = new Grid(1,5);
			
			Label name = new Label("Name");
			name.setStyleName("bold");
			properties.setWidget(0, 0, name);
			Label nameL = new Label(selectedEntity);
			nameL.setStyleName("tag");
			properties.setWidget(0, 1, nameL);
			
			properties.setWidget(0, 2, space);
			
			Label parent = new Label("Layer");
			parent.setStyleName("bold");
			properties.setWidget(0, 3, parent);
			Label parentL = new Label(selectedLayer);
			parentL.setStyleName("tag");
			properties.setWidget(0, 4, parentL);
			
			grid.setWidget(0, 0, properties);
			grid.setWidget(1, 0, null);
			
			HorizontalPanel buttons = new HorizontalPanel();
			Button delete = new Button("Delete");
			delete.addClickHandler(new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								RiscossJsonClient.deleteEntity( selectedEntity, new JsonCallback() {
									@Override
									public void onFailure(Method method,Throwable exception) {
										Window.alert( exception.getMessage() );
									}
									@Override
									public void onSuccess(Method method,JSONValue response) {
										mainView.remove(rightPanel);
										RiscossJsonClient.listEntities(new JsonCallback() {
											@Override
											public void onFailure(
													Method method,
													Throwable exception) {
											}
											@Override
											public void onSuccess(
													Method method,
													JSONValue response) {
												reloadEntityTable(response);
											}
										});
									}} );
							}
						} ) ;
			delete.setStyleName("button");
			buttons.add(delete);
			grid.setWidget(2, 0, buttons);
			grid.setWidget(3, 0, null);
			grid.setWidget(4, 0, ppgEnt);
			rightPanel.add(grid);
			mainView.add(rightPanel);
		}
	}
	
}
