package eu.riscoss.client.entities;

import java.util.ArrayList;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

import eu.riscoss.client.EntityInfo;
import eu.riscoss.client.JsonEntitySummary;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.ui.TreeWidget;
import eu.riscoss.shared.RiscossUtil;

public class EntitiesListBox {

	VerticalPanel 	mainPanel = new VerticalPanel();
	VerticalPanel 	list = new VerticalPanel();
	
	EntitiesModule 	module = new EntitiesModule();
	
	//List-tree button
	Button			changeList;
	Boolean			enabledTree = false;
	
	//Fields used for the filtering
	HorizontalPanel filterPanel;
	TextBox			entityFilterQuery = new TextBox();
	String 			entityQueryString = "";
	ListBox 		layerList = new ListBox();
	String			filterLayer = "all";

	//CellTable of entities
	CellTable<JSONObject> 		table = new CellTable<>();
	Column<JSONObject, String> 		entityName;
	Column<JSONObject, String>		layerName;
	ListDataProvider<JSONObject> 	dataProvider;
	ArrayList<JSONObject> 			entitiesList = new ArrayList<>();
	SimplePager 				pager = new SimplePager();
	
	//Tree elements
	private TreeWidget 	root;
	protected String 	nextEntityName;
	TreeWidget			entitiesTree = new TreeWidget();

	
	public EntitiesListBox(EntitiesModule module) {
		this.module = module;
		RiscossJsonClient.listLayers(new JsonCallback() {
			@Override
			public void onSuccess(Method method, JSONValue response) {
				layerList.addItem("all"); //"all" is considered as "" in the RiscossJsonClient method
				for( int i = 0; i < response.isArray().size(); i++ ) {
					JSONObject o = (JSONObject)response.isArray().get( i );
					layerList.addItem( o.get( "name" ).isString().stringValue() );
				}
				initializeBox();
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				
			}
		});
	}
	
	public Widget getWidget() {
		return mainPanel;
	}
	
