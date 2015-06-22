package eu.riscoss.client.layers;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class LayerPropertyPage implements IsWidget {
	
	SimplePanel panel = new SimplePanel();
	
	public LayerPropertyPage() {
		panel.setWidget( new Label("Properties" ) );
	}
	
	@Override
	public Widget asWidget() {
		return this.panel;
	}
	
	public void setSelectedLayer( String layer ) {
		
	}
	
}
