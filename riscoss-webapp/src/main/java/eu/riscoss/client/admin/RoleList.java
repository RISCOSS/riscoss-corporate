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
import eu.riscoss.shared.RoleInfo;

public class RoleList implements IsWidget {
	
	CellTable<RoleInfo>			table;
	ListDataProvider<RoleInfo>	dataProvider;
	
	VerticalPanel				tablePanel = new VerticalPanel();
	
	public RoleList() {
		
		table = new CellTable<>();
		
		table.addColumn( new Column<RoleInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(RoleInfo roleInfo) {
				return new LinkHtml( roleInfo.getName(), "javascript:selectRole(\"" + roleInfo.getName() + "\")" ); };
		}, "Role");
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
		
		dataProvider = new ListDataProvider<RoleInfo>();
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

	public void append( RoleInfo info ) {
		dataProvider.getList().add( info );
	}
	
}
