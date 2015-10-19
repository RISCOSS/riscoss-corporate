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

package eu.riscoss.client.rdr;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.JsonRiskDataList;

public class EntityDataBox implements IsWidget {
	
	SimplePanel panel = new SimplePanel();
	String entity; 
	
	@Override
	public Widget asWidget() {
		return panel;
	}

	public void setSelectedEntity( String name ) {
		
		entity = name;
		
		if( panel.getWidget() != null )
			panel.getWidget().removeFromParent();
		
		RiscossJsonClient.getRiskData( name, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}

			@Override
			public void onSuccess(Method method, JSONValue response) {
				showRiskData( new JsonRiskDataList( response ) );
			}} );
		
	}

	protected void showRiskData( JsonRiskDataList list ) {
		VerticalPanel p = new VerticalPanel();
		p.setWidth("100%");
		Grid grid = new Grid( list.size(), 2 );
//		grid.getColumnFormatter().setWidth(0, "20%");
//		grid.getColumnFormatter().setWidth(1, "80%");
		for( int i = 0; i < list.size(); i++ ) {
			JsonRiskDataList.RiskDataItem item = list.get( i );
			grid.setWidget( i, 0, new Label( item.getId() ) );
			grid.setWidget( i, 1, new Label( item.getValue() ) );
		}
		Label title = new Label(entity);
		title.setStyleName("smallTitle");
		p.add(title);
		p.add(grid);
		panel.setWidget( p );
	}

}
