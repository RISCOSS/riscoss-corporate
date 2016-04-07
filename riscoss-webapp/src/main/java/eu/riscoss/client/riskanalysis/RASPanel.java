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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.util.Date;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.fop.apps.FOPException;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossCall;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecMissingData;
import eu.riscoss.client.codec.CodecRASInfo;
import eu.riscoss.client.codec.CodecRiskData;
import eu.riscoss.client.entities.EntityPropertyPage;
import eu.riscoss.client.ras.RASModule;
import eu.riscoss.client.report.RiskAnalysisReport;
import eu.riscoss.client.report.RiskAnalysisResults;
import eu.riscoss.shared.JMissingData;
import eu.riscoss.shared.JRASInfo;

public class RASPanel implements IsWidget {
	
	SimplePanel 		panel = new SimplePanel();
	private String 		selectedRAS;
//	RiskAnalysisReport 	report = new RiskAnalysisReport();
	VerticalPanel		 inputTable = new VerticalPanel();
	HorizontalPanel		mainChart = new HorizontalPanel();
	JsonRiskAnalysis	sessionSummary;		
	
	//NEW RASPANEL
	RiskAnalysisResults r = new RiskAnalysisResults();
	
	private String		rasName;
	private String 		riskConf;
	private String		entity;
	private String 		date;
	private String		RASID;
	Date currentDate;
	
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
	TextBox 		rasNameBox;
	Label 			title;

