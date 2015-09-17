package eu.riscoss.client.admin;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;

import eu.riscoss.client.RiscossCall;
import eu.riscoss.client.RiscossJsonClient;

public class AdminModule implements EntryPoint {

	public native void exportJS() /*-{
	var that = this;
	$wnd.selectDomain = $entry(function(amt) {
		that.@eu.riscoss.client.admin.AdminModule::setSelectedDomain(Ljava/lang/String;)(amt);
	});
}-*/;
	
	String				selectedDomain = null;
	
	DomainList			domainList;
	
	DomainPropertyPage	domainPPG;
	
	@Override
	public void onModuleLoad() {
		
		exportJS();
		
//		HorizontalPanel vp = new HorizontalPanel();
		
//		{
			HorizontalPanel toolbar = new HorizontalPanel();
			toolbar.add( new Button( "New Domain", new ClickHandler() {
				@Override
				public void onClick( ClickEvent event ) {
					onNewDomainClicked();
				}
			} ) );
			
			DockPanel dock = new DockPanel();
			dock.add( toolbar,DockPanel.NORTH );
			
			domainList = new DomainList();
//			domainList.asWidget().setHeight( "100%" );
			dock.add( domainList, DockPanel.WEST );
			
//			vp.add( dock );
			
//		}
		
		domainPPG = new DomainPropertyPage();
		
		dock.add( domainPPG, DockPanel.CENTER );
		
		dock.setSize( "100%", "100%" );
		dock.setCellHeight( toolbar, "1%" );
		dock.setCellWidth( domainPPG, "75%" );
		
		RootPanel.get().add( dock );
		
		RiscossJsonClient.listDomainsForUser( null, new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				if( response == null ) return;
				if( response.isArray() == null ) return;
				JSONArray array = response.isArray();
				for( int i = 0; i < array.size(); i++ ) {
					String domain = array.get( i ).isString().stringValue();
					domainList.append( domain );
				}
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	protected void onNewDomainClicked() {
		String name = Window.prompt( "Domain name:", "" );
		if( name == null || name.trim().equals("") ) 
			return;
		RiscossJsonClient.createDomain(name,  new JsonCallback() {
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					domainList.append( response.isString().stringValue() );
				}
				
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
			});
	}
	
	public void setSelectedDomain( String domainName ) {
		this.domainPPG.setSelectedDomain( domainName );
	}
	
}
