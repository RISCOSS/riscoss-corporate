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

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class LabeledWidget implements IsWidget {
	
	HorizontalPanel panel = new HorizontalPanel();
	
	public LabeledWidget( String label, Widget w ) {
		this( new Label( label ), w );
	}
	
	public LabeledWidget( IsWidget label, Widget w ) {
		panel.add( label );
		panel.add( w );
	}
	
	@Override
	public Widget asWidget() {
		return panel;
	}

}