	protected void loadRASSummary( JsonRiskAnalysis ras ) {
		sessionSummary = ras;
		if( panel.getWidget() != null ) {
			panel.getWidget().removeFromParent();
		}
		riskConf = ras.getRC();
		entity = ras.getTarget();
		rasName = ras.getName();
		date = ras.getDate();
		RASID = ras.getID();
		currentDate = getDate(date);
		
		vPanel = new VerticalPanel();
		vPanel.setWidth("100%");
		title = new Label(rasName);
		title.setStyleName("h1");
		title.setWidth("100%");
		vPanel.add(title);
		Grid grid = new Grid(4,4);
		grid.setCellSpacing(0);
		grid.setCellPadding(0);
		grid.setStyleName("table");
		
		Label nL = new Label("Name");
		nL.setStyleName("headTable");
		nL.setWidth("130px");
		grid.setWidget(0, 0, nL);
		/*Label rasName = new Label(ras.getName());
		rasName.setStyleName("contentTable");
		rasName.setHeight("100%");
		grid.setWidget(0, 1, rasName);*/
		rasNameBox = new TextBox();
		rasNameBox.setText(ras.getName());
		rasNameBox.setWidth("97%");
		grid.setWidget(0, 1, rasNameBox);
		
		
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
		HorizontalPanel empty3 = new HorizontalPanel();
		
		buttons.addStyleName("margin-top");
		buttons2.setStyleName("margin-top");
		empty.setWidth("12px");
		empty2.setWidth("12px");
		empty3.setWidth("12px");
		
		//If RASPanel placed in multi-layer analysis
		if (risk != null) {
			buttons.add(risk.getBack());
			buttons.add(saveRas);
			buttons.add(remove);
			buttons.add(empty);
			buttons.add(mitigation);
			buttons.add(whatIf);
			buttons.add(empty3);
			buttons.add(generateReport);
			buttons2.add(update);
			buttons2.add(run);
			buttons2.add(empty2);
			buttons2.add(backupUpdate);
			buttons2.add(backupRun);
		}
		//If RASPanel placed in browse ras
		else if (rasModule != null) {
			buttons.add(browseBack);
			buttons.add(saveRas);
			buttons.add(browseDelete);
			buttons.add(empty);
			buttons.add(mitigation);
			buttons.add(whatIf);
			buttons.add(empty3);
			buttons.add(generateReport);
			buttons2.add(update);
			buttons2.add(run);
			buttons2.add(empty2);
			buttons2.add(backupUpdate);
			buttons2.add(backupRun);
		}
		//If RASPanel is in entity
		else if (eppg != null && entityB) {
			buttons.add(entityBack);
			buttons.add(saveRas);
			buttons.add(entityDelete);
			buttons.add(empty);
			buttons.add(mitigation);
			buttons.add(whatIf);
			buttons.add(empty3);
			buttons.add(generateReport);
			buttons2.add(update);
			buttons2.add(run);
			buttons2.add(empty2);
			buttons2.add(backupUpdate);
			buttons2.add(backupRun);
		}
		//If RASPanel is in layer
		else {
			buttons.add(layerBack);
			buttons.add(saveRas);
			buttons.add(layerDelete);
			buttons.add(empty);
			buttons.add(mitigation);
			buttons.add(whatIf);
			buttons.add(empty3);
			buttons.add(generateReport);
			buttons2.add(update);
			buttons2.add(run);
			buttons2.add(empty2);
			buttons2.add(backupUpdate);
			buttons2.add(backupRun);
		}
		buttonPanel.add(buttons);
		buttonPanel.add(buttons2);
		checkLastRiskSession();
		vPanel.add(buttonPanel);
		Label resultsTitle = new Label("Risk analysis results");
		resultsTitle.setStyleName("subtitle");
		vPanel.add(resultsTitle);
		
		vPanel.add(r);
		vPanel.add(mainChart);
//		vPanel.add(report.asWidget());
		
		panel.setWidget( vPanel );
		
		RiscossJsonClient.getSessionResults( selectedRAS, new JsonCallback() {
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( "" + exception );
			}
			@Override
			public void onSuccess( Method method, JSONValue response ) {
				try {
						
							vPanel.remove(mainChart);
//							vPanel.remove(report.asWidget());
							r.showResults(sessionSummary, 
								response.isObject());

						if (r.getEvidence()) {
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
		checkIfEntityExists();
		
	}
	
	private void checkIfEntityExists() {
		RiscossJsonClient.listEntities(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				boolean b = false;
				for(int i=0; i<response.isArray().size(); i++){
					JSONObject o = (JSONObject)response.isArray().get(i);
					if (o.get("name").isString().stringValue().equals(entity)) b = true;
				}
				if (!b) {
					buttons2.clear();
					buttons.remove(mitigation);
					if (inputButtons != null) inputButtons.clear();
				}
			}
		});
	}

	String mostCurrentSession;
	
	public void checkLastRiskSession() {
		RiscossJsonClient.listRiskAnalysisSessions(entity, riskConf, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				if( response == null ) return;
				if( response.isObject() == null ) return;
				response = response.isObject().get( "list" );
				CodecRASInfo codec = GWT.create( CodecRASInfo.class );
				if( response.isArray() != null ) {
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JRASInfo info = codec.decode( response.isArray().get( i ) );
						RiscossJsonClient.getSessionSummary(info.getId(), new JsonCallback(){
							@Override
							public void onFailure(Method method, Throwable exception) {
								Window.alert(exception.getMessage());
							}
							@Override
							public void onSuccess(Method method, JSONValue response) {
								JsonRiskAnalysis ras = new JsonRiskAnalysis( response );
								Date d = getDate(ras.getDate());
								if (d.after(currentDate) && !ras.getID().equals(RASID)) {
									mostCurrentSession = ras.getID();
									buttons2.clear();
									buttons.remove(mitigation);
									if (inputButtons != null) inputButtons.clear();
									Label l = new Label("Current session is not the last one.");
									Anchor a = new Anchor("Click here to see last execution results.");
									buttons2.add(l);
									buttons2.add(a);
									a.addClickHandler(new ClickHandler() {
										@Override
										public void onClick(ClickEvent arg0) {
											Window.Location.replace("riskanalysis.jsp?id=" + mostCurrentSession);
										}
									});
								}
							}
						});
					}
				}
			}
		});
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
	Button 		saveRas;
	Button 		whatIf;
	Button 		generateReport;
	
