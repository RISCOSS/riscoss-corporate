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

import java.util.ArrayList;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.JsonEntitySummary;
import eu.riscoss.client.ModelInfo;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecRASInfo;
import eu.riscoss.client.ui.TreeWidget;
import eu.riscoss.shared.JRASInfo;

public class RiskAnalysisWizard implements EntryPoint {
	
	DockPanel dock			= new DockPanel();
	
	EntitySelectionPanel	entitySelectionPanel = new EntitySelectionPanel();
	RCSelectionPanel		rcSelectionPanel = new RCSelectionPanel();
	RASSelectionPanel		rasSelectionPanel = new RASSelectionPanel();
	RASPanel				rasPanel = new RASPanel();
	
	HorizontalPanel			topPanel = new HorizontalPanel();
//	HorizontalPanel			bottomPanel = new HorizontalPanel();
	
	VerticalPanel			page = new VerticalPanel();
	HorizontalPanel			mainView = new HorizontalPanel();
	VerticalPanel			leftPanel = new VerticalPanel();
	VerticalPanel			rightPanel = new VerticalPanel();
	HorizontalPanel			top = new HorizontalPanel();
	Grid 					g;
	
	TreeWidget				entitiesTree = new TreeWidget();
	TreeWidget				root = new TreeWidget();
	
	TreeWidget				riskTree = new TreeWidget();
	
	String 					selectedEntity;
	String					selectedRiskConf;
	String					selectedRiskSession;
	
	Button					newRiskSession;
	
	Button 					back;
	Button					remove;
	
	Label 					entitiesL;
	Label					riskL;
	
//	Wizard					wizard = new Wizard();
	
	String					idRerun;
	String 					newId;
	
	Label 					title;
	
