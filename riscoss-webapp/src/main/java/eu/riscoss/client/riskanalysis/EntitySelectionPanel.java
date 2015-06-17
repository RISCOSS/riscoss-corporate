package eu.riscoss.client.riskanalysis;

import java.util.ArrayList;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.EntityInfo;
import eu.riscoss.client.ui.LinkHtml;

class EntitySelectionPanel implements IsWidget {
	
	public interface Listener {
		
		void onEntitySelected( String entity );
		
	}
	
	CellTable<EntityInfo>			entityTable;
	ListDataProvider<EntityInfo>	entityDataProvider;
	ArrayList<Listener>				listeners = new ArrayList<Listener>();
	
	String							selectedEntity = "";
	
	public EntitySelectionPanel() {
		
		exportJS();
		
		entityTable = new CellTable<EntityInfo>();
		
		entityTable.addColumn( new Column<EntityInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(EntityInfo object) {
				return new LinkHtml( object.getName(), "javascript:setSelectedEntity(\"" + object.getName() + "\")" ); };
		}, "Entities");
		
		entityDataProvider = new ListDataProvider<EntityInfo>();
		entityDataProvider.addDataDisplay(entityTable);
	}
	
	public void setSelectedEntity( String entity ) {
		this.selectedEntity = entity;
		for( Listener l : listeners ) {
			l.onEntitySelected( entity );
		}
		
	}
	
	public native void exportJS() /*-{
	var that = this;
	$wnd.setSelectedEntity = $entry(function(amt) {
		that.@eu.riscoss.client.riskanalysis.EntitySelectionPanel::setSelectedEntity(Ljava/lang/String;)(amt);
	});
	}-*/;
	
	void loadEntities() {
		Resource resource = new Resource( GWT.getHostPageBaseURL() + "api/entities/list");
		
		resource.get().send( new JsonCallback() {
			
			public void onSuccess(Method method, JSONValue response) {
				if( response.isArray() != null ) {
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						entityDataProvider.getList().add( new EntityInfo( 
								o.get( "name" ).isString().stringValue() ) );
					}
				}
				
				// TODO notify container
				//					loadRiskConfs();
			}
			
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	@Override
	public Widget asWidget() {
		return entityTable;
	}
	
	public void addSelectionListener( Listener listener ) {
		this.listeners.add( listener );
	}

	public String getSelectedEntity() {
		return this.selectedEntity;
	}
}