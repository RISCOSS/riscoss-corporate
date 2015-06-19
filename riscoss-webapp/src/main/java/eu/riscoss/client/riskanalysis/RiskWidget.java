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
