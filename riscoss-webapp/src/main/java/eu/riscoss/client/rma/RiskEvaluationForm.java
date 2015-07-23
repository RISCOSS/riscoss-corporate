package eu.riscoss.client.rma;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.shared.JAHPComparison;

public class RiskEvaluationForm implements IsWidget {
	
	SimplePanel container = new SimplePanel();
	
	ArrayList<JAHPComparison> list;
	
	public RiskEvaluationForm() {
	}
	
	@Override
	public Widget asWidget() {
		return container;
	}
	
	public void loadValues( String goal, ArrayList<JAHPComparison> list ) {
		if( container.getWidget() != null ) {
			container.getWidget().removeFromParent();
		}
		this.list = list;
		
		PreferenceMatrix grid = new PreferenceMatrix( list );

		DockPanel dock = new DockPanel();
		dock.add( new Label( "Preference matrix for goal '" + goal + "'" ), DockPanel.NORTH );
		dock.add( grid, DockPanel.CENTER );
		
		container.setWidget( dock );
	}
	
}
