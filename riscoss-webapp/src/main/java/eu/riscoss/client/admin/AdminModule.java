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
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.riscoss.client.Log;
import eu.riscoss.client.codec.RoleInfoCodec;
import eu.riscoss.shared.JRoleInfo;

public class AdminModule implements EntryPoint {

	public native void exportJS() /*-{
	var that = this;
	$wnd.selectRole = $entry(function(amt) {
		that.@eu.riscoss.client.admin.AdminModule::setSelectedRole(Ljava/lang/String;)(amt);
	});
}-*/;
	
	RoleList roleList;
	UserList userList;
	
	@Override
	public void onModuleLoad() {
		
		exportJS();
		
		VerticalPanel vp = new VerticalPanel();
		
		{
			HorizontalPanel toolbar = new HorizontalPanel();
			toolbar.add( new Button( "New Role", new ClickHandler() {
				@Override
				public void onClick( ClickEvent event ) {
					onNewRoleClicked();
				}
			} ) );
			
			roleList = new RoleList();
			
			DockPanel dock = new DockPanel();
			dock.add( toolbar,DockPanel.NORTH );
			dock.add( roleList, DockPanel.CENTER );
			
			vp.add( dock );
			
		}
		
		{
			HorizontalPanel toolbar = new HorizontalPanel();
			toolbar.add( new Button( "New User", new ClickHandler() {
				@Override
				public void onClick( ClickEvent event ) {
					onNewUserClicked();
				}
			} ) );
			
			userList = new UserList();
			
			DockPanel dock = new DockPanel();
			dock.add( toolbar,DockPanel.NORTH );
			dock.add( userList, DockPanel.CENTER );
			
			vp.add( dock );
			
		}
		
		vp.setSize( "100%", "100%" );
		
		RootPanel.get().add( vp );
		
		new Resource( GWT.getHostPageBaseURL() + "api/admin/roles/list" )
			.get().send( new JsonCallback() {
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					if( response == null ) return;
					if( response.isArray() == null ) return;
					Log.println( "" + response );
					JSONArray array = response.isArray();
					RoleInfoCodec codec = GWT.create( RoleInfoCodec.class );
					for( int i = 0; i < array.size(); i++ ) {
						JRoleInfo info = codec.decode( array.get( i ) );
						roleList.append( info );
					}
				}
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
			});
	}
	
	protected void onNewUserClicked() {
		String name = Window.prompt( "User name:", "" );
		if( name == null ) return;
		name = name.trim();
		if( "".equals( name ) ) return;
		new Resource( GWT.getHostPageBaseURL() + "api/admin/users/create" )
			.addQueryParam( "name", name ).post().send( new JsonCallback() {
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					RoleInfoCodec codec = GWT.create( RoleInfoCodec.class );
					JRoleInfo info = codec.decode( response );
					roleList.append( info );
				}
				
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
			});
	}

	protected void onNewRoleClicked() {
		String name = Window.prompt( "Role name:", "" );
		if( name == null ) return;
		name = name.trim();
		if( "".equals( name ) ) return;
		new Resource( GWT.getHostPageBaseURL() + "api/admin/roles/create" )
			.addQueryParam( "name", name ).post().send( new JsonCallback() {
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					RoleInfoCodec codec = GWT.create( RoleInfoCodec.class );
					JRoleInfo info = codec.decode( response );
					roleList.append( info );
				}
				
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
			});
	}

	public void setSelectedRole( String role ) {}
	
}
