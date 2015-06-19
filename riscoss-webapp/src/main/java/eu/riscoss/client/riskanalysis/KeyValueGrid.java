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
