package eu.riscoss.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Image;

public class GaugeImage extends Image {
	
	public void setEvidence( String p, String m ) {
		super.setUrl( GWT.getHostPageBaseURL() + "gauge?type=e&p=" + p + "&m=" + m + "&w=100&h=50" );
	}

	public void setDistribution( String string ) {
		super.setUrl( GWT.getHostPageBaseURL() + "gauge?type=d&f=" + string );
	}
	
}