	public void onModuleLoad() {
		
		try {
			
			mainView.setStyleName("mainViewLayer");
			mainView.setWidth("100%");
			page.setWidth("100%");
			leftPanel.setStyleName("leftPanelLayer");
			leftPanel.setWidth("100%");
			rightPanel.setStyleName("leftPanelLayer");
			rightPanel.setWidth("400px");
			//leftPanel.setHeight("100%");
			
			title = new Label("Multi-layer Analysis");
			title.setStyleName("title");
			page.add(title);
			
			g = new Grid(3, 3);
			g.setStyleName("gridRisk");
			
			Label entityL = new Label("Selected Entity");
			entityL.setStyleName("bold");
			g.setWidget(0, 0, entityL);
			
			g.setWidget(0, 1, new Label(" - "));
			
			Label riskL = new Label("Selected Risk Conf");
			riskL.setStyleName("bold");
			g.setWidget(1, 0, riskL);
			
			g.setWidget(1, 1, new Label(" - "));
			
			top.add(g);
			page.add(top);
			
			generateEntitiesTree();
			entitiesL = new Label("Entities");
			entitiesL.setStyleName("smallTitle");
			entitiesL.setWidth("100%");
			leftPanel.add(entitiesL);
			leftPanel.add(entitiesTree);
			mainView.add(leftPanel);
			mainView.add(rightPanel);
			
			page.add(mainView);
			RootPanel.get().add(page);
			
			newRiskSession = new Button("New risk session");
			newRiskSession.setStyleName("deleteButton");
			newRiskSession.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					String n = newName.getText().trim();
					if( "".equals( n ) ) return;
					RiscossJsonClient.creteRiskAnalysisSession(n, selectedRiskConf, selectedEntity, new JsonCallback() {
						@Override
						public void onFailure( Method method, Throwable exception ) {
							Window.alert( exception.getMessage() );
						}
						@Override
						public void onSuccess( Method method, JSONValue response ) {
							JsonRiskAnalysis ras =  new JsonRiskAnalysis( response );
							newId = ras.getID();
							RiscossJsonClient.rerunRiskAnalysisSession(newId, "", new JsonCallback() {
								@Override
								public void onFailure(Method method,
										Throwable exception) {
									Window.alert(exception.getMessage());
								}
								@Override
								public void onSuccess(Method method,
										JSONValue response) {
									
									generateRiskTree();
									g.setWidget(2, 0, null);
									g.setWidget(2, 1, null);
									g.setWidget(2, 2, null);

									selectedRiskSession = newName.getText().trim();
									newName.setText("");
									mainView.clear();
									top.remove(g);
									vPanel = new VerticalPanel();
									vPanel.setStyleName("leftPanelLayer");
									rasPanel = new RASPanel();
									rasPanel.loadRAS(newId);
									HorizontalPanel h = new HorizontalPanel();
									h.add(back);
									h.add(remove);
									vPanel.add(h);
									vPanel.add(rasPanel);
									mainView.add(vPanel);
								}
							});
						}} );
				}
			});
			
			back = new Button("Back", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					reloadPage();
				}
			});
			back.setStyleName("deleteButton");
			
			remove = new Button("Remove", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					RiscossJsonClient.deleteRiskAnalysisSession(list.get(elem).getId(), new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert(exception.getMessage());
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							generateRiskTree();
							reloadPage();
						}
					});
				}
			});
			remove.setStyleName("deleteButton");
			
		}
		catch( Exception ex ) {
			
			Window.alert( ex.getMessage() );
		}
	}
	
	private void appendChilds(TreeWidget rootEnt, JSONArray children) {
		this.root = rootEnt;
		for (int i = 0; i < children.size(); ++i) {
			RiscossJsonClient.getEntityData(children.get(i).isString().stringValue(), new JsonCallback() {
				TreeWidget rootWidget = root;
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert(exception.getMessage());
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					JsonEntitySummary entityElement = new JsonEntitySummary(response);
					nextEntityName = entityElement.getEntityName();
					Anchor a = new Anchor(nextEntityName + " (" + entityElement.getLayer() + ")");
					a.setWidth("100%");
					a.setStyleName("font");
					a.addClickHandler(new ClickHandler() {
						String name = nextEntityName;
						@Override
						public void onClick(ClickEvent event) {
							setSelectedEntity(name);
						}
					});
					HorizontalPanel cPanel = new HorizontalPanel();
					cPanel.setStyleName("tree");
					cPanel.setWidth("100%");
					cPanel.add(a);
					TreeWidget c = new TreeWidget(cPanel);
					rootWidget.addChild(c);
					if (entityElement.getChildrenList().size() > 0) appendChilds(c, entityElement.getChildrenList()); 
				}
			});
		}
	}
	
	String nextEntityName;
	
	private void generateEntitiesTree() {
		RiscossJsonClient.listEntities( new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				for (int i = 0; i < response.isArray().size(); ++i) {
					RiscossJsonClient.getEntityData( response.isArray().get(i).isObject().get("name").isString().stringValue(), new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert( exception.getMessage() );
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							JsonEntitySummary entityElement = new JsonEntitySummary(response);
							if (entityElement.getParentList().size() == 0) {
								nextEntityName = entityElement.getEntityName();
								Anchor a = new Anchor(nextEntityName  + " (" + entityElement.getLayer() + ")");
								a.setWidth("100%");
								a.setStyleName("font");
								a.addClickHandler(new ClickHandler() {
									String name = nextEntityName;
									@Override
									public void onClick(ClickEvent event) {
										setSelectedEntity(name);
									}
								});
								HorizontalPanel cPanel = new HorizontalPanel();
								cPanel.setStyleName("tree");
								cPanel.setWidth("100%");
								cPanel.add(a);
								TreeWidget c = new TreeWidget(cPanel);
								entitiesTree.addChild(c);
								if (entityElement.getChildrenList().size() > 0) {
									appendChilds(c, entityElement.getChildrenList());
								}
							}
							leftPanel.add(entitiesTree);
						}} );
				}
			}
		});
	}
	
	List<String>	models;
	
	private void setSelectedEntity(String name) {
		selectedEntity = name;
		g.setWidget(0, 1, new Label(name));
		RiscossJsonClient.listRCs(name, new JsonCallback() {
			
			public void onSuccess(Method method, JSONValue response) {
				if( response.isArray() != null ) {
					models = new ArrayList<String>();
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						models.add( o.get( "name" ).isString().stringValue() );
					}
					generateRiskTree();
				}
			}
			
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
		g.setWidget(2, 0, null);
		g.setWidget(2, 1, null);
		g.setWidget(2, 2, null);
		selectedRiskConf = "";
		g.setWidget(1, 1, new Label(" - "));
	}
	
	
	String nextRisk;
	String risksses;
	List<JRASInfo> list = new ArrayList<>();
	int count = 0;
	
	private void generateRiskTree() {
		rightPanel.clear();
		riskTree = new TreeWidget();
		for (int i = 0; i < models.size(); ++i) {
			nextRisk = models.get(i);
			RiscossJsonClient.listRiskAnalysisSessions( selectedEntity, models.get(i), new JsonCallback() {
				String risk = nextRisk;
				public void onSuccess(Method method, JSONValue response) {
					if( response == null ) return;
					if( response.isObject() == null ) return;
					response = response.isObject().get( "list" );
					List<JRASInfo> riskSessions = new ArrayList<>();
					CodecRASInfo codec = GWT.create( CodecRASInfo.class );
					if( response.isArray() != null ) {
						for( int i = 0; i < response.isArray().size(); i++ ) {
							JRASInfo info = codec.decode( response.isArray().get( i ) );
							riskSessions.add( info );
						}
					}
					Anchor a = new Anchor("> " + risk);
					a.setWidth("100%");
					a.setStyleName("font");
					a.addClickHandler(new ClickHandler() {
						String name = risk;
						@Override
						public void onClick(ClickEvent event) {
							setSelectedRiskConf(name);
						}
					});
					HorizontalPanel cPanel = new HorizontalPanel();
					cPanel.setStyleName("tree");
					cPanel.setWidth("100%");
					cPanel.add(a);
					TreeWidget c = new TreeWidget(cPanel);
					riskTree.addChild(c);
					for (int i = 0; i < riskSessions.size(); ++i) {
						risksses = riskSessions.get(i).getName();
						Anchor b = new Anchor(risksses);
						b.setWidth("100%");
						b.setStyleName("font");
						b.addClickHandler(new ClickHandler() {
							String name = risksses;
							int k = count;
							@Override
							public void onClick(ClickEvent event) {
								setSelectedRiskSes(name, k);
							}
						});
						list.add(riskSessions.get(i));
						++count;
						HorizontalPanel dPanel = new HorizontalPanel();
						dPanel.setStyleName("tree");
						dPanel.setWidth("100%");
						dPanel.add(b);
						TreeWidget d = new TreeWidget(dPanel);
						c.addChild(d);
					}
				}
				
				public void onFailure(Method method, Throwable exception) {
					Window.alert( exception.getMessage() );
				}
			});
			
		}
		Label rL = new Label("Risk Analysis");
		rL.setStyleName("smallTitle");
		rightPanel.add(rL);
		rightPanel.add(riskTree);
	}
	
	TextBox newName = new TextBox();
	
	private void setSelectedRiskConf(String name) {
		selectedRiskConf = name;
		g.setWidget(1, 1, new Label(name));
		Label nameL = new Label("Risk session name");
		nameL.setStyleName("bold");
		g.setWidget(2, 0, nameL);
		newName.setText(selectedRiskConf + " - " + selectedEntity);
		g.setWidget(2, 1, newName);
		g.setWidget(2, 2, newRiskSession);
		
	}
	
	VerticalPanel vPanel;
	int elem;
	
	private void setSelectedRiskSes(String name, int k) {
		newName.setText("");
		g.setWidget(2, 0, null);
		g.setWidget(2, 1, null);
		g.setWidget(2, 2, null);
		selectedRiskSession = name;
		elem = k;
		mainView.clear();
		top.remove(g);
		vPanel = new VerticalPanel();
		vPanel.setStyleName("leftPanelLayer");
		rasPanel = new RASPanel();
		rasPanel.loadRAS(list.get(k).getId());
		HorizontalPanel h = new HorizontalPanel();
		h.add(back);
		h.add(remove);
		vPanel.add(rasPanel);
		vPanel.add(h);
		title.setText(selectedRiskSession);
		mainView.add(vPanel);
	}
	
	private void reloadPage() {
		mainView.clear();
		mainView.add(leftPanel);
		mainView.add(rightPanel);
		top.add(g);
		selectedRiskConf = "";
		selectedRiskSession = "";
		title.setText("Multi-layer Analysis");
		g.setWidget(1, 1, new Label(" - "));
	}
}
