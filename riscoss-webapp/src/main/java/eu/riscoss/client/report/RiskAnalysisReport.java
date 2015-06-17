package eu.riscoss.client.report;

import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.JsonRiskResult;
import eu.riscoss.client.ui.GaugeImage;

public class RiskAnalysisReport implements IsWidget {
	
	SimplePanel panel = new SimplePanel();
	
	@Override
	public Widget asWidget() {
		return panel;
	}
	
	public void showResults( JSONArray response ) {
		
		if( panel.getWidget() != null ) {
			panel.getWidget().removeFromParent();
		}
		
		Grid grid = new Grid();
		for( int i = 0; i < response.isArray().size(); i++ ) {
			JSONObject v = response.isArray().get( i ).isObject();
			JsonRiskResult result = new JsonRiskResult( v );
//			grid.insertRow( grid.getRowCount() );
			switch( result.getDataType() ) {
			case DISTRIBUTION: {
				grid.resize( grid.getRowCount() +1, 3 );
				
				VerticalPanel panel = new VerticalPanel();
				
				String label = v.get( "id" ).isString().stringValue();
				if( v.get( "name" ) != null ) {
					if( v.get( "name" ).isString() != null ) {
						label = v.get( "name" ).isString().stringValue();
					}
				}
				panel.add( new Label( label ) );
				grid.setWidget( i, 0, panel );
				
				GaugeImage img = new GaugeImage();
				img.setDistribution( result.getDistributionString() );
				grid.setWidget( i, 1, img );
				
				grid.setWidget( i, 2, new Label( result.getDescription() ) );
			}
				break;
			case EVIDENCE: {
				grid.resize( grid.getRowCount() +1, 3 );
				VerticalPanel panel = new VerticalPanel();
				panel.add( new Label( v.get( "id" ).isString().stringValue() ) );
				panel.add( new HTML( 
						"Exposure: <font color='red'>" + 
								v.get( "e" ).isObject().get( "e" ).isNumber().doubleValue() +
						"</font>") );
				grid.setWidget( i, 0, panel );
				
				if( "evidence".equals( v.get( "datatype" ).isString().stringValue() ) ) {
					GaugeImage img = new GaugeImage();
					img.setEvidence( v.get( "p" ).isString().stringValue(), v.get( "m" ).isString().stringValue() );
					grid.setWidget( i, 1, img );
				}
				grid.setWidget( i, 2, new Label( v.get( "description" ).isString().stringValue() ) );
			}
				break;
			case INTEGER:
			case NaN:
			case REAL:
			case STRING:
			default:
				break;
			}
		}
		
		panel.setWidget( grid );
	}
}
