package eu.riscoss.client.admin;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossCall;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecDomainInfo;
import eu.riscoss.client.codec.CodecUserInfo;
import eu.riscoss.client.riskanalysis.KeyValueGrid;
import eu.riscoss.client.ui.ClickWrapper;
import eu.riscoss.shared.JDomainInfo;
import eu.riscoss.shared.JUserInfo;
import eu.riscoss.shared.KnownRoles;

public class DomainPropertyPage implements IsWidget {
	
//	public native void exportJS() /*-{
//	var that = this;
//	$wnd.selectRole = $entry(function(amt) {
//		that.@eu.riscoss.client.admin.DomainPropertyPage::setSelectedRole(Ljava/lang/String;)(amt);
//	});
//}-*/;
	
	HorizontalPanel panel = new HorizontalPanel();
	
	String selectedDomain;
	String selectedRole;
	
	
	TabPanel tabPanel = new TabPanel();
	
//	RoleList			roleList;
	UserList			userList;
	
	ListBox				roleBox;
	
	class UserSelectionDialog {
		DialogBox dialog = new DialogBox( false, true );
		
		VerticalPanel list = new VerticalPanel();
		
		public void selectUsers() {
			
			DockPanel dock = new DockPanel();
			
			dock.add( list, DockPanel.CENTER );
			dock.add( new Button( "Done", new ClickHandler() {
				@Override
				public void onClick( ClickEvent event ) {
					dialog.hide();
				}
			} ), DockPanel.SOUTH );
			
			dialog.setWidget( dock );
			dialog.setText( "Add User" );
			
			dialog.center();
			
			// TODO: load the list of users
			RiscossJsonClient.listUsers(new JsonCallback() {
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					if( response == null ) return;
					if( response.isArray() == null ) return;
					Log.println( "" + response );
					JSONArray array = response.isArray();
					CodecUserInfo codec = GWT.create( CodecUserInfo.class );
					
					for( int i = 0; i < array.size(); i++ ) {
						JUserInfo info = codec.decode( array.get( i ) );
						Anchor a = new Anchor( info.getUsername() );
						a.addClickHandler( new ClickWrapper<String>( info.getUsername() ) {
							@Override
							public void onClick( ClickEvent event ) {
								dialog.hide();
								importUser( getValue() );
							}} );
						list.add( a );
					}
				}
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
			});
			
		}
	}
	
	public DomainPropertyPage() {
		
//		exportJS();
		
		userList = new UserList();
		
		userList.asWidget().setHeight( "100%" );
		
		HorizontalPanel toolbar = new HorizontalPanel();
		toolbar.add( new Button( "Add User", new ClickHandler() {
			@Override
			public void onClick( ClickEvent event ) {
				onNewUserClicked();
			}
		} ) );
		
		DockPanel dock = new DockPanel();
		dock.add( toolbar, DockPanel.NORTH );
		dock.add( userList, DockPanel.CENTER );
		
		KeyValueGrid grid = new KeyValueGrid();
		
		roleBox = new ListBox( false );
		roleBox.addItem( "[none]" );
		for( KnownRoles r : KnownRoles.values() ) {
			roleBox.addItem( r.name() );
		}
		roleBox.addChangeHandler( new ChangeHandler() {
			@Override
			public void onChange( ChangeEvent event ) {
				RiscossCall.fromCookies().withDomain(selectedDomain).admin().fx("default-role")
				.arg("role", roleBox.getItemText( roleBox.getSelectedIndex() ) ).post( new JsonCallback() {
					@Override
					public void onSuccess( Method method, JSONValue response ) {}
					@Override
					public void onFailure( Method method, Throwable exception ) {}
				});
			}
		});
		grid.add( "Default user role", roleBox );
		
		tabPanel.add( dock, "Users" );
		tabPanel.add( grid, "Properties" );
		
		tabPanel.selectTab( 0 );
		
		panel.add( tabPanel );
		
		panel.setVisible( false );
	}
	
	@Override
	public Widget asWidget() {
		return this.panel;
	}
	
	public void setSelectedDomain( String domainName ) {
		
		this.selectedDomain = domainName;
		
		RiscossJsonClient.getDomainInfo(domainName, new JsonCallback() {
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				CodecDomainInfo codec = GWT.create( CodecDomainInfo.class );
				JDomainInfo info = codec.decode( response );
				Log.println( "" + response );
				if( "".equals( info.predefinedRole ) ) {
					roleBox.setSelectedIndex( 0 );
				}
				else {
					for( int i = 0; i < roleBox.getItemCount(); i++ ) {
						if( roleBox.getItemText( i ).equals( info.predefinedRole ) ) {
							roleBox.setSelectedIndex( i );
						}
					}
				}
				
				RiscossJsonClient.getDomainUsers(info.name, new JsonCallback() {
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
		} );
		
		panel.setVisible( true );
		
	}
	
	protected void onNewUserClicked() {
		new UserSelectionDialog().selectUsers();
	}
	
	void importUser( String user ) {
		
		if( user == null ) return;
		RiscossJsonClient.setDomainUserRole(selectedDomain, user, KnownRoles.Guest.name(), new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				CodecUserInfo codec = GWT.create( JUserInfo.class );
				JUserInfo info = codec.decode( response );
				userList.append( info );
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
		
		// TODO: assign user to role(s?)
//		RiscossCall.fromCookies().withService( "admin" ).;
	}
	
}
