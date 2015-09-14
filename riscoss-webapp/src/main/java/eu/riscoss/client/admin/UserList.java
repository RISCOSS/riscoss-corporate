package eu.riscoss.client.admin;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.ui.LinkHtml;
import eu.riscoss.shared.JUserInfo;

//never used (only in unused RolePropertyPage!!)
@Deprecated
public class UserList implements IsWidget {
	
	CellTable<JUserInfo>			table;
	ListDataProvider<JUserInfo>	dataProvider;
	
	VerticalPanel				tablePanel = new VerticalPanel();
	
	public UserList() {
		
		table = new CellTable<>();
		
		table.addColumn( new Column<JUserInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(JUserInfo roleInfo) {
				return new LinkHtml( roleInfo.getUsername(), "javascript:selectUser(\"" + roleInfo.getUsername() + "\")" ); };
		}, "User");
//		Column<RoleInfo,String> c = new Column<RoleInfo,String>(new ButtonCell() ) {
//			@Override
//			public String getValue(EntityInfo object) {
//				return "Delete";
//			}};
//			c.setFieldUpdater(new FieldUpdater<RoleInfo, String>() {
//				@Override
//				public void update(int index, RoleInfo object, String value) {
//					deleteEntity( object );
//				}
//			});
//		table.addColumn( c, "");
		
		dataProvider = new ListDataProvider<JUserInfo>();
		dataProvider.addDataDisplay( table );
		
		SimplePager pager = new SimplePager();
	    pager.setDisplay( table );
	    
		tablePanel.add( table );
		tablePanel.add( pager );
		
		
	}

	@Override
	public Widget asWidget() {
		return this.tablePanel;
	}

	public void clear() {
		dataProvider.getList().clear();
	}

	public void append( JUserInfo info ) {
		dataProvider.getList().add( info );
	}
	
}
