package eu.riscoss.client.riskanalysis;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.ui.LinkHtml;

public class RASSelectionPanel implements IsWidget {
	
	String selectedRAS;

	DockPanel panel = new DockPanel();
	
	CellTable<String>		table;
	ListDataProvider<String>	dataProvider;

	private String selectedEntity;

	private String selectedRC;
	
	public RASSelectionPanel() {
		
		exportJS();
		
		table = new CellTable<String>();
		
		table.addColumn( new Column<String,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(String object) {
				return new LinkHtml( object, "javascript:setSelectedRAS(\"" + object + "\")" ); };
		}, "Available Risk Analysis Sessions");
		Column<String,String> c = new Column<String,String>(new ButtonCell() ) {
			@Override
			public String getValue(String object) {
				return "Delete";
			}};
			c.setFieldUpdater(new FieldUpdater<String, String>() {
				@Override
				public void update(int index, String object, String value) {
					deleteRAS( object );
				}
			});
			table.addColumn( c, "");
		
		dataProvider = new ListDataProvider<String>();
		dataProvider.addDataDisplay( table );
		
		Button button = new Button( "Create New" );
		button.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( ClickEvent event ) {
				onNewSessionRequested();
			}
		});
		
		panel.add( button, DockPanel.NORTH );
		panel.add( table, DockPanel.CENTER );
		
	}
	
	protected void deleteRAS( String ras ) {
		new Resource( GWT.getHostPageBaseURL() + "api/analysis/session/" + ras + "/delete" ).delete().send( new JsonCallbackWrapper<String>( ras ) {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				
				dataProvider.getList().remove( getValue() );
				
				if( selectedRAS != null ) {
					selectedRAS = null;
				}
				
			}
			
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}

	public native void exportJS() /*-{
	var that = this;
	$wnd.setSelectedRAS = $entry(function(amt) {
		that.@eu.riscoss.client.riskanalysis.RASSelectionPanel::setSelectedRAS(Ljava/lang/String;)(amt);
	});
	}-*/;
	
	public void setSelectedRAS( String ras ) {
		this.selectedRAS = ras;
	}
	
	protected void onNewSessionRequested() {
		new Resource( GWT.getHostPageBaseURL() + "api/analysis/session/new" )
			.addQueryParam( "target", selectedEntity )
			.addQueryParam( "rc", selectedRC )
			.post().send( new JsonCallback() {
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					dataProvider.getList().add( response.isObject().get( "id" ).isString().stringValue() );
				}} );
	}

	@Override
	public Widget asWidget() {
		return panel;
	}

	public void loadRASList( String entity, String rc ) {
		
		this.selectedEntity = entity;
		this.selectedRC = rc;
		
		dataProvider.getList().clear();
		
		new Resource( GWT.getHostPageBaseURL() + "api/analysis/session/list")
			.addQueryParam( "entity", entity )
			.addQueryParam( "rc", rc )
			.get().send( new JsonCallback() {
			public void onSuccess(Method method, JSONValue response) {
				if( response == null ) return;
				if( response.isObject() == null ) return;
				response = response.isObject().get( "list" );
				if( response.isArray() != null ) {
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						dataProvider.getList().add( new String( 
								o.get( "id" ).isString().stringValue() ) );
					}
				}
			}
			
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
		
	}

	public String getSelectedRAS() {
		return this.selectedRAS;
	}
	
}
