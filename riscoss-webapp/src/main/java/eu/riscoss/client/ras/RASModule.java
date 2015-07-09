package eu.riscoss.client.ras;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.codec.CodecRASInfo;
import eu.riscoss.client.ui.LinkHtml;
import eu.riscoss.shared.JRASInfo;

public class RASModule implements EntryPoint {

	DockPanel					panel = new DockPanel();
	
	CellTable<JRASInfo>			table;
	ListDataProvider<JRASInfo>	dataProvider;
	
	public native void exportJS() /*-{
	var that = this;
	$wnd.setSelectedRAS = $entry(function(amt) {
		that.@eu.riscoss.client.ras.RASModule::setSelectedRAS(Ljava/lang/String;)(amt);
	});
	}-*/;
	
	@Override
	public void onModuleLoad() {
		exportJS();
		
		table = new CellTable<JRASInfo>();
		
		table.addColumn( new Column<JRASInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(JRASInfo object) {
				return new LinkHtml( object.getName(), "javascript:setSelectedRAS(\"" + object.getId() + "\")" ); };
		}, "Available Risk Analysis Sessions");
		Column<JRASInfo,String> c = new Column<JRASInfo,String>(new ButtonCell() ) {
			@Override
			public String getValue(JRASInfo object) {
				return "Delete";
			}};
			c.setFieldUpdater(new FieldUpdater<JRASInfo, String>() {
				@Override
				public void update(int index, JRASInfo object, String value) {
					deleteRAS( object );
				}
			});
			table.addColumn( c, "");
		
		dataProvider = new ListDataProvider<JRASInfo>();
		dataProvider.addDataDisplay( table );
		
		SimplePager pager = new SimplePager();
	    pager.setDisplay( table );
	    
		VerticalPanel tablePanel = new VerticalPanel();
		tablePanel.add( table );
		tablePanel.add( pager );
		
		panel.add( tablePanel, DockPanel.CENTER );
		
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
					CodecRASInfo codec = GWT.create( CodecRASInfo.class );
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JRASInfo info = codec.decode( response.isArray().get( i ) );
						dataProvider.getList().add( info );
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
	
	protected void deleteRAS( JRASInfo info ) {
		new Resource( GWT.getHostPageBaseURL() + "api/analysis/session/" + info.getId() + "/delete" ).delete().send( new JsonCallbackWrapper<JRASInfo>( info ) {
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
