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

package eu.riscoss.client.entities;

import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.JsonEncoderDecoder;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.riscoss.client.EntityInfo;
import eu.riscoss.client.JsonEntitySummary;
import eu.riscoss.client.JsonUtil;
import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecLayerContextualInfo;
import eu.riscoss.client.riskanalysis.JsonRiskAnalysis;
import eu.riscoss.client.riskanalysis.RASPanel;
import eu.riscoss.client.ui.TreeWidget;
import eu.riscoss.shared.JEntityNode;
import eu.riscoss.shared.JLayerContextualInfo;
import eu.riscoss.shared.RiscossUtil;

public class EntitiesModule implements EntryPoint {
	
	VerticalPanel					rightPanel = new VerticalPanel();
	
	EntityPropertyPage				ppg = null;
	
	VerticalPanel					page = new VerticalPanel();
	HorizontalPanel					mainView = new HorizontalPanel();
	VerticalPanel					leftPanel = new VerticalPanel();
	HorizontalPanel					space = new HorizontalPanel();
	
	Grid							grid;
	
	String							selectedEntity;
	String							selectedLayer;
	String 							newEntity;
	String 							nextEntityName;
		
	TextBox							entityName = new TextBox();
	TextBox							entityFilterQuery = new TextBox();
	String 							entityQueryString = "";
	ListBox 						layerList2 = new ListBox(); //used for filtering
	String							filterLayer = "";
	
	ListBox							layerList = new ListBox();
	
	Button							newEntityButton;
	Button							save;
		
	TreeWidget						entitiesTree = new TreeWidget();
	TreeWidget						root = new TreeWidget();
	
	EntitiesListBox					b;
	
	public EntitiesModule() {
	}
	
	public native void exportJS() /*-{
		var that = this;
		$wnd.selectEntity = $entry(function(amt) {
  		that.@eu.riscoss.client.entities.EntitiesModule::setSelectedEntity(Ljava/lang/String;)(amt);
		});
	}-*/;
	
