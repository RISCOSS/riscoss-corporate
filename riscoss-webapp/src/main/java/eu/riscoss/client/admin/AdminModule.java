package eu.riscoss.client.admin;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.riscoss.client.RiscossCall;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecDomainInfo;
import eu.riscoss.client.riskanalysis.KeyValueGrid;
import eu.riscoss.shared.JDomainInfo;
import eu.riscoss.shared.KnownRoles;

public class AdminModule implements EntryPoint {

	public native void exportJS() /*-{
	var that = this;
	$wnd.selectDomain = $entry(function(amt) {
		that.@eu.riscoss.client.admin.AdminModule::setSelectedDomain(Ljava/lang/String;)(amt);
	});
}-*/;
	
	String				selectedDomain = null;
	
	VerticalPanel		page = new VerticalPanel();
	HorizontalPanel		mainView = new HorizontalPanel();
	VerticalPanel		leftPanel = new VerticalPanel();
	VerticalPanel		rightPanel = new VerticalPanel();
	
	TextBox 			newDomainName = new TextBox();
	ListBox				roleList = new ListBox();
	
	DomainList			domainList;
	
	DomainPropertyPage	domainPPG;
	
	@Override
	public void onModuleLoad() {
		
		exportJS();
		
		roleList.addItem("[none]");
		for(KnownRoles r: KnownRoles.values()) {
			roleList.addItem(r.name());
		}
		
//		HorizontalPanel vp = new HorizontalPanel();
		mainView.setStyleName("mainViewLayer");
		mainView.setWidth("100%");
		leftPanel.setStyleName("leftPanelLayer");
		leftPanel.setWidth("450px");
		rightPanel.setStyleName("rightPanelLayer");
		page.setWidth("100%");
//		{
//		HorizontalPanel toolbar = new HorizontalPanel();
		Button newDomain = new Button("New Domain");
		newDomain.addClickHandler(new ClickHandler() {
			@Override
			public void onClick( ClickEvent event ) {
				onNewDomainClicked();
			}
		});
		newDomain.setStyleName("button");
//		toolbar.add(newDomain);
		
//		DockPanel dock = new DockPanel();
//		dock.add( toolbar,DockPanel.NORTH );
		
		domainList = new DomainList();
//			domainList.asWidget().setHeight( "100%" );
//		dock.add( domainList, DockPanel.WEST );
			
//			vp.add( dock );
			
//		}
		
		//domainPPG = new DomainPropertyPage();
		
		Label title = new Label("Domain management");
		title.setStyleName("title");
		page.add(title);
		
		HorizontalPanel newDomainData = new HorizontalPanel();
		newDomainData.setStyleName("layerData");
		Label name = new Label("Name");
		name.setStyleName("bold");
		newDomainData.add(name);
		newDomainName.setWidth("120px");
		newDomainName.setStyleName("layerNameField");
		newDomainData.add(newDomainName);
		
		Label role = new Label("Default role");
		role.setStyleName("bold");
		newDomainData.add(role);
		newDomainData.add(roleList);
		
		
		leftPanel.add(newDomainData);
		leftPanel.add(newDomain);
		leftPanel.add(domainList);
		
		mainView.add(leftPanel);
		mainView.add(rightPanel);
		page.add(mainView);
		
//		dock.add( domainPPG, DockPanel.CENTER );
		
//		dock.setSize( "100%", "100%" );
//		dock.setCellHeight( toolbar, "1%" );
//		dock.setCellWidth( domainPPG, "75%" );
		
		RootPanel.get().add( page );
		
		RiscossJsonClient.listAllDomains(new JsonCallback() {
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
	
	String		newName;
	
	protected void onNewDomainClicked() {
		String name = newDomainName.getText().trim();
		if( name == null || name.trim().equals("") ) 
			return;
		newName = name;
		RiscossJsonClient.listAllDomains(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				for (int i = 0; i < response.isArray().size(); ++i) {
					if (response.isArray().get(i).isString().stringValue().equals(newName)) {
						Window.alert("Domain name already in use. Please write another name");
						return;
					}
				}
				RiscossJsonClient.createDomain(newName,  new JsonCallback() {
					@Override
					public void onSuccess( Method method, JSONValue response ) {
						RiscossCall.fromCookies().withDomain(newName).admin().fx("default-role")
						.arg("role", roleList.getItemText( roleList.getSelectedIndex() ) ).post( new JsonCallback() {
							@Override
							public void onSuccess( Method method, JSONValue response ) {
								newDomainName.setText("");
								roleList.setSelectedIndex(0);
								setSelectedDomain(newName);
								domainList.append( newName );
							}
							@Override
							public void onFailure( Method method, Throwable exception ) {}
						});
					}
					
					@Override
					public void onFailure( Method method, Throwable exception ) {
						Window.alert( exception.getMessage() );
					}
				});
			}
		});
	}
	
	ListBox roleBox;
	
	public void setSelectedDomain( String domainName ) {
		domainPPG = new DomainPropertyPage();
		selectedDomain = domainName;
		this.domainPPG.setSelectedDomain( selectedDomain );
		
		rightPanel.clear();
		rightPanel.setWidth("90%");
		Label subtitle = new Label(selectedDomain);
		subtitle.setStyleName("subtitle");
		rightPanel.add(subtitle);
		
		roleBox = new ListBox( false );
		roleBox.addItem("[none]");
		for( KnownRoles r : KnownRoles.values() ) {
			roleBox.addItem( r.name() );
		}
		
		/*roleBox.addChangeHandler( new ChangeHandler() {
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
		});*/
		RiscossJsonClient.getDomainInfo(selectedDomain, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				CodecDomainInfo codec = GWT.create( CodecDomainInfo.class );
				JDomainInfo info = codec.decode( response );
				if (info.predefinedRole.equals("")) roleBox.setSelectedIndex(0);
				else {
					for (int i = 0; i < roleBox.getItemCount(); ++i) {
						if (info.predefinedRole.equals(roleBox.getItemText(i))) roleBox.setSelectedIndex(i);
					}
				}
				
				HorizontalPanel domainData = new HorizontalPanel();
				domainData.setStyleName("layerData");
				
				Label publicDomain = new Label("Is it a public domain?");
				publicDomain.setStyleName("bold");
				domainData.add(publicDomain);
				
				if (info.predefinedRole.equals("")) {
					Label answer = new Label("No");
					domainData.add(answer);
				}
				else {
					Label answer = new Label("Yes");
					domainData.add(answer);
					HorizontalPanel space = new HorizontalPanel();
					space.setWidth("50px");
					domainData.add(space);
					Label name = new Label("Default role");
					name.setStyleName("bold");
					domainData.add(name);
					domainData.add(roleBox);
				}
				
				rightPanel.add(domainData);
				
				HorizontalPanel buttons = new HorizontalPanel();
				Button save = new Button("Save");
				save.setStyleName("button");
				save.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent arg0) {
						save();
					}
				});
				Button delete = new Button("Delete");
				delete.setStyleName("button");
				delete.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent arg0) {
						deleteDomain();
					}
				});
				buttons.add(save);
				buttons.add(delete);
				rightPanel.add(buttons);
				
				rightPanel.add(domainPPG);
			}
		});
		
	}
	
	private void save() {
		saveDefaultRole();
		savePropertyPageChanges();
	}

	private void saveDefaultRole() {
		RiscossCall.fromCookies().withDomain(selectedDomain).admin().fx("default-role")
		.arg("role", roleBox.getItemText( roleBox.getSelectedIndex() ) ).post( new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {}
			@Override
			public void onFailure( Method method, Throwable exception ) {}
		});
	}
	
	private void savePropertyPageChanges() {
		domainPPG.save();
	}
	
	private void deleteDomain() {
		RiscossJsonClient.getDomainUsers(selectedDomain, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				if (response.isArray().size() > 0) Window.alert("Domains with associated users cannot be deleted");
				else {
					if (Window.confirm("Are you sure that you want to delete the domain " + selectedDomain + "?")) {
						RiscossJsonClient.deleteDomain(selectedDomain, new JsonCallback() {
							@Override
							public void onFailure(Method method,
									Throwable exception) {
								Window.alert(exception.getMessage());
							}
							@Override
							public void onSuccess(Method method,
									JSONValue response) {
								Window.Location.reload();
							}
						});
					}
				}
			}
		});
	}
	
}