	protected void generateButtons() {
		
		saveRas = new Button("Save");
		saveRas.setStyleName("deleteButton");
		saveRas.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveRASData();	
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
				Boolean b = Window.confirm("Are you sure that you want to delete risk session " + rasName + "?");
				if (b) {
					String or = Window.Location.getParameter("or");
					if (or == null) {
						risk.remove(selectedRAS);
					}
					else Window.Location.replace("dashboard.jsp");
				}
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
				Boolean b = Window.confirm("Are you sure that you want to delete risk session " + rasName + "?");
				if (b) {
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
				Boolean b = Window.confirm("Are you sure that you want to delete risk session " + rasName + "?");
				if (b)
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
				Boolean b = Window.confirm("Are you sure that you want to delete risk session " + rasName + "?");
				if (b)
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
				Log.println(values.toString());
				RiscossJsonClient.setAnalysisMissingData(selectedRAS, values, new JsonCallback() {
					@Override
					public void onSuccess( Method method, JSONValue response ) {
						onSaveAndRunClicked();
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
		
		whatIf = new Button("What-If");
		whatIf.setStyleName("deleteButton");
		whatIf.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				whatIfAnalysis();
			}
		});
		
		generateReport = new Button("Generate report");
		generateReport.setStyleName("deleteButton");
		generateReport.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				generateReport();
			}
		});
	}
	
	Label running = new Label("  Running...");
	
	public void saveRASData() {
		title.setText(rasNameBox.getText());
		RiscossJsonClient.renameRiskAnalysisSession(RASID, rasNameBox.getText(), new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				
			}
		});
	}
	
	public void onSaveAndRunClicked() {
		buttons2.add(running);
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
//						report = new RiskAnalysisReport();
						r.showResults(sessionSummary, 
							response.isObject());
					
					if (r.getEvidence()) inputDataInfo(response.isObject().get( "input" ));
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
	
	public void reloadPanel() {
		buttons2.add(running);
		RiscossJsonClient.rerunRiskAnalysisSession(selectedRAS, "", new RiscossJsonClient.JsonWaitWrapper(
				new JsonCallback() {
			@Override
			public void onSuccess( Method method, JSONValue response ) {
					
//						report = new RiskAnalysisReport();
						r.showResults(sessionSummary, 
							response.isObject());
						buttons2.remove(running);
					
					if (r.getEvidence()) inputDataInfo(response.isObject().get( "input" ));
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
	HorizontalPanel inputButtons;
	
	protected void inputDataInfo(JSONValue input) {
		CodecMissingData codec = GWT.create( CodecMissingData.class );
		JMissingData md = codec.decode( input );
		inputForm = new MultiLayerInputMatrix();
		inputForm.load(md);
		inputTable.clear();
		inputTable.add(inputForm);
		inputButtons = new HorizontalPanel();
		inputButtons.add(save);
		inputButtons.add(backupSave);
		inputTable.add(inputButtons);
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
//								report = new RiskAnalysisReport();
								r.showResults(sessionSummary, 
									response.isObject());
							
							if (r.getEvidence()) inputDataInfo(response.isObject().get( "input" ));
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
		String split[] = rasName.split("\\(");
		if (split.length > 0 )name = split[0] + "(" + date + ")";
		else name = rasName + "(" + date + ")";
		RiscossJsonClient.creteRiskAnalysisSession(name, riskConf, entity, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				JsonRiskAnalysis ras =  new JsonRiskAnalysis( response );
				id = ras.getID();
				if (r.getEvidence()) {
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
		String split[] = rasName.split("\\(");
		if (split.length > 0 )name = split[0] + "(" + date + ")";
		else name = rasName + "(" + date + ")";
		RiscossJsonClient.creteRiskAnalysisSession(name, riskConf, entity, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				JsonRiskAnalysis ras =  new JsonRiskAnalysis( response );
				id = ras.getID();
				if (r.getEvidence()) {
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
		String split[] = rasName.split("\\(");
		if (split.length > 0 )name = split[0] + "(" + date + ")";
		else name = rasName + "(" + date + ")";
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
					@Override
					public void onFailure( Method method, Throwable exception ) {
						Window.alert( exception.getMessage() );
					}
				});
			}
		});
	}
	
	private void applyMitigation() {
//		RiscossWebApp.loadPanel("rma.jsp?id=" + selectedRAS);
//		FramePanel p = new FramePanel("rma.jsp?id=" + selectedRAS);
//		RootPanel.get().clear();
//		RootPanel.get().add(p.getWidget());
//		p.activate();
		Window.Location.replace("rma.jsp?id=" + selectedRAS);
	}
	
	private void whatIfAnalysis() {
		List<String> entities = r.getEntities();
		String s = "";
		for (String e : entities) s = s + e + "@";
		//String s = entity;
		Window.Location.replace("whatifanalysis.jsp?id=" + selectedRAS + "&entities=" + s);
	}
	
	private void generateReport() {
		/*RiscossJsonClient.generateReport(selectedRAS, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
	            
			}
		});*/
		String url = GWT.getHostPageBaseURL() + "analysis/download?domain=" + RiscossJsonClient.getDomain() + "&name="+ rasName+"&rasId=" + selectedRAS +"&type=rasHTML&token="+RiscossCall.getToken();
		Window.open(url, "", "");
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
	
	private Date getDate (String date) {
		String info[] = date.split(" ");
		String day[] = info[0].split("-");
		String hour[] = info[1].split("\\.");
		Date d = new Date();
		d.setDate(Integer.parseInt(day[0]));
		d.setMonth(Integer.parseInt(day[1])-1);
		d.setYear(Integer.parseInt(day[2]));
		d.setHours(Integer.parseInt(hour[0]));
		d.setMinutes(Integer.parseInt(hour[1]));
		d.setSeconds(Integer.parseInt(hour[2]));
		return d;
		
	}
		
}