	public void onModuleLoad() {
		
		exportJS();
		
		RiscossJsonClient.listLayers(new JsonCallback() {
			@Override
			public void onSuccess(Method method, JSONValue response) {
				for( int i = 0; i < response.isArray().size(); i++ ) {
					JSONObject o = (JSONObject)response.isArray().get( i );
					layerList.addItem( o.get( "name" ).isString().stringValue() );
					layerList2.addItem( o.get( "name" ).isString().stringValue() );
				}
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				
			}
		});
		
		space.setWidth("80px");
		mainView.setStyleName("mainViewLayer");
		mainView.setWidth("100%");
		leftPanel.setStyleName("leftPanelLayer");
		leftPanel.setWidth("500px");
		rightPanel.setStyleName("rightPanelLayer");
		
		Label title = new Label("Entity management");
		title.setStyleName("title");
		page.add(title);
		
		HorizontalPanel layerData = new HorizontalPanel();
		layerData.setStyleName("layerData");
		Label name = new Label("Name");
		name.setStyleName("bold");
		layerData.add(name);
		entityName.setWidth("120px");
		entityName.setStyleName("layerNameField");
		layerData.add(entityName);
		Label parent = new Label("Layer");
		parent.setStyleName("bold");
		layerData.add(parent);
		layerList.setStyleName("parentNameField");
		layerData.add(layerList);
		leftPanel.add(layerData);
		
		HorizontalPanel buttons = new HorizontalPanel();
		
		newEntityButton = new Button("New entity");
		newEntityButton.setStyleName("button");
		newEntityButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				if (entityName.getText() == null || entityName.getText().equals("") ) 
					return;
				//String s = RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
				if (!RiscossUtil.sanitize(entityName.getText()).equals(entityName.getText())){
					//info: firefox has some problem with this window, and fires assertion errors in dev mode
					Window.alert("Name contains prohibited characters (##,@,\") \nPlease re-enter name");
					return;
				}
				RiscossJsonClient.listEntities(new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert(exception.getMessage());
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						for(int i=0; i<response.isArray().size(); i++){
							JSONObject o = (JSONObject)response.isArray().get(i);
							if (entityName.getText().equals(o.get( "name" ).isString().stringValue())) {
								//info: firefox has some problem with this window, and fires assertion errors in dev mode
								Window.alert("Entity name already in use.\nPlease re-enter name.");
								return;
							}
						}
						createEntity();
					}
				});
			}
		});
		buttons.add(newEntityButton);
		
		leftPanel.add(buttons);

		ppg = new EntityPropertyPage(this);
		
		page.setWidth("100%");
		//leftPanel.add(tablePanel);
		entitiesTree.asWidget().setWidth("100%");
		
		
		b = new EntitiesListBox(this);
		leftPanel.add(b.getWidget());
		mainView.add(leftPanel);
		mainView.add(rightPanel);
		page.add(mainView);
		
		save = new Button("Save");
		save.setStyleName("button");
		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveEntityData();	
			}
		});
		
		RootPanel.get().add( page );

	}
	
	public void saveEntityData() {
		ppg.saveEntityData(entityNameBox.getText(), entityLayer.getValue(entityLayer.getSelectedIndex()));
	}
	
	public interface CodecEntityNodeList extends JsonEncoderDecoder<JEntityNode> {}
	
	ListBox entityLayer;
	
	public void setSelectedEntity( String entity ) {
		this.selectedEntity = entity;
		ppg = new EntityPropertyPage(this);
		ppg.setSelectedEntity( entity );
		
		RiscossJsonClient.getEntityData( entity, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				JsonEntitySummary info = new JsonEntitySummary( response );
				selectedLayer = info.getLayer();
				RiscossJsonClient.listLayers(new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						// TODO Auto-generated method stub
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						entityLayer = new ListBox();
						for(int i=0; i<response.isArray().size(); i++){
							JSONObject o = (JSONObject)response.isArray().get(i);
							entityLayer.insertItem(o.get("name").isString().stringValue(), i);
							if (o.get("name").isString().stringValue().equals(selectedLayer)) 
								entityLayer.setSelectedIndex(i);
						}
						loadProperties();
					}
				});
			}} );
		
	}
	
	TextBox entityNameBox;
	
	private void loadProperties() {
		mainView.remove(rightPanel);
		rightPanel = new VerticalPanel();
		rightPanel.setWidth("80%");
		rightPanel.setStyleName("rightPanelLayer");
		
		Label title = new Label(selectedEntity);
		title.setStyleName("subtitle");
		rightPanel.add(title);
		
		grid = new Grid(1,5);
		grid.setStyleName("properties");
		
		Label nameL = new Label("Name");
		nameL.setStyleName("bold");
		grid.setWidget(0,0,nameL);
		
		entityNameBox = new TextBox();
		entityNameBox.setText(selectedEntity);
		entityNameBox.setEnabled(false);
		grid.setWidget(0, 1, entityNameBox);
		
		grid.setWidget(0, 2, space);
		
		Label parentN = new Label("Layer");
		parentN.setStyleName("bold");
		grid.setWidget(0, 3, parentN);
		
		Label parentNv = new Label(selectedLayer);
		parentNv.setStyleName("tag");
		grid.setWidget(0, 4, entityLayer);
		
		rightPanel.add(grid);
		
		HorizontalPanel buttons = new HorizontalPanel();
		Button delete = new Button("Delete");
		delete.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							hasRiskSessions();
						}
					} ) ;
		delete.setStyleName("button");
		Button runDataCollectors = new Button ("Run data collectors");
		runDataCollectors.setStyleName("button");
		runDataCollectors.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				ppg.runDC();
			}
		});
		buttons.add(save);
		buttons.add(runDataCollectors);
		buttons.add(delete);
		rightPanel.add(buttons);
		
		
		ppg = new EntityPropertyPage(this);
		ppg.setSelectedEntity(selectedEntity);
		rightPanel.add(ppg);
		mainView.add(rightPanel);
	}

	public void reloadData() {
		//entitiesTree.clear();
		b.reload();
		//generateTree(entityQueryString, filterLayer);
	}
	
	protected void createEntity() {
			
			RiscossJsonClient.createEntity( entityName.getText(), layerList.getItemText(layerList.getSelectedIndex()),"", new JsonCallback() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert( exception.getMessage() );
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					entityName.setText("");
					newEntity = response.isObject().get( "name" ).isString().stringValue();
					
					EntityInfo info = new EntityInfo( newEntity );
					
					info.setLayer( JsonUtil.getValue( response, "layer", "" ) );
					
					reloadData();

					RiscossJsonClient.getLayerContextualInfo(layerList.getItemText(layerList.getSelectedIndex()), new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert( exception.getMessage() );
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
							JLayerContextualInfo jLayerContextualInfo = codec.decode( response );
							updateContextualInfo(jLayerContextualInfo);
						}
					});
					
				}} );
		}
	
	protected void updateContextualInfo( JLayerContextualInfo contextualInfo ) {
		int k;
		JSONArray array = new JSONArray();
		if (contextualInfo != null) {
			for (k = 0; k < contextualInfo.getSize(); k++) {
				JSONObject o = new JSONObject();
				o.put( "id", new JSONString( contextualInfo.getContextualInfoElement(k).getId() ) );
				o.put( "target", new JSONString( newEntity ) );
				String value = contextualInfo.getContextualInfoElement(k).getDefval();
				for (int i = 0; i < contextualInfo.getContextualInfoElement(k).getInfo().size(); ++i) {
					value+=";"+contextualInfo.getContextualInfoElement(k).getInfo().get(i);
				}
				o.put( "value", new JSONString( value ) );
				o.put( "type", new JSONString( "custom" ) );
				o.put( "datatype", new JSONString( contextualInfo.getContextualInfoElement(k).getType()));
				o.put( "origin", new JSONString( "user" ) );
				array.set( k, o );
			}
		}
		RiscossJsonClient.postRiskData( array, new JsonCallback() {
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				setSelectedEntity(newEntity);
			}} );
	}
	
	public void back() {
		page.clear();
		page.setStyleName("");
		Label title = new Label("Entity management");
		title.setStyleName("title");
		page.add(title);
		page.add(mainView);
		upload();
	}
	
	public void deleteRiskSes(String ras) {
		RiscossJsonClient.deleteRiskAnalysisSession(ras, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				back();
				upload();
			}
		});
	}
	
	private void upload() {
		ppg = new EntityPropertyPage(this);
		ppg.setSelectedEntity(selectedEntity);
		ppg.setSelectedTab(4);
		loadProperties();
	}
	
	RASPanel rasPanelResult;
	
	public void setSelectedRiskSes(String risk, EntityPropertyPage p) {
		rasPanelResult = new RASPanel(null);
		rasPanelResult.setEppg(p, true);
		rasPanelResult.loadRAS(risk);
		
		RiscossJsonClient.getSessionSummary(risk, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				page.clear();
				page.setStyleName("leftPanelLayer");
				JsonRiskAnalysis json = new JsonRiskAnalysis( response );
				/*Label title = new Label(json.getName());
				title.setStyleName("subtitle");
				page.add(title);*/
				page.add(rasPanelResult);
			}
		});

	}
	
	Boolean hasRisk;
	
	protected void hasRiskSessions() {
		hasRisk = false;
		RiscossJsonClient.listRiskAnalysisSessions(selectedEntity, "", new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());	
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				if (response.isObject().get("list").isArray().size() > 0) hasRisk = true;
				deleteEntity();
			}
		});
	}

	protected void deleteEntity() {
		if (hasRisk) Window.alert("Entities with associated risk sessions cannot be deleted");
		else {
			Boolean b = Window.confirm("Are you sure that you want to delete entity " + selectedEntity + "?");
			if (b) {
				RiscossJsonClient.deleteEntity( selectedEntity, new JsonCallback() {
					@Override
					public void onFailure(Method method,Throwable exception) {
						Window.alert( exception.getMessage() );
					}
					@Override
					public void onSuccess(Method method,JSONValue response) {
						mainView.remove(rightPanel);
						reloadData();
					}
				} );
			}
		}
	}
	
}