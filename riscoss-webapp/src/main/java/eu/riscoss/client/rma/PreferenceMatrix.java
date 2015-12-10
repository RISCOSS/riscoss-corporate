package eu.riscoss.client.rma;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.kiouri.sliderbar.client.event.BarValueChangedEvent;
import com.kiouri.sliderbar.client.event.BarValueChangedHandler;
import com.kiouri.sliderbar.client.solution.adv.AdvancedSliderBar;
import com.kiouri.sliderbar.client.view.SliderBar;

import eu.riscoss.shared.JAHPComparison;


public class PreferenceMatrix implements IsWidget {

	static class ComparisonRow {
		
		JAHPComparison c;
		
		Label leftWidget;
		Label rightWidget;
		SliderBar sb;
		
		ComparisonRow( JAHPComparison c ) {
			this.c = c;
			leftWidget = new Label( c.getId1() );
			rightWidget = new Label( c.getId2() );
			sb = new AdvancedSliderBar();
			sb.setNotSelectedInFocus();
			sb.setMaxValue( 8 );
			sb.setValue( c.value );
			sb.addBarValueChangedHandler( new BarValueChangedHandler() {
				public void onBarValueChanged(BarValueChangedEvent event) {
					ComparisonRow.this.c.value = sb.getValue();
				}
			});
		}
		
		public Widget getLeftWidget() {
			return this.leftWidget;
		}
		public Widget getSlider() {
			return this.sb;
		}
		public Widget getRightWidget() {
			return rightWidget;
		}
	}
	
	SimplePanel container = new SimplePanel();
	
	ArrayList<JAHPComparison> preferences;
	
	@Override
	public Widget asWidget() {
		return container;
	}
	
	public PreferenceMatrix( ArrayList<JAHPComparison> list ) {
		loadValues( list );
	}
	
	public void loadValues( ArrayList<JAHPComparison> list ) {
		if( container.getWidget() != null ) {
			container.getWidget().removeFromParent();
		}
		Grid grid = new Grid( 0, 3 );
		int i = 0;
		for( JAHPComparison c : list ) {
			if( c.getId1().equals( c.getId2() ) ) continue;
			grid.resize( grid.getRowCount() +1, 3 );
			
			ComparisonRow row = new ComparisonRow( c );
			grid.setWidget( i, 0, row.getLeftWidget() );
			grid.setWidget( i, 1, row.getSlider() );
			grid.setWidget( i, 2, row.getRightWidget() );
			
			i++;
		}
		this.preferences = list;
		container.setWidget( grid );
	}
	
	public ArrayList<JAHPComparison> getValues() {
		return this.preferences;
	}
	
	public void insertRow (JAHPComparison c) {
		Grid grid = (Grid) container.getWidget();
		int i = grid.getRowCount();
		grid.resize(grid.getRowCount() + 1, 3);
		
		ComparisonRow row = new ComparisonRow( c );
		grid.setWidget( i, 0, row.getLeftWidget() );
		grid.setWidget( i, 1, row.getSlider() );
		grid.setWidget( i, 2, row.getRightWidget() );
	}
	
}
