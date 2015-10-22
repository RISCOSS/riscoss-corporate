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

import java.util.Date;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.python.icu.util.Calendar;

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
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecMissingData;
import eu.riscoss.client.report.RiskAnalysisReport;
import eu.riscoss.shared.JMissingData;

public class RASPanel implements IsWidget {
	
	SimplePanel 		panel = new SimplePanel();
	private String 		selectedRAS;
	RiskAnalysisReport 	report = new RiskAnalysisReport();
	SimplePanel		 	inputTable;
	HorizontalPanel		mainChart = new HorizontalPanel();
	RiskAnalysisWizard 	risk = null;
	JsonRiskAnalysis	sessionSummary;		
	
	private String		rasName;
	private String 		riskConf;
	private String		entity;
	
	public RASPanel(RiskAnalysisWizard w) {
		if (w != null) risk = w;
	}
	
	@Override
	public Widget asWidget() {
		return panel;
	}
	
	public JsonRiskAnalysis getSummary() {
		return this.sessionSummary;
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
	
	VerticalPanel vPanel;

	protected void loadRASSummary( JsonRiskAnalysis ras ) {
		sessionSummary = ras;
		if( panel.getWidget() != null ) {
			panel.getWidget().removeFromParent();
		}
		
		riskConf = ras.getRC();
		entity = ras.getTarget();
		rasName = ras.getName();
		
		vPanel = new VerticalPanel();
		Grid grid = new Grid(6,4);
		grid.setCellSpacing(0);
		grid.setCellPadding(0);
		grid.setStyleName("table");
		
		Label nL = new Label("Name");
		nL.setStyleName("headTable");
		nL.setWidth("130px");
		grid.setWidget(0, 0, nL);
		Label rasName = new Label(ras.getName());
		rasName.setStyleName("contentTable");
		rasName.setHeight("100%");
		grid.setWidget(0, 1, rasName);
		
		Label idL = new Label("ID");
		idL.setStyleName("headTable");
		idL.setWidth("130px");
		grid.setWidget(1, 0, idL);
		Label rasID = new Label(ras.getID());
		rasID.setStyleName("contentTable");
		grid.setWidget(1, 1, rasID);
		
		Label rcL = new Label("Risk configuration");
		rcL.setStyleName("headTable");
		rcL.setWidth("130px");
		grid.setWidget(2, 0, rcL);
		Label rasrc = new Label(ras.getRC());
		rasrc.setStyleName("contentTable");
		grid.setWidget(2, 1, rasrc);
		
		Label eL = new Label("Target entity");
		eL.setStyleName("headTable");
		eL.setWidth("130px");
		grid.setWidget(3, 0, eL);
		Label rasEntity = new Label(ras.getTarget());
		rasEntity.setStyleName("contentTable");
		grid.setWidget(3, 1, rasEntity);

//		grid.add( "Last execution:", new Label( ras.getDate() ) );
//		grid.add( "Action:", new RadioButton( "action", "Run" ) );
		{
			
			Button updateIndicators = new Button("Run data collectors");
			updateIndicators.setStyleName("deleteButton");
			updateIndicators.addClickHandler(new ClickHandler() {
				@Override
				public void onClick( ClickEvent event ) {
					onUpdatedIndicatorsClicked();
				}
			});
			
			
			Label nI = new Label("Data collectors");
			nI.setStyleName("headTable");
			nI.setWidth("130px");
			grid.setWidget(4, 0, nI);
			Label lastUpd = new Label("Last execution: -");
			lastUpd.setStyleName("contentTable");
			grid.setWidget(4, 1, lastUpd);
		}
		{
			//hp.setWidth( "100%" );
			
			Label aL = new Label("Analysis");
			aL.setStyleName("headTable");
			aL.setWidth("130px");
			grid.setWidget(5, 0, aL);
			Label lastEx = new Label("Last execution: " + ras.getDate());
			lastEx.setStyleName("contentTable");
			grid.setWidget(5, 1, lastEx);
		}
		
		generateButtons();
		
		vPanel.add(grid);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.setStyleName("margin-top");
		if (risk != null) {
			buttons.add(risk.getBack());
			buttons.add(missingVal);
			buttons.add(update);
			buttons.add(run);
			buttons.add(backupUpdate);
			buttons.add(backupRun);
			buttons.add(remove);
		}
		else {
			buttons.add(missingVal);
			buttons.add(update);
			buttons.add(run);
			buttons.add(backupUpdate);
			buttons.add(backupRun);
		}
		vPanel.add(buttons);
		Label resultsTitle = new Label("Risk analysis results");
		resultsTitle.setStyleName("subtitle");
		vPanel.add(resultsTitle);
		vPanel.add(mainChart);
		vPanel.add(report.asWidget());
		
		Label inputValues = new Label("Input values");
		inputValues.setStyleName("subtitle");
		//vPanel.add(inputValues);
		//vPanel.add(inputTable);
		
		panel.setWidget( vPanel );
		
		RiscossJsonClient.getSessionResults( selectedRAS, new JsonCallback() {
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( "" + exception );
			}
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				try {
					if (risk != null) {
						report.showResults( 
								sessionSummary,
								response.isObject().get( "results" ).isArray(),
								response.isObject().get( "argumentation" ) );
					}
					else {
						report.showResults(
								response.isObject().get( "results" ).isArray(),
								response.isObject().get( "argumentation" ) );
						
					}
					
					//inputDataInfo(response.isObject().get( "input" ));
				}
				catch( Exception ex ) {
//					Window.alert( ex.getMessage() + "\n" + response );
				}
			}
			
		} );
		
	}
	
	Button		missingVal;
	Button		update;
	Button		run;
	Button		backupUpdate;
	Button		backupRun;
	Button		remove;
	
	protected void generateButtons() {
		
		missingVal = new Button("Edit missing values");
		missingVal.setStyleName("deleteButton");
		missingVal.addClickHandler(new ClickHandler() {
			@Override
			public void onClick( ClickEvent event ) {
				onEditMissingValues();
			}
		});
		
		update = new Button("Update");
		update.setStyleName("deleteButton");
		update.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				onUpdateIsClicked();
			}
		});
		
		run = new Button("Run");
		run.setStyleName("deleteButton");
		run.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				onRunIsClicked();
			}
		});
		
		backupUpdate = new Button("Backup & Update");
		backupUpdate.setStyleName("deleteButton");
		backupUpdate.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				onBackupUpdateClicked();
			}
		});
		
		backupRun = new Button("Backup & Run");
		backupRun.setStyleName("deleteButton");
		backupRun.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				onBackupRunClicked();
			}
		});
		
		remove = new Button("Delete");
		remove.setStyleName("deleteButton");
		remove.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				risk.remove(selectedRAS);
			}
		});
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
	
	protected void inputDataInfo(JSONValue input) {
		CodecMissingData codec = GWT.create( CodecMissingData.class );
		JMissingData md = codec.decode( input );
		MultiLayerInputForm inputForm = new MultiLayerInputMatrixRead();
		inputForm.load(md);
		vPanel.add(inputForm);
	}

	protected void onUpdateIsClicked() {
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
					if (risk != null) {
						report.showResults( 
								sessionSummary,
								response.isObject().get( "results" ).isArray(),
								response.isObject().get( "argumentation" ) );
					}
					else {
						report.showResults(
								response.isObject().get( "results" ).isArray(),
								response.isObject().get( "argumentation" ) );
						
					}
					
					//inputDataInfo(response.isObject().get( "input" ));
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
	
	protected void onRunIsClicked() {
		RiscossJsonClient.updateSessionData(selectedRAS, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				onUpdateIsClicked();
			}
		});
	}
	
	String name;
	String id;
	
	protected void onBackupUpdateClicked() {
		
		String date = getDate();
		name = rasName + " (" + date + ")";
		RiscossJsonClient.creteRiskAnalysisSession(name, riskConf, entity, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				JsonRiskAnalysis ras =  new JsonRiskAnalysis( response );
				id = ras.getID();
				RiscossJsonClient.rerunRiskAnalysisSession(id, "", new JsonCallback() {

					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert(exception.getMessage());
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						loadRAS(id);
						risk.generateRiskTree();
						risk.setTitle(name);
					}
				});
			}
		});
	}
	
	protected void onBackupRunClicked() {
		String date = getDate();
		name = rasName + " (" + date + ")";
		RiscossJsonClient.creteRiskAnalysisSession(name, riskConf, entity, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				JsonRiskAnalysis ras =  new JsonRiskAnalysis( response );
				id = ras.getID();
				RiscossJsonClient.updateSessionData(id, new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert(exception.getMessage());
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						RiscossJsonClient.rerunRiskAnalysisSession(id, "", new JsonCallback() {

							@Override
							public void onFailure(Method method, Throwable exception) {
								Window.alert(exception.getMessage());
							}
							@Override
							public void onSuccess(Method method, JSONValue response) {
								loadRAS(id);
								risk.generateRiskTree();
								risk.setTitle(name);
							}
						});
					}
				});
			}
		});
	}
	
	private String getDate() {
		Date c = new Date();
		int y = c.getYear() + 1900;
		int m = c.getMonth() + 1;
		if (m == 13) {
			m = 1;
			++y;
		}
		return c.getDate() + "-" + m + "-" + y + " " + c.getHours() + "." + c.getMinutes() + "." + c.getSeconds();
	}
		
}
