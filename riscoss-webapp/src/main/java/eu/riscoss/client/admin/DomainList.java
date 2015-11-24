package eu.riscoss.client.admin;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.ui.LinkHtml;

public class DomainList implements IsWidget {
	
	CellTable<String>			table;
	ListDataProvider<String>	dataProvider;
	
	VerticalPanel				tablePanel = new VerticalPanel();
	
	public DomainList() {
		
		table = new CellTable<>(15, (Resources) GWT.create(TableResources.class));
		
		table.addColumn( new Column<String,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(String domain) {
				return new LinkHtml( domain, "javascript:selectDomain(\"" + domain + "\")" ); };
		}, "Domain");
		
		dataProvider = new ListDataProvider<String>();
		dataProvider.addDataDisplay( table );
		
		SimplePager pager = new SimplePager();
	    pager.setDisplay( table );
	    
	    table.setWidth("100%");
		tablePanel.add( table );
		tablePanel.add( pager );
		tablePanel.setWidth("100%");
		
		
	}

	@Override
	public Widget asWidget() {
		return this.tablePanel;
	}

	public void append( String info ) {
		dataProvider.getList().add( info );
	}
	
}
