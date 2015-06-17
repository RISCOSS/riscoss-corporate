package eu.riscoss.client.ui;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class LabeledWidget implements IsWidget {
	
	HorizontalPanel panel = new HorizontalPanel();
	
	public LabeledWidget( String label, Widget w ) {
		panel.add( new Label( label ) );
		panel.add( w );
	}
	
	@Override
	public Widget asWidget() {
		return panel;
	}

}