	private void initializeBox() {
		changeList = new Button("Change to tree view");
		changeList.setStyleName("button");
		changeList.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				if (enabledTree) {
					enabledTree = false;
					changeList.setText("Change to tree view");
					reloadListData();
				}
				else {
					enabledTree = true;
					changeList.setText("Change to list view");
					reloadTreeData();
				}
			}
		});
		mainPanel.add(changeList);
		filterPanel = searchFields();
		mainPanel.add(filterPanel);
		mainPanel.add(list);
		list.setWidth("100%");
		reloadListData();
	}
	
	private HorizontalPanel searchFields() {
		HorizontalPanel h = new HorizontalPanel();
		
		Label filterlabel = new Label("Search entities: ");
		filterlabel.setStyleName("bold");
		h.add(filterlabel);
		
		entityFilterQuery.setWidth("120px");
		entityFilterQuery.setStyleName("layerNameField");
		h.add(entityFilterQuery);
		
		entityFilterQuery.addKeyUpHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (entityFilterQuery.getText() != null){
					String tmp = RiscossUtil.sanitize(entityFilterQuery.getText());
					if (!tmp.equals(entityQueryString)) {
						entityQueryString = tmp;
						if (enabledTree) reloadTreeData();
						else reloadListData();
					}
				}
			}
		});
		
		Label layerLabel =  new Label("Layer: ");
		layerLabel.setStyleName("bold");
		h.add(layerLabel);
		
		h.add(layerList);
		layerList.addChangeHandler(new ChangeHandler() {
			
			@Override
			public void onChange(ChangeEvent event) {
				filterLayer = layerList.getItemText(layerList.getSelectedIndex());
				if (enabledTree) reloadTreeData();
				else reloadListData();
			}
		});
		
		return h;
	}
	
	public void reload() {
		if (enabledTree) reloadTreeData();
		else reloadListData();
	}
	
	private void reloadListData() {

		list.clear();
		
		RiscossJsonClient.searchEntities(entityQueryString, filterLayer, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				entitiesList = new ArrayList<>();
				for( int i = 0; i < response.isArray().size(); i++ ) {
					JSONObject o = (JSONObject)response.isArray().get( i );
					entitiesList.add(o);
				}
				
				table = new CellTable<JSONObject>(15, (Resources) GWT.create(TableResources.class));
				table.setWidth("100%");
				entityName = new Column<JSONObject, String>(new TextCell()) {
					@Override
					public String getValue(JSONObject arg0) {
						return arg0.get("name").isString().stringValue();
					}
				};
				layerName = new Column<JSONObject, String>(new TextCell()) {
					@Override
					public String getValue(JSONObject arg0) {
						return arg0.get("layer").isString().stringValue();
					}
				};

				final SingleSelectionModel<JSONValue> selectionModel = new SingleSelectionModel<JSONValue>();
			    table.setSelectionModel(selectionModel);
			    selectionModel.addSelectionChangeHandler(new Handler() {
					@Override
					public void onSelectionChange(SelectionChangeEvent arg0) {
						module.setSelectedEntity(selectionModel.getSelectedObject().isObject().get("name").isString().stringValue());
					}
			    });
				
				table.addColumn(entityName, "Entity");
				table.addColumn(layerName, "Layer");
				
				if (entitiesList.size() > 0) table.setRowData(0, entitiesList);
				else {
					entitiesList.add(new JSONObject());
					table.setRowData(0, entitiesList);
					entitiesList.remove(0);
				}
				table.setStyleName("table");
				
				dataProvider = new ListDataProvider<JSONObject>();
				dataProvider.addDataDisplay( table );
				
				for( int i = 0; i < entitiesList.size(); i++ ) {
					dataProvider.getList().add( entitiesList.get(i) );
				}
				
				pager = new SimplePager();
			    pager.setDisplay( table );
			    
			    list.add(table);
			    list.add(pager);
			}
		});
	}
	
	private void reloadTreeData() {
		
		list.clear();
		entitiesTree = new TreeWidget();
		if (entityQueryString.equals("") && filterLayer.equals("all")) generateTree();
		else generateListTree();
		
	}
	
	private void appendChilds(TreeWidget rootEnt, JSONArray children) {
		this.root = rootEnt;
		for (int i = 0; i < children.size(); ++i) {
			RiscossJsonClient.getEntityData(children.get(i).isString().stringValue(), new JsonCallback() {
				TreeWidget rootWidget = root;
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert(exception.getMessage());
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					JsonEntitySummary entityElement = new JsonEntitySummary(response);
					nextEntityName = entityElement.getEntityName();
					Anchor a = new Anchor(nextEntityName + " (" + entityElement.getLayer() + ")");
					a.setWidth("100%");
					a.setStyleName("font");
					a.addClickHandler(new ClickHandler() {
						String name = nextEntityName;
						@Override
						public void onClick(ClickEvent event) {
							module.setSelectedEntity(name);
						}
					});
					HorizontalPanel cPanel = new HorizontalPanel();
					cPanel.setStyleName("tree");
					cPanel.setWidth("100%");
					cPanel.add(a);
					TreeWidget c = new TreeWidget(cPanel);
					rootWidget.addChild(c);
					if (entityElement.getChildrenList().size() > 0) appendChilds(c, entityElement.getChildrenList()); 
				}
			});
		}
	}
	
	private void generateTree() {
		RiscossJsonClient.listEntities(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				for (int i = 0; i < response.isArray().size(); ++i) {
					RiscossJsonClient.getEntityData( response.isArray().get(i).isObject().get("name").isString().stringValue(), new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert( exception.getMessage() );
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							JsonEntitySummary entityElement = new JsonEntitySummary(response);
							if (entityElement.getParentList().size() == 0) {
								nextEntityName = entityElement.getEntityName();
								Anchor a = new Anchor(nextEntityName  + " (" + entityElement.getLayer() + ")");
								a.setWidth("100%");
								a.setStyleName("font");
								a.addClickHandler(new ClickHandler() {
									String name = nextEntityName;
									@Override
									public void onClick(ClickEvent event) {
										module.setSelectedEntity(name);
									}
								});
								HorizontalPanel cPanel = new HorizontalPanel();
								cPanel.setStyleName("tree");
								cPanel.setWidth("100%");
								cPanel.add(a);
								TreeWidget c = new TreeWidget(cPanel);
								entitiesTree.addChild(c);
								if (entityElement.getChildrenList().size() > 0) {
									appendChilds(c, entityElement.getChildrenList());
								}
							}
							list.add(entitiesTree);
						}} );
				}
			}
		});
	}
	
	private void generateListTree() {
		
		list.clear();
		
		RiscossJsonClient.searchEntities( entityQueryString, filterLayer, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				for (int i = 0; i < response.isArray().size(); ++i) {
					RiscossJsonClient.getEntityData( response.isArray().get(i).isObject().get("name").isString().stringValue(), new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert( exception.getMessage() );
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							JsonEntitySummary entityElement = new JsonEntitySummary(response);
							nextEntityName = entityElement.getEntityName();
							Anchor a = new Anchor(nextEntityName  + " (" + entityElement.getLayer() + ")");
							a.setWidth("100%");
							a.setStyleName("font");
							a.addClickHandler(new ClickHandler() {
								String name = nextEntityName;
								@Override
								public void onClick(ClickEvent event) {
									module.setSelectedEntity(name);
								}
							});
							HorizontalPanel cPanel = new HorizontalPanel();
							cPanel.setStyleName("tree");
							cPanel.setWidth("100%");
							cPanel.add(a);
							TreeWidget c = new TreeWidget(cPanel);
							entitiesTree.addChild(c);
							list.add(entitiesTree);
						}} );
				}
			}
		});
		
	}
	
}
