package eu.riscoss.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;

public class FramePanel {

	Frame frame = new Frame();
	private String url;

	
	public FramePanel( String url ) {
		this( url, "100%", "100%" );
	}

	public FramePanel( String url, String w, String h ) {
		this.url = url;
		frame.setSize( w, h );
		frame.getElement().getStyle().setBorderWidth(0, Unit.PX);
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.client.ActivablePanel#getWidget()
	 */
	public Widget getWidget() {
		return frame;
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.client.ActivablePanel#activate()
	 */
	public void activate() {
		frame.setUrl( GWT.getHostPageBaseURL() + url );
	}
	
	public void setUrl( String url ) {
		this.frame.setUrl( url );
	}
}
