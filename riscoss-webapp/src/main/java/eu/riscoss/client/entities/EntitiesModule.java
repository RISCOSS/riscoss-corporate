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

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.EntityInfo;
import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.JsonUtil;
import eu.riscoss.client.RiscossCall;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecLayerContextualInfo;
import eu.riscoss.client.layers.LayersComboBox;
import eu.riscoss.client.ui.ClickWrapper;
import eu.riscoss.client.ui.EntityBox;
import eu.riscoss.client.ui.LinkHtml;
import eu.riscoss.shared.JLayerContextualInfo;
import eu.riscoss.shared.RiscossUtil;

public class EntitiesModule implements EntryPoint {
	
	DockPanel						dock = new DockPanel();
	
	CellTable<EntityInfo>			table;
	ListDataProvider<EntityInfo>	dataProvider;
	
	VerticalPanel					rightPanel = new VerticalPanel();
	
	EntityPropertyPage				ppg = null;
	
	VerticalPanel					page = new VerticalPanel();
	HorizontalPanel					mainView = new HorizontalPanel();
	VerticalPanel					leftPanel = new VerticalPanel();
	VerticalPanel					rightPanel2 = new VerticalPanel();
	
	String							selectedEntity;
	String 							newEntity;
	
	TextBox							entityName = new TextBox();
	ListBox							layerName = new ListBox();
	Button							newEntityButton;
	
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
		
		String layer = Window.Location.getParameter( "layer" );
		
		RiscossCall.fromCookies().layers().list().get( new JsonCallback() {
			@Override
			public void onSuccess(Method method, JSONValue response) {
				for( int i = 0; i < response.isArray().size(); i++ ) {
					JSONObject o = (JSONObject)response.isArray().get( i );
					layerName.addItem( o.get( "name" ).isString().stringValue() );
				}
			}

			@Override
			public void onFailure(Method method, Throwable exception) {
				
			}
		});
		
		mainView.setStyleName("mainViewLayer");
		mainView.setWidth("100%");
		leftPanel.setStyleName("leftPanelLayer");
		leftPanel.setWidth("500px");
		//leftPanel.setHeight("100%");
		rightPanel.setStyleName("rightPanelLayer");
		//rightPanel.setWidth("60%");
		//rightPanel.setHeight("100%");
		
		Label title = new Label("Entity management");
		title.setStyleName("title");
		page.add(title);
		
		HorizontalPanel layerData = new HorizontalPanel();
		layerData.setStyleName("layerData");
		Label name = new Label("Name");
		name.setStyleName("text");
		layerData.add(name);
		entityName.setWidth("120px");
		entityName.setStyleName("layerNameField");
		layerData.add(entityName);
		Label parent = new Label("Layer");
		parent.setStyleName("text");
		layerData.add(parent);
		layerName.setStyleName("parentNameField");
		layerData.add(layerName);
		leftPanel.add(layerData);
		
		HorizontalPanel buttons = new HorizontalPanel();
		
		newEntityButton = new Button("NEW ENTITY");
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
						// TODO Auto-generated method stub
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						for(int i=0; i<response.isArray().size(); i++){
							JSONObject o = (JSONObject)response.isArray().get(i);
							if (entityName.getText().equals(o.get( "name" ).isString().stringValue())){
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
		
		table = new CellTable<EntityInfo>(15, (Resources) GWT.create(TableResources.class));
		
		table.addColumn( new Column<EntityInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(EntityInfo object) {
				return new LinkHtml( object.getName(), "javascript:selectEntity(\"" + object.getName() + "\")" ); };
		}, "Entity");
		table.addColumn( new Column<EntityInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(EntityInfo object) {
				return new LinkHtml( object.getLayer(), "" ); };
		}, "Layer");
		/*Column<EntityInfo,String> c = new Column<EntityInfo,String>(new ButtonCell() ) {
			@Override
			public String getValue(EntityInfo object) {
				return "Delete";
			}};
			c.setFieldUpdater(new FieldUpdater<EntityInfo, String>() {
				@Override
				public void update(int index, EntityInfo object, String value) {
					deleteEntity( object );
				}
			});
		c.setCellStyleNames("cellbutton");
		table.addColumn( c, "");*/
		table.setStyleName("entityTable");
		table.setWidth("100%");
		
