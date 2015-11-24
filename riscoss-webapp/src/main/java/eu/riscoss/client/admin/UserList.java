package eu.riscoss.client.admin;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecUserInfo;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.ui.LinkHtml;
import eu.riscoss.shared.JUserInfo;
import eu.riscoss.shared.KnownRoles;
import eu.riscoss.shared.Pair;

public class UserList implements IsWidget {
	
	CellTable<Pair<String,String>>			table;
	ListDataProvider<Pair<String,String>>	dataProvider;
	
	VerticalPanel				tablePanel = new VerticalPanel();
	ArrayList<String>			roles = new ArrayList<>();
	SimplePager pager;
	
	String selectedDomain;
	
	DomainPropertyPage dpp = null;
	
	ArrayList<Pair<String, String>> users;
	int size;
	
	public UserList(String selectedD) {
		if (!selectedD.equals("")) {
			this.selectedDomain = selectedD;
			for (KnownRoles r : KnownRoles.values()) {
				roles.add(r.toString());
			}
			
			users = new ArrayList<>();
			
			RiscossJsonClient.getDomainUsers(selectedDomain, new JsonCallback() {
				String currentName = "";
				int currentI;
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert(exception.getMessage());
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					CodecUserInfo codec = GWT.create( CodecUserInfo.class );
					size = response.isArray().size();
					for (int i = 0; i < size; ++i ) {
						currentI = i;
						JUserInfo info = codec.decode( response.isArray().get( i ) );
						currentName = info.getUsername();
						RiscossJsonClient.getDomainUserRole(selectedDomain, currentName, new JsonCallback() {
							String name = currentName;
							int k = currentI;
							@Override
							public void onFailure(Method method, Throwable exception) {
								Window.alert(exception.getMessage());
							}
							@Override
							public void onSuccess(Method method, JSONValue response) {
								String p[] = response.isString().stringValue().split("-");
								users.add(new Pair<String, String>(name, p[0]));
								if (k == size-1) generateTable();
							}
						});
					}
	 			}
			});
		
		}
	}

	public void generateTable() {
		table = new CellTable<>(15, (Resources) GWT.create(TableResources.class));
		
		/*table.addColumn( new Column<JUserInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(JUserInfo roleInfo) {
				return new LinkHtml( roleInfo.getUsername(), "javascript:selectUser(\"" + roleInfo.getUsername() + "\")" ); };
		}, "User");
		table.addColumn( new Column<JUserInfo,String>(new SelectionCell(roles)) {
			String role = "";
			@Override
			public String getValue(JUserInfo arg0) {
				Window.alert(arg0.getUsername());
				RiscossJsonClient.getDomainUserRole(selectedDomain, arg0.getUsername().trim(), new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert(exception.getMessage());
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						String sp[] = response.isString().stringValue().split("-");
						role = sp[0];
						Window.alert(role);
					}
				});
				return role;
			}*/
		Column<Pair<String,String>, String> userName = new Column<Pair<String, String>, String>(new TextCell()) {
			@Override
			public String getValue(Pair<String, String> arg0) {
				return arg0.getLeft();
			}
		};
		Column<Pair<String,String>, String> role = new Column<Pair<String,String>, String>(new SelectionCell(roles)) {
			@Override
			public String getValue(Pair<String,String> arg0) {
				return arg0.getRight();
			}
		};
		table.setRowData(0, users);
		
		role.setFieldUpdater(new FieldUpdater<Pair<String,String>, String>() {
			@Override
			public void update(int arg0, Pair<String, String> arg1, String arg2) {
				Pair<String, String> p = new Pair<String,String>(arg1.getLeft(), arg2);
				dataProvider.getList().set(arg0, p);
			}
		});
		
		final SelectionModel<Pair<String, String>> selectionModel = new SingleSelectionModel<Pair<String, String>>();
	    table.setSelectionModel(selectionModel);
	    selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent arg0) {
				List<Pair<String, String>> l = dataProvider.getList();
				for (int i = 0; i < l.size(); ++i) {
					if (selectionModel.isSelected(l.get(i))) {
						dpp.setSelectedUser(l.get(i).getLeft());
					}
				}
			}
	    });
		
		
		table.addColumn(userName, "User name");
		table.addColumn(role, "Role");
		
		dataProvider = new ListDataProvider<Pair<String,String>>();
		dataProvider.addDataDisplay( table );
		
		for( int i = 0; i < users.size(); i++ ) {
			dataProvider.getList().add( users.get(i) );
		}
		
		pager = new SimplePager();
	    pager.setDisplay( table );
	    
		tablePanel.add( table );
		tablePanel.add( pager );
		
		table.setWidth("400px");
		tablePanel.setWidth("400px");
		
		
	}
	
	@Override
	public Widget asWidget() {
		return this.tablePanel;
	}

	public void clear() {
		dataProvider.getList().clear();
	}
	
	public void save() {
		List<Pair<String, String>> usersAndRoles = dataProvider.getList();
		for (Pair<String,String> p : usersAndRoles) {
			//Window.alert(p.getLeft() + " - " + p.getRight());
			RiscossJsonClient.setDomainUserRole(selectedDomain, p.getLeft(), p.getRight(), new JsonCallback() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert(exception.getMessage());
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					
				}
			});
		}
		
		
	}
	
	public void setDPP(DomainPropertyPage dpp) {
		this.dpp = dpp;
	}
	
}
