package eu.riscoss.client.riskanalysis;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;

public class KeyValueGrid extends Grid {
	
	public void add( String label, IsWidget w ) {
		
		resize( getRowCount() +1, 2 );
		getCellFormatter().setHorizontalAlignment( getRowCount() -1, 0, HasHorizontalAlignment.ALIGN_RIGHT );
		getCellFormatter().setVerticalAlignment( getRowCount() -1, 0, HasVerticalAlignment.ALIGN_TOP );
		super.setWidget( getRowCount() -1, 0, new Label( label ) );
		super.setWidget( getRowCount() -1, 1, w );
		
	}
	
}
