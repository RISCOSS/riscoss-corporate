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
import com.google.gwt.user.client.ui.Label;
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
	
	String selectedDomain = "";
	String selectedRole;
	
	String domainRole = "";
	
	
	TabPanel tabPanel = new TabPanel();
	
//	RoleList			roleList;
	UserList			userList = new UserList(selectedDomain);
	
	ListBox				roleBox;
	Boolean				isPublic;
	
	/*class UserSelectionDialog {
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
	}*/
	
	ListBox newUserName = new ListBox();
	ListBox newUserRole = new ListBox();
	DockPanel dock = new DockPanel();
	
	HorizontalPanel main = new HorizontalPanel();
	VerticalPanel leftPanel = new VerticalPanel();
	
	public DomainPropertyPage() {
		
		generateButton();
		
//		exportJS();
		tabPanel.add( main, "Users" );
		//tabPanel.add( grid, "Properties" );
		tabPanel.setWidth("100%");
		
		tabPanel.selectTab( 0 );
		
		panel.add( tabPanel );
		panel.setWidth("100%");
		
		panel.setVisible( false );
		
		HorizontalPanel newUserData = new HorizontalPanel();
		newUserData.setStyleName("layerData");
		Label name = new Label("Name");
		name.setStyleName("bold");
		newUserData.add(name);
		newUserName.setWidth("120px");
		newUserName.setStyleName("layerNameField");
		newUserData.add(newUserName);
		
		Label role = new Label("Role");
		role.setStyleName("bold");
		newUserData.add(role);
		newUserRole.setWidth("120px");
		newUserRole.setStyleName("layerNameField");
		newUserData.add(newUserRole);
		
		Button newUser = new Button("Add User");
		newUser.setStyleName("deleteButton");
		newUser.addClickHandler(new ClickHandler() {
			@Override
			public void onClick( ClickEvent event ) {
				onNewUserClicked();
			}
		});
		newUserData.add(newUser);
		
		leftPanel.add(newUserData);
		//dock.add( userList, DockPanel.CENTER );
		
		/*KeyValueGrid grid = new KeyValueGrid();
		
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
		grid.add( "Default user role", roleBox );*/
		
		main.setStyleName("mainLayerPanel");
		main.setWidth("100%");
		leftPanel.setStyleName("leftLayerPanel");
		leftPanel.setWidth("100%");
		
		main.add(leftPanel);
		
		
		loadData();
		
	}
	
	Button delete;
	
	private void generateButton() {
		delete = new Button("Delete");
		delete.setStyleName("button");
		delete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				RiscossJsonClient.removeDomainUserRole(selectedDomain, selectedUser, new JsonCallback() {

					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert(exception.getMessage());
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						loadData();
						leftPanel.remove(userList);
						leftPanel.remove(delete);
						userList = new UserList(selectedDomain);
						setDPP();
						leftPanel.add(userList);
					}
				});
			}
		});
	}

	public void loadData() {
		newUserName.clear();
		newUserRole.clear();
		RiscossJsonClient.listUsers(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage() );
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				JSONArray array = response.isArray();
				CodecUserInfo codec = GWT.create( CodecUserInfo.class );
				for( int i = 0; i < array.size(); i++ ) {
					JUserInfo info = codec.decode( array.get( i ) );
					newUserName.addItem(info.getUsername());
				}
			}
		});
		for (KnownRoles r : KnownRoles.values()) {
			newUserRole.addItem(r.toString());
		}
	
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
				
				domainRole = info.predefinedRole;
				if (domainRole.equals("")) isPublic = false;
				else isPublic = true;
				leftPanel.remove(userList);
				userList = new UserList(selectedDomain);
				leftPanel.add(userList);
				setDPP();
				/*RiscossJsonClient.getDomainUsers(info.name, new JsonCallback() {
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
				});*/
				
			}
		} );
		
		panel.setVisible( true );
		
	}
	
	protected void setDPP() {
		userList.setDPP(this);
	}
	
	protected void onNewUserClicked() {
		importUser(newUserName.getItemText(newUserName.getSelectedIndex()),
				newUserRole.getItemText(newUserRole.getSelectedIndex()));
	}
	
	void importUser( String user , String role) {
		
		if( user == null ) return;
		RiscossJsonClient.setDomainUserRole(selectedDomain, user, role, new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				loadData();
				leftPanel.remove(userList);
				userList = new UserList(selectedDomain);
				setDPP();
				leftPanel.add(userList);
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
		
		// TODO: assign user to role(s?)
//		RiscossCall.fromCookies().withService( "admin" ).;
	}
	
	public String getRole() {
		return domainRole;
	}
	
	public void save() {
		userList.save();
	}
	
	String selectedUser;
	
	public void setSelectedUser(String user) {
		
		selectedUser = user;
		
		if (!isPublic) {
			leftPanel.remove(delete);
			delete.setText("Delete " + user + " from " + selectedDomain);
			leftPanel.add(delete);
		}
		
		//rightPanel.add(userData);
	}
	
}
