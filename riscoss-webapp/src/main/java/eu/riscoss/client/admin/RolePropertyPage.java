package eu.riscoss.client.admin;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.Log;
import eu.riscoss.client.codec.CodecUserInfo;
import eu.riscoss.shared.JUserInfo;

public class RolePropertyPage implements IsWidget {
	
	SimplePanel panel = new SimplePanel();
	
	TabPanel	tabPanel = new TabPanel();
	
	UserList	userList;
	
	public RolePropertyPage() {
		HorizontalPanel toolbar = new HorizontalPanel();
		toolbar.add( new Button( "Add User", new ClickHandler() {
			@Override
			public void onClick( ClickEvent event ) {
				onNewUserClicked();
			}
		} ) );
		
		userList = new UserList();
		
		DockPanel dock = new DockPanel();
		dock.add( toolbar,DockPanel.NORTH );
		dock.add( userList, DockPanel.CENTER );
		
		tabPanel.add( new SimplePanel(), "Role Properties" );
		tabPanel.add( dock, "Users" );
		
		panel.setWidget( tabPanel );
	}
	
	class UserSelectionDialog {
		DialogBox dialog = new DialogBox( true, true );
		
		public List<String> selectRoles() {
			
			// TODO: load the list of users
			new Resource( GWT.getHostPageBaseURL() + "api/admin/users/list" )
			.get().send( new JsonCallback() {
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					if( response == null ) return;
					if( response.isArray() == null ) return;
					Log.println( "" + response );
					JSONArray array = response.isArray();
					CodecUserInfo codec = GWT.create( CodecUserInfo.class );
					userList.clear();
					for( int i = 0; i < array.size(); i++ ) {
						JUserInfo info = codec.decode( array.get( i ) );
						userList.append( info );
					}
				}
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
			});
			
			dialog.show();
			
			// TODO Auto-generated method stub
			return new ArrayList<String>();
		}
	}
	
	protected void onNewUserClicked() {
		
		List<String> roles = new UserSelectionDialog().selectRoles();
		
		// TODO: assign user to role(s?)
//		RiscossCall.fromCookies().withService( "admin" ).;
	}

	@Override
	public Widget asWidget() {
		return this.panel;
	}

	public void setSelectedRole( String domainName, String roleName ) {
		
//		this.selectedDomain = domainName;
		new Resource( GWT.getHostPageBaseURL() + "api/admin/" + domainName + "/roles/" + roleName + "/users/list" )
		.get().send( new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				if( response == null ) return;
				if( response.isArray() == null ) return;
				Log.println( "" + response );
				JSONArray array = response.isArray();
				CodecUserInfo codec = GWT.create( CodecUserInfo.class );
				userList.clear();
				for( int i = 0; i < array.size(); i++ ) {
					JUserInfo info = codec.decode( array.get( i ) );
					userList.append( info );
				}
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
		
	}
	
}
