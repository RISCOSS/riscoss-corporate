package eu.riscoss.client.admin;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.ui.LinkHtml;

public class DomainsModule implements EntryPoint {

	public native void exportJS() /*-{
	var that = this;
	$wnd.selectRole = $entry(function(amt) {
		that.@eu.riscoss.client.admin.DomainsModule::setSelectedDomain(Ljava/lang/String;)(amt);
	});
}-*/;
	
	CellTable<String>			table;
	ListDataProvider<String>	dataProvider;
	
	@Override
	public void onModuleLoad() {
		
		exportJS();
		
		VerticalPanel vp = new VerticalPanel();
		
		{
			HorizontalPanel toolbar = new HorizontalPanel();
			toolbar.add( new Button( "New Domain", new ClickHandler() {
				@Override
				public void onClick( ClickEvent event ) {
					onNewDomainClicked();
				}
			} ) );
			
			table = new CellTable<>();
			
			table.addColumn( new Column<String,SafeHtml>(new SafeHtmlCell() ) {
				@Override
				public SafeHtml getValue(String roleInfo) {
					return new LinkHtml( roleInfo, "javascript:selectDomain(\"" + roleInfo + "\")" ); };
			}, "Domain");
			
			dataProvider = new ListDataProvider<String>();
			dataProvider.addDataDisplay( table );
			
			SimplePager pager = new SimplePager();
		    pager.setDisplay( table );
		    
//			tablePanel.add( table );
//			tablePanel.add( pager );
			
			DockPanel dock = new DockPanel();
			dock.add( toolbar,DockPanel.NORTH );
			dock.add( table, DockPanel.CENTER );
			dock.add( pager, DockPanel.SOUTH );
			
			vp.add( dock );
			
		}
		
		vp.setSize( "100%", "100%" );
		
		RootPanel.get().add( vp );
		
		RiscossJsonClient.listDomainsForUser("", new JsonCallback() { //check if correct!
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					if( response == null ) return;
					if( response.isArray() == null ) return;
					Log.println( "" + response );
					JSONArray array = response.isArray();
//					CodecRoleInfo codec = GWT.create( CodecRoleInfo.class );
					for( int i = 0; i < array.size(); i++ ) {
//						JRoleInfo info = codec.decode( array.get( i ) );
						dataProvider.getList().add( array.get( i ).isString().stringValue() );
					}
				}
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
			});
	}
	
	protected void onNewDomainClicked() {
		String name = Window.prompt( "Role name:", "" );
		if( name == null ) return;
		name = name.trim();
		if( "".equals( name ) ) return;
		new Resource( GWT.getHostPageBaseURL() + "api/admin/roles/create" )
			.addQueryParam( "name", name ).post().send( new JsonCallback() {
				@Override
				public void onSuccess( Method method, JSONValue response ) {
//					CodecRoleInfo codec = GWT.create( CodecRoleInfo.class );
//					JRoleInfo info = codec.decode( response );
					dataProvider.getList().add( response.isString().stringValue() );
				}
				
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
			});
	}

	public void setSelectedDomain( String domain ) {}
	
}
