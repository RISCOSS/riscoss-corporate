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
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecMissingData;
import eu.riscoss.client.codec.CodecRiskData;
import eu.riscoss.client.entities.EntityPropertyPage;
import eu.riscoss.client.ras.RASModule;
import eu.riscoss.client.report.RiskAnalysisReport;
import eu.riscoss.client.rma.RMAModule;
import eu.riscoss.client.ui.FramePanel;
import eu.riscoss.shared.JMissingData;

public class RASPanel implements IsWidget {
	
	SimplePanel 		panel = new SimplePanel();
	private String 		selectedRAS;
	RiskAnalysisReport 	report = new RiskAnalysisReport();
	VerticalPanel		 inputTable = new VerticalPanel();
	HorizontalPanel		mainChart = new HorizontalPanel();
	JsonRiskAnalysis	sessionSummary;		
	
	private String		rasName;
	private String 		riskConf;
	private String		entity;
	
	RiskAnalysisWizard 	risk = null;
	
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
	
	RASModule 		rasModule = null;
	
	public void setBrowse(RASModule ras) {
		if (ras != null) rasModule = ras;
	}
	
	EntityPropertyPage 	eppg = null;
	Boolean entityB;
	
	public void setEppg(EntityPropertyPage eppg, Boolean entityB) {
		if (eppg != null) this.eppg = eppg;
		this.entityB = entityB;
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
	HorizontalPanel buttons;
	HorizontalPanel buttons2;

	protected void loadRASSummary( JsonRiskAnalysis ras ) {
		sessionSummary = ras;
		if( panel.getWidget() != null ) {
			panel.getWidget().removeFromParent();
		}
		riskConf = ras.getRC();
		entity = ras.getTarget();
		rasName = ras.getName();
		
		vPanel = new VerticalPanel();
		Grid grid = new Grid(4,4);
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
		
		Label rcL = new Label("Risk configuration");
		rcL.setStyleName("headTable");
		rcL.setWidth("130px");
		grid.setWidget(1, 0, rcL);
		Label rasrc = new Label(ras.getRC());
		rasrc.setStyleName("contentTable");
		grid.setWidget(1, 1, rasrc);
		
		Label eL = new Label("Target entity");
		eL.setStyleName("headTable");
		eL.setWidth("130px");
		grid.setWidget(2, 0, eL);
		Label rasEntity = new Label(ras.getTarget());
		rasEntity.setStyleName("contentTable");
		grid.setWidget(2, 1, rasEntity);

		{
			
			Button updateIndicators = new Button("Run data collectors");
			updateIndicators.setStyleName("deleteButton");
			updateIndicators.addClickHandler(new ClickHandler() {
				@Override
				public void onClick( ClickEvent event ) {
					onUpdatedIndicatorsClicked();
				}
			});
			
		}
		{

			Label aL = new Label("Execution time");
			aL.setStyleName("headTable");
			aL.setWidth("130px");
			grid.setWidget(3, 0, aL);
			Label lastEx = new Label(ras.getDate());
			lastEx.setStyleName("contentTable");
			grid.setWidget(3, 1, lastEx);
		}
		
		generateButtons();
		
		vPanel.add(grid);
		
		VerticalPanel buttonPanel = new VerticalPanel();
		
		buttons = new HorizontalPanel();
		buttons2 = new HorizontalPanel();
		HorizontalPanel empty = new HorizontalPanel();
		HorizontalPanel empty2 = new HorizontalPanel();
		
		buttons.addStyleName("margin-top");
		buttons2.setStyleName("margin-top");
		empty.setWidth("12px");
		empty2.setWidth("12px");
		
		//If RASPanel placed in multi-layer analysis
		if (risk != null) {
			buttons.add(risk.getBack());
			buttons.add(remove);
			buttons.add(empty);
			buttons.add(mitigation);
			buttons2.add(update);
			buttons2.add(run);
			buttons2.add(empty2);
			buttons2.add(backupUpdate);
			buttons2.add(backupRun);
		}
		//If RASPanel placed in browse ras
		else if (rasModule != null) {
			buttons.add(browseBack);
			buttons.add(browseDelete);
			buttons.add(empty);
			buttons.add(mitigation);
			buttons2.add(update);
			buttons2.add(run);
			buttons2.add(empty2);
			buttons2.add(backupUpdate);
			buttons2.add(backupRun);
		}
		//If RASPanel is in entity
		else if (eppg != null && entityB) {
			buttons.add(entityBack);
			buttons.add(entityDelete);
			buttons.add(empty);
			buttons.add(mitigation);
			buttons2.add(update);
			buttons2.add(run);
			buttons2.add(empty2);
			buttons2.add(backupUpdate);
			buttons2.add(backupRun);
		}
		//If RASPanel is in layer
		else {
			buttons.add(layerBack);
			buttons.add(layerDelete);
			buttons.add(empty);
			buttons.add(mitigation);
			buttons2.add(update);
			buttons2.add(run);
			buttons2.add(empty2);
			buttons2.add(backupUpdate);
			buttons2.add(backupRun);
		}
		buttonPanel.add(buttons);
		buttonPanel.add(buttons2);
		vPanel.add(buttonPanel);
		Label resultsTitle = new Label("Risk analysis results");
		resultsTitle.setStyleName("subtitle");
		vPanel.add(resultsTitle);
		vPanel.add(mainChart);
		vPanel.add(report.asWidget());
		
		panel.setWidget( vPanel );
		
		RiscossJsonClient.getSessionResults( selectedRAS, new JsonCallback() {
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( "" + exception );
			}
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				try {
						report.showResults( 
								sessionSummary,
								response.isObject().get( "results" ).isArray(),
								response.isObject().get( "argumentation" ) );
						if (report.getEvidence()) {
							Label inputValues = new Label("Input values");
							inputValues.setStyleName("subtitle");
							vPanel.add(inputValues);
							vPanel.add(inputTable);
							inputDataInfo(response.isObject().get( "input" ));
						}
				}
				catch( Exception ex ) {
//					Window.alert( ex.getMessage() + "\n" + response );
				}
			}
			
		} );
		
	}
	
	Button		update;
	Button		run;
	Button		backupUpdate;
	Button		backupRun;
	Button		remove;
	Button		browseBack;
	Button 		browseDelete;
	Button		entityBack;
	Button		entityDelete;
	Button  	layerBack;
	Button		layerDelete;
	Button		save;
	Button		backupSave;
	Button		mitigation;
	
	protected void generateButtons() {
		
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
		
		browseBack = new Button("Back");
		browseBack.setStyleName("deleteButton");
		browseBack.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				rasModule.back();
			}
		});
		
		browseDelete = new Button("Delete");
		browseDelete.setStyleName("deleteButton");
		browseDelete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				RiscossJsonClient.deleteRiskAnalysisSession(sessionSummary.getID(), new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert(exception.getMessage());
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						rasModule.back();
					}
				});
			}
		});
		
		entityBack = new Button("Back");
		entityBack.setStyleName("deleteButton");
		entityBack.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				eppg.back(entityB);
			}
		});
		
		entityDelete = new Button("Delete");
		entityDelete.setStyleName("deleteButton");
		entityDelete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				eppg.delete(sessionSummary.getID(), entityB);
			}
		});
		
		layerBack = new Button("Back");
		layerBack.setStyleName("deleteButton");
		layerBack.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				eppg.back(entityB);
			}
		});
		
		layerDelete = new Button("Delete");
		layerDelete.setStyleName("deleteButton");
		layerDelete.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				eppg.delete(sessionSummary.getID(), entityB);
			}
		});
		
		save = new Button("Save & Run");
		save.setStyleName("deleteButton");
		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				CodecRiskData crd = GWT.create( CodecRiskData.class );
				JSONValue values = crd.encode( inputForm.getValueMap() );
				RiscossJsonClient.setAnalysisMissingData(selectedRAS, values, new JsonCallback() {
					@Override
					public void onSuccess( Method method, JSONValue response ) {
						onUpdatedIndicatorsClicked();
					}
					@Override
					public void onFailure( Method method, Throwable exception ) {
						Window.alert( exception.getMessage() );
					}
				});
			}
		});
		
		backupSave = new Button("Backup, Save & Run");
		backupSave.setStyleName("deleteButton");
		backupSave.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				onBackupSaveClicked();
			}
		});
		
		mitigation = new Button("Apply mitigation");
		mitigation.setStyleName("deleteButton");
		mitigation.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				applyMitigation();	
			}
		});
	}
	
	Label running = new Label("  Running...");
	
	public void reloadPanel() {
		buttons2.add(running);
		RiscossJsonClient.rerunRiskAnalysisSession(selectedRAS, "", new RiscossJsonClient.JsonWaitWrapper(
				new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
						report.showResults( 
								sessionSummary,
								response.isObject().get( "results" ).isArray(),
								response.isObject().get( "argumentation" ) );
						buttons2.remove(running);
					
					if (report.getEvidence()) inputDataInfo(response.isObject().get( "input" ));
				}
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		} ) );
	}

	protected void onUpdatedIndicatorsClicked() {
		RiscossJsonClient.updateSessionData(selectedRAS, new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				onUpdateIsClicked();
			}
			
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	MultiLayerInputForm inputForm;
	
	protected void inputDataInfo(JSONValue input) {
		CodecMissingData codec = GWT.create( CodecMissingData.class );
		JMissingData md = codec.decode( input );
		inputForm = new MultiLayerInputMatrix();
		inputForm.load(md);
		inputTable.clear();
		inputTable.add(inputForm);
		HorizontalPanel buttons = new HorizontalPanel();
		buttons.add(save);
		buttons.add(backupSave);
		inputTable.add(buttons);
	}

	
	protected void onUpdateIsClicked() {
		RiscossJsonClient.updateSessionData(selectedRAS, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				buttons2.add(running);
				RiscossJsonClient.rerunRiskAnalysisSession(selectedRAS, "", new RiscossJsonClient.JsonWaitWrapper(
						new JsonCallback() {
					@Override
					public void onSuccess( Method method, JSONValue response ) {
//						Window.alert( "" + response );
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
							report.showResults( 
									sessionSummary,
									response.isObject().get( "results" ).isArray(),
									response.isObject().get( "argumentation" ) );
							
							if (report.getEvidence()) inputDataInfo(response.isObject().get( "input" ));
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
		});
	}
	
	protected void onRunIsClicked() {
		RiscossJsonClient.runRDCs(entity, new JsonCallback() {
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
		buttons2.add(running);
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
				if (report.getEvidence()) {
					CodecRiskData crd = GWT.create( CodecRiskData.class );
					newValues = crd.encode( inputForm.getValueMap() );
					RiscossJsonClient.setAnalysisMissingData(id, newValues, new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert(exception.getMessage());
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
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
				else {
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
			}
		});
	}
	
	protected void onBackupRunClicked() {
		buttons2.add(running);
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
				if (report.getEvidence()) {
					CodecRiskData crd = GWT.create( CodecRiskData.class );
					newValues = crd.encode( inputForm.getValueMap() );
					RiscossJsonClient.setAnalysisMissingData(id, newValues, new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert(exception.getMessage());
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							RiscossJsonClient.runRDCs(entity, new JsonCallback() {
								@Override
								public void onFailure(Method method, Throwable exception) {
									Window.alert(exception.getMessage());
								}
								@Override
								public void onSuccess(Method method, JSONValue response) {
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
					});
				}
				else {
					RiscossJsonClient.runRDCs(entity, new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert(exception.getMessage());
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
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
			}
		});
	}
	
	JSONValue newValues;
	
	protected void onBackupSaveClicked() {
		CodecRiskData crd = GWT.create( CodecRiskData.class );
		newValues = crd.encode( inputForm.getValueMap() );
		buttons2.add(running);
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
				RiscossJsonClient.setAnalysisMissingData(id, newValues, new JsonCallback() {
					@Override
					public void onSuccess( Method method, JSONValue response ) {
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
					@Override
					public void onFailure( Method method, Throwable exception ) {
						Window.alert( exception.getMessage() );
					}
				});
			}
		});
	}
	
	private void applyMitigation() {
		FramePanel p = new FramePanel("rma.jsp?id=" + selectedRAS);
		RootPanel.get().clear();
		RootPanel.get().add(p.getWidget());
		p.activate();
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
