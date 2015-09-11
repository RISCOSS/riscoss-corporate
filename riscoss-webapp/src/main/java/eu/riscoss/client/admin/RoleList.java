package eu.riscoss.client.admin;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.ui.LabeledWidget;
import eu.riscoss.shared.KnownRoles;

public class RoleList implements IsWidget {
	
//	CellTable<JRoleInfo>			table;
//	ListDataProvider<JRoleInfo>		dataProvider;
	
	VerticalPanel					tablePanel = new VerticalPanel();
	
	public RoleList() {
		
		for( KnownRoles r : KnownRoles.values() ) {
			CheckBox checkBox = new CheckBox();
			Anchor a = new Anchor( r.name() );
			
			tablePanel.add( new LabeledWidget( checkBox, a ) );
		}
		
//		table = new CellTable<>();
//		
//		table.addColumn( new Column<JRoleInfo,SafeHtml>(new SafeHtmlCell() ) {
//			@Override
//			public SafeHtml getValue(JRoleInfo roleInfo) {
//				return new LinkHtml( roleInfo.getName(), "javascript:selectRole(\"" + roleInfo.getName() + "\")" ); };
//		}, "Role");
//		
//		dataProvider = new ListDataProvider<JRoleInfo>();
//		dataProvider.addDataDisplay( table );
		
//		SimplePager pager = new SimplePager();
//	    pager.setDisplay( table );
	    
//		tablePanel.add( table );
//		tablePanel.add( pager );
		
		
	}

	@Override
	public Widget asWidget() {
		return this.tablePanel;
	}

//	public void append( JRoleInfo info ) {
//		dataProvider.getList().add( info );
//	}
//
//	public void clear() {
//		dataProvider.getList().clear();
//	}
	
}