		dataProvider = new ListDataProvider<EntityInfo>();
		dataProvider.addDataDisplay( table );
		ppg = new EntityPropertyPage();
		
		HorizontalPanel hpanel = new HorizontalPanel();
		Button a = new Button( "NEW ENTITY" );
		a.addClickHandler( new ClickWrapper<String>( layer ) {
			@Override
			public void onClick(ClickEvent event) {
				new CreateEntityDialog( getValue() ).show();
			}
		});
		a.setStyleName("button");
		
		hpanel.add( a );
		hpanel.setWidth( "100%" );
		
		SimplePager pager = new SimplePager();
	    pager.setDisplay( table );
	    
		VerticalPanel tablePanel = new VerticalPanel();
		tablePanel.setWidth("100%");
		tablePanel.add( table );
		tablePanel.add( pager );
		
		dock.add( hpanel, DockPanel.NORTH );
		dock.add( tablePanel, DockPanel.CENTER );
		dock.add( rightPanel, DockPanel.EAST );
		
		table.setWidth( "100%" );
		dock.setSize( "100%", "100%" );
		dock.setCellHeight( hpanel, "1%" );
		dock.setCellWidth( tablePanel, "40%" );
		dock.setCellWidth( rightPanel, "100%" );
		
		dock.setCellVerticalAlignment( tablePanel, DockPanel.ALIGN_TOP );
		dock.setVerticalAlignment( DockPanel.ALIGN_TOP );
		
		//RootPanel.get().add( dock );
		page.setWidth("100%");
		leftPanel.add(tablePanel);
		mainView.add(leftPanel);
		mainView.add(rightPanel);
		page.add(mainView);
		
		RootPanel.get().add( page );
		
		String url = ( layer != null ?
				"api/entities/" + RiscossJsonClient.getDomain() + "/list/" + layer :
				"api/entities/" + RiscossJsonClient.getDomain() + "/list" );
		
		Resource resource = new Resource( GWT.getHostPageBaseURL() + url );
		
		resource.get().send( new JsonCallback() {
			
			public void onSuccess(Method method, JSONValue response) {
				if( response.isArray() != null ) {
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						EntityInfo info = new EntityInfo( o.get( "name" ).isString().stringValue() );
						info.setLayer( JsonUtil.getValue( o, "layer", "" ) );
						insertEntityIntoTable( info );
					}
				}
			}
			
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	public DockPanel getDock() {
		return this.dock;
	}
	
	protected void setSelectedEntity( String entity ) {
		this.selectedEntity = entity;
		ppg.setSelectedEntity( entity );
		
		mainView.remove(rightPanel);
		rightPanel = new VerticalPanel();
		rightPanel.setWidth("80%");
		rightPanel.setStyleName("rightPanelLayer");
		
		Label l = new Label(selectedEntity);
		l.setStyleName("subtitle");
		rightPanel.add(l);
		
		HorizontalPanel buttons = new HorizontalPanel();
		Button delete = new Button("DELETE");
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
									Window.Location.reload();
								}} );
						}
					} ) ;
		delete.setStyleName("button");
		buttons.add(delete);
		rightPanel.add(buttons);
		
		rightPanel.add(ppg);
		mainView.add(rightPanel);
		
