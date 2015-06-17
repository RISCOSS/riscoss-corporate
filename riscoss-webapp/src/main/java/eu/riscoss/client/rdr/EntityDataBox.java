package eu.riscoss.client.rdr;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.JsonRiskDataList;

public class EntityDataBox implements IsWidget {
	
	SimplePanel panel = new SimplePanel();
	
	@Override
	public Widget asWidget() {
		return panel;
	}

	public void setSelectedEntity( String name ) {
		
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
		Grid grid = new Grid( list.size(), 2 );
//		grid.getColumnFormatter().setWidth(0, "20%");
//		grid.getColumnFormatter().setWidth(1, "80%");
		for( int i = 0; i < list.size(); i++ ) {
			JsonRiskDataList.RiskDataItem item = list.get( i );
			grid.setWidget( i, 0, new Label( item.getId() ) );
			grid.setWidget( i, 1, new Label( item.getValue() ) );
		}
		panel.setWidget( grid );
	}

}
