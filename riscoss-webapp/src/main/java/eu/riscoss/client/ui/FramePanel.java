/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/**
 * @author 	Alberto Siena
**/

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