//		aDelete.setEnabled( true );
		
	}
	
	class CreateEntityDialog {
		
		String layer;

		/**
		 * Note: ensure that the layer is valid before calling this constructor!
		 * @param layer
		 */
		CreateEntityDialog( String layer ) {
			this.layer = layer;
		}
		
		String getChosenName() {
			String s = RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
			txt.setText(s);
			return s;
		}
		
		/**
		 * 
		 * @return null if no layer available
		 */
		String getChosenLayer() {
			if( layer != null ) 
				return layer;
			return layersBox.getSelectedLayer(); 
		}
		
		String getChosenParent() {
			return entityBox.getSelectedEntity();
		}
		
		DialogBox dialog = new DialogBox( true );
		
		TextBox			txt;
		LayersComboBox	layersBox;
		EntityBox		entityBox;
		
		public void show() {
			if( layer == null )
				layersBox = new LayersComboBox().preloaded();
//			layersBox.setSelectedLayer( layer );
			entityBox = new EntityBox();
			
			Grid grid = new Grid( 4, 2 );
			txt = new TextBox();
			grid.setWidget( 0, 0, new Label( "Name:" ) );
			grid.setWidget( 0, 1, txt );
			
			grid.setWidget( 1, 0, new Label( "Layer:" ) );
			if( layer == null )
				grid.setWidget( 1, 1, layersBox );
			else
				grid.setWidget( 1, 1, new Label( layer ) );
			
			grid.setWidget( 2, 0, new Label( "Parent entity:" ) );
			
			grid.setWidget( 3, 0, new Button( "Ok", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if (getChosenLayer()==null||getChosenLayer().equals("")){
						Window.alert("No Layer selected. Please create and/or select one first.");
					} else {

					RiscossJsonClient.createEntity( getChosenName(), getChosenLayer(), getChosenParent(), new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert( exception.getMessage() );
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							newEntity = response.isObject().get( "name" ).isString().stringValue();
							EntityInfo info = new EntityInfo( newEntity );
							
							
							info.setLayer( JsonUtil.getValue( response, "layer", "" ) );
							insertEntityIntoTable( info );
							
							dialog.hide();
							
							RiscossCall.fromCookies().layers().layer( layer ).property( RiscossCall.CONTEXTUALINFO ).get( new JsonCallback() {
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
				}
			}) );
			
			grid.setWidget( 3, 1, new Button( "Cancel", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					//Window.Location.reload();
					dialog.hide();
				}
			} )) ;
			
			dialog.setWidget( grid );
			dialog.show();
		}
	}
	
	protected void createEntity() {
			
			RiscossJsonClient.createEntity( entityName.getText(), layerName.getItemText(layerName.getSelectedIndex()),"", new JsonCallback() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert( exception.getMessage() );
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					newEntity = response.isObject().get( "name" ).isString().stringValue();
					EntityInfo info = new EntityInfo( newEntity );
					
					
					info.setLayer( JsonUtil.getValue( response, "layer", "" ) );
					insertEntityIntoTable( info );
					
					RiscossCall.fromCookies().layers().layer( layerName.getItemText(layerName.getSelectedIndex()) ).property( RiscossCall.CONTEXTUALINFO ).get( new JsonCallback() {
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
		RiscossJsonClient.postRiskData( array, new JsonCallback() {
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				//								Window.alert( "Ok" );
			}} );
	}
	
	protected void onCreateNewEntityClicked( String layer ) {
		new CreateEntityDialog( layer ).show();
	}
	
	protected void insertEntityIntoTable( EntityInfo info ) {
		dataProvider.getList().add( info );
	}
	
	protected void deleteEntity( EntityInfo info ) {
		
		if( info == null ) return;
		if( "".equals( info.getName() ) ) return;
		
		RiscossJsonClient.deleteEntity( info.getName(), new JsonCallbackWrapper<EntityInfo>( info ) {
			@Override
			public void onSuccess(Method method, JSONValue response) {
				
				dataProvider.getList().remove( getValue() );
				
				if( selectedEntity != null ) {
					if( selectedEntity.equals( getValue().getName() ) ) {
						setSelectedEntity( null );
						selectedEntity = null;
					}
				}
				
			}
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		} );
		
	}
	
}
