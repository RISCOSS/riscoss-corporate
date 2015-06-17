package eu.riscoss.client.riskanalysis;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class RiskWidget implements IsWidget {
	
	VerticalPanel panel = new VerticalPanel();
	
	Image img;
	
	public RiskWidget( JSONObject o ) {
		if( o.get( "label" ) != null )
			panel.add( new Label( o.get( "label" ).isString().stringValue()  ));
		else
			panel.add( new Label( o.get( "id" ).isString().stringValue()  ));
		img = new Image();
		img.setSize( "100px", "50px" );
		panel.add( img );
	}
	
	@Override
	public Widget asWidget() {
		return panel;
	}
	
	public void setValue( String p, String m ) {
		img.setUrl( GWT.getHostPageBaseURL() + "gauge?type=e&p=" + p + "&m=" + m + "&w=100&h=50" );
	}
}
