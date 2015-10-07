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

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecMissingData;
import eu.riscoss.client.report.RiskAnalysisReport;
import eu.riscoss.shared.JMissingData;

public class RASPanel implements IsWidget {
	
	SimplePanel panel = new SimplePanel();
	private String selectedRAS;
	RiskAnalysisReport report = new RiskAnalysisReport();
	
	public RASPanel() {
		
	}
	
	@Override
	public Widget asWidget() {
		return panel;
	}
		
	public void loadRAS( String selectedRAS ) {
		this.selectedRAS = selectedRAS;
		RiscossJsonClient.getSessionSummary( selectedRAS, new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				loadRASSummary( new JsonRiskAnalysis( response ) );
			}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}

	protected void loadRASSummary( JsonRiskAnalysis ras ) {
		
		if( panel.getWidget() != null ) {
			panel.getWidget().removeFromParent();
		}
		
		Grid grid = new Grid(7,2);
		
		Label nL = new Label("Name");
		nL.setStyleName("bold");
		grid.setWidget(0, 0, nL);
		grid.setWidget(0, 1, new Label(ras.getName()));
		
		Label idL = new Label("ID");
		idL.setStyleName("bold");
		grid.setWidget(1, 0, idL);
		grid.setWidget(1, 1, new Label(ras.getID()));
		
		Label rcL = new Label("Risk configuration");
		rcL.setStyleName("bold");
		grid.setWidget(2, 0, rcL);
		grid.setWidget(2, 1, new Label(ras.getRC()));
		
		Label eL = new Label("Target entity");
		eL.setStyleName("bold");
		grid.setWidget(3, 0, eL);
		grid.setWidget(3, 1, new Label(ras.getTarget()));

//		grid.add( "Last execution:", new Label( ras.getDate() ) );
//		grid.add( "Action:", new RadioButton( "action", "Run" ) );
		{
			HorizontalPanel hp = new HorizontalPanel();
			hp.add( new Label( "Last update: -" ) );
			hp.add( new Button( "Update now", new ClickHandler() {
				@Override
				public void onClick( ClickEvent event ) {
					onUpdatedIndicatorsClicked();
				}
			} ) );
			hp.add( new Button( "Edit missing values", new ClickHandler() {
				@Override
				public void onClick( ClickEvent event ) {
					onEditMissingValues();
				}
			} ) );
			hp.setWidth( "100%" );
			
			Label nI = new Label("Indicators");
			nI.setStyleName("bold");
			grid.setWidget(4, 0, nI);
			grid.setWidget(4, 1, hp);
		}
		{
			HorizontalPanel hp = new HorizontalPanel();
			hp.add( new Label( "Last execution: " + ras.getDate()) );
			hp.add( new Button( "Run now", new ClickHandler() {
				@Override
				public void onClick( ClickEvent event ) {
					onRunAnalysisClicked();
				}
			} ) );
			hp.setWidth( "100%" );
			
			Label aL = new Label("Analysis");
			aL.setStyleName("bold");
			grid.setWidget(5, 0, aL);
			grid.setWidget(5, 1, hp);
		}
		
		Label lrL = new Label("Last report");
		lrL.setStyleName("bold");
		grid.setWidget(6, 0, lrL);
		grid.setWidget(6, 1, report.asWidget());
		
		panel.setWidget( grid );
		
		RiscossJsonClient.getSessionResults( selectedRAS, new JsonCallback() {
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( "" + exception );
			}
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				try {
					report.showResults( response.isObject().get( "results" ).isArray() );
				}
				catch( Exception ex ) {
//					Window.alert( ex.getMessage() + "\n" + response );
				}
			}
			
		} );
		
	}
	
	protected void onEditMissingValues() {
		RiscossJsonClient.getAnalysisMissingData(selectedRAS, new JsonCallback() {
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					CodecMissingData codec = GWT.create( CodecMissingData.class );
					JMissingData md = codec.decode( response );
					new MissingDataDialog( md, selectedRAS ).show();
				}
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
			} );
	}

	protected void onUpdatedIndicatorsClicked() {
		RiscossJsonClient.updateSessionData(selectedRAS, new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				Window.alert( "Done" );
			}
			
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}

	protected void onRunAnalysisClicked() {
		RiscossJsonClient.rerunRiskAnalysisSession(selectedRAS, "", new RiscossJsonClient.JsonWaitWrapper(
				new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
//				Window.alert( "" + response );
				try {
					RiscossJsonClient.getSessionSummary( selectedRAS, new JsonCallback() {
						@Override
						public void onSuccess( Method method, JSONValue response ) {
							loadRASSummary( new JsonRiskAnalysis( response ) );
						}
						@Override
						public void onFailure( Method method, Throwable exception ) {
							Window.alert( exception.getMessage() );
						}
					});
					report.showResults( response.isObject().get( "results" ).isArray() );
				}
				catch( Exception ex ) {
					Window.alert( ex.getMessage() + "\n" + response );
				}
			}
			
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		} ) );
	}
		
}
