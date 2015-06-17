package eu.riscoss.client.entities;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.EntityInfo;
import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.layers.LayersComboBox;
import eu.riscoss.client.ui.EntityBox;
import eu.riscoss.client.ui.LinkHtml;

public class EntitiesModule implements EntryPoint {
	
	DockPanel						dock = new DockPanel();
	
	CellTable<EntityInfo>			table;
	ListDataProvider<EntityInfo>	dataProvider;
	
	SimplePanel						rightPanel = new SimplePanel();
	
	EntityPropertyPage				ppg = null;
	
	String							selectedEntity;
	
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
		
		table = new CellTable<>();
		
		table.addColumn( new Column<EntityInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(EntityInfo object) {
				return new LinkHtml( object.getName(), "javascript:selectEntity(\"" + object.getName() + "\")" ); };
		}, "Entity");
		Column<EntityInfo,String> c = new Column<EntityInfo,String>(new ButtonCell() ) {
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
		table.addColumn( c, "");
		
		dataProvider = new ListDataProvider<EntityInfo>();
		dataProvider.addDataDisplay( table );
		ppg = new EntityPropertyPage();
		
		HorizontalPanel hpanel = new HorizontalPanel();
		Anchor a = new Anchor( "Create new entity..." );
		a.addClickHandler( new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onCreateNewEntityClicked();
			}
		});
		hpanel.add( a );
//		aDelete = new Anchor( "Delete this entity..." );
//		aDelete.addClickHandler( new ClickHandler() {
//			@Override
//			public void onClick(ClickEvent event) {
//				deleteSelectedEntity();
//			}
//		});
//		aDelete.setEnabled( false );
//		hpanel.add( aDelete );
		hpanel.setWidth( "100%" );
//		hpanel.setCellHorizontalAlignment( aDelete, HorizontalPanel.ALIGN_RIGHT );
		
		dock.add( hpanel, DockPanel.NORTH );
		dock.add( table, DockPanel.CENTER );
		dock.add( rightPanel, DockPanel.EAST );
		
		
		table.setWidth( "100%" );
		dock.setSize( "100%", "100%" );
		dock.setCellWidth( table, "40%" );
		dock.setCellWidth( rightPanel, "100%" );
//		ppg.asWidget().setVisible( false );
		
		RootPanel.get().add( dock );
		
		
		String url = ( layer != null ?
				"api/entities/list/" + layer :
				"api/entities/list" );
		
		Resource resource = new Resource( GWT.getHostPageBaseURL() + url );
		
		resource.get().send( new JsonCallback() {
			
			public void onSuccess(Method method, JSONValue response) {
				if( response.isArray() != null ) {
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						insertEntityIntoTable(
								o.get( "name" ).isString().stringValue() );
					}
				}
			}
			
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	protected void setSelectedEntity( String entity ) {
		if( rightPanel.getWidget() != null ) {
			rightPanel.getWidget().removeFromParent();
		}
//		aDelete.setEnabled( true );
		this.selectedEntity = entity;
		ppg.setSelectedEntity( entity );
		if( entity != null ) {
			rightPanel.setWidget( ppg.asWidget() );
		}
	}
	
	class CreateEntityDialog {
		
		String getChosenName() {
			return txt.getText().trim();
		}
		
		String getChosenLayer() {
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
			
			layersBox = new LayersComboBox().preloaded();
			entityBox = new EntityBox();
			
			Grid grid = new Grid( 4, 2 );
			
			txt = new TextBox();
			grid.setWidget( 0, 0, new Label( "Name:" ) );
			grid.setWidget( 0, 1, txt );
			
			grid.setWidget( 1, 0, new Label( "Layer:" ) );
			grid.setWidget( 1, 1, layersBox );
			grid.setWidget( 2, 0, new Label( "Parent entity:" ) );
			
			grid.setWidget( 3, 1, new Button( "Ok", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					RiscossJsonClient.createEntity( getChosenName(), getChosenLayer(), getChosenParent(), new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert( exception.getMessage() );
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							String entity = response.isObject().get( "name" ).isString().stringValue();
							insertEntityIntoTable( entity );
							dialog.hide();
						}} );
				}
			}) );
			
			dialog.setWidget( grid );
			dialog.show();
		}
	}
	
	protected void onCreateNewEntityClicked() {
		new CreateEntityDialog().show();
	}
	
	protected void insertEntityIntoTable( String name ) {
		dataProvider.getList().add( new EntityInfo( name ) );
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
