package eu.riscoss.client.ras;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.ui.LinkHtml;

public class RASModule implements EntryPoint {

	DockPanel					panel = new DockPanel();
	
	CellTable<String>			table;
	ListDataProvider<String>	dataProvider;
	
	public native void exportJS() /*-{
	var that = this;
	$wnd.setSelectedRAS = $entry(function(amt) {
		that.@eu.riscoss.client.ras.RASModule::setSelectedRAS(Ljava/lang/String;)(amt);
	});
	}-*/;
	
	@Override
	public void onModuleLoad() {
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
		
		panel.add( table, DockPanel.CENTER );
		
		RootPanel.get().add( panel );
		
		loadRASList();
	}
	
	public void loadRASList() {
		
		dataProvider.getList().clear();
		
		new Resource( GWT.getHostPageBaseURL() + "api/analysis/session/list")
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
	
	public void setSelectedRAS( String ras ) {
		
	}
	
	protected void deleteRAS( String ras ) {
		new Resource( GWT.getHostPageBaseURL() + "api/analysis/session/" + ras + "/delete" ).delete().send( new JsonCallbackWrapper<String>( ras ) {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				dataProvider.getList().remove( getValue() );
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}

}
