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

package eu.riscoss.client.entities;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.JsonEntitySummary;
import eu.riscoss.client.JsonRiskDataList;
import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecRASInfo;
import eu.riscoss.client.layers.LayersModule;
import eu.riscoss.client.rdr.EntityDataBox;
import eu.riscoss.client.report.RiskAnalysisReport;
import eu.riscoss.client.riskanalysis.RASPanel;
import eu.riscoss.client.ui.ContextualInfoTable;
import eu.riscoss.client.ui.CustomizableForm;
import eu.riscoss.client.ui.CustomizableForm.CustomField;
import eu.riscoss.client.ui.TreeWidget;
import eu.riscoss.shared.JRASInfo;
import eu.riscoss.shared.RiscossUtil;

public class EntityPropertyPage implements IsWidget {
	
	public class RDCConfDialog {
		
		DialogBox dialog = new DialogBox( false, true );
		DockPanel dock = new DockPanel();
		RDCConfigurationPage ppg = new RDCConfigurationPage();
		String RDCEntity;
		EntitiesModule module;
		LayersModule lModule;
		
		RDCConfDialog(EntitiesModule module) {
			this.module = module;
			dock.add( ppg.asWidget(), DockPanel.CENTER );
			dialog.setWidget( dock );
		}
		
		public void setLModule(LayersModule m) {
			this.lModule = m;
		}
		
		public Boolean changedData() {
			return ppg.changedData();
		}
		
		public void setChangedData() {
			ppg.setChangedData();
		}
		
		public void saveAndRun() {
			Boolean b = Window.confirm("Data collectors will be saved before running. Do you want to continue? (If you click 'Cancel', it will not be executed)");
			if (b) {
				JSONObject json = ppg.getJson();
				String str = "";
				String sep = "";
				for( String key : json.keySet() ) {
					if( json.get( key ).isObject().get( "enabled" ).isBoolean().booleanValue() == true ) {
						str += sep + key;
						sep = ", ";
					}
				}
				saveAndRunRDC(json);
			}
		}
		
		public void save(String entity) {
			RDCEntity = entity;
			JSONObject json = ppg.getJson();
			String str = "";
			String sep = "";
			for( String key : json.keySet() ) {
				if( json.get( key ).isObject().get( "enabled" ).isBoolean().booleanValue() == true ) {
					str += sep + key;
					sep = ", ";
				}
			}
			RiscossJsonClient.saveRDCs( json, RDCEntity, new JsonCallback() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert(exception.getMessage());
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					if (module != null) {
						module.reloadData();
					}
					if (lModule != null) {
						lModule.reloadData(RDCEntity);
					}
				}
			});
		}
		
		private void saveAndRunRDC(JSONObject json) {
			RiscossJsonClient.saveRDCs( json, RDCEntity, new JsonCallback() {
				@Override
				public void onSuccess(Method method, JSONValue response) {
					RiscossJsonClient.runRDCs( EntityPropertyPage.this.entity, new JsonCallback() {
						@Override
						public void onSuccess(Method method, JSONValue response) {
							try {
							Log.println( "RDC Collector returned " + response );
							JSONObject json = response.isObject();
							String msg = json.get( "msg" ).isString().stringValue();
							Log.println( "Iterating..." );
							for( String rdc : json.keySet() ) {
								Log.println( rdc );
								JSONObject o = json.get( rdc ).isObject();
								if( o == null ) continue;
								if( "error".equals( o.get( "result" ).isString().stringValue() ) ) {
									msg += " " + rdc + ": " + o.get( "error-message" ).isString().stringValue();
								}
								Log.println( "Ok" );
							}
							Log.println( "MSG: " + msg );
							Window.alert( msg );
							module.ppg.refreshDC();
							}
							catch( Exception ex ) {
								Window.alert( ex.getMessage() );
							}
						}
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert( exception.getMessage() );
						}
					});
				}
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert( exception.getMessage() );
				}} );
		}
		
		public void show(String entity) {
			ppg.setSelectedEntity( entity );
			dialog.show();
		}

		public DockPanel getDock() {
			return dock;
		}
		
		public void setSelectedEntity(String entity) {
			RDCEntity = entity;
			ppg.setSelectedEntity(entity);
		}
		
	}
	
	public void setLModule(LayersModule l) {
		confDialog.setLModule(l);
	}
	
	TabPanel			tab				= new TabPanel();
	SimplePanel			summaryPanel	= new SimplePanel();
	SimplePanel			ciPanel			= new SimplePanel();
	SimplePanel			rasPanel		= new SimplePanel();
	SimplePanel			newRasPanel		= new SimplePanel();
	SimplePanel			dataCollectors 	= new SimplePanel();
	SimplePanel			rdr				= new SimplePanel();
	ContextualInfoTable	userForm 		= new ContextualInfoTable();
	FlexTable			tb				= new FlexTable();
	
	String				layer;
	
	Anchor				rdcAnchor;
	
	private String		entity;
	
	RDCConfDialog		confDialog;
	EntityDataBox		entityDataBox;
	ArrayList<String>	entitiesList;
	ArrayList<String>	parentList;
	ArrayList<String>	childrenList;
	
	String[]			contextualInfo;
	ArrayList<String>	extraInfoList;
	boolean				rasLoaded		= false;
	EntitiesModule		module;
	
	Button				backRAS;
	
	Boolean				changedData = false;
	
	public EntityPropertyPage(EntitiesModule m) {
		this.module = m;
		tab.add( summaryPanel, "Properties" );
		tab.add( ciPanel, "Custom information" );
		tab.add( dataCollectors, "Data Collectors");
		tab.add( rdr, "Data Repository");
		tab.add( newRasPanel, "Analysis Sessions" );
		tab.selectTab( 0 );
		tab.setSize( "100%", "100%" );
		
		RiscossJsonClient.listEntities(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				entitiesList = new ArrayList<>();
				for (int i = 0; i < response.isArray().size(); ++i) {
					entitiesList.add(response.isArray().get(i).isObject().get("name").isString().stringValue());
				}
			}
		});
		
	}
	
	public void setSelectedTab(int i) {
		tab.selectTab(i);
	}
	
	@Override
	public Widget asWidget() {
		return this.tab;
	}
	
	public void setSelectedEntity( String entity ) {
		
		if( summaryPanel.getWidget() != null ) {
			summaryPanel.getWidget().removeFromParent();
		}
		if( ciPanel.getWidget() != null ) {
			ciPanel.getWidget().removeFromParent();
		}
		
		this.entity = entity;
		extraInfoList = new ArrayList<>();
		
		if( this.entity == null ) return;
		
		RiscossJsonClient.getEntityData( entity, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				loadProperties( response );
			}} );
	}
	
	public String getLayer() {
		return layer;
	}

	
	VerticalPanel parents;
	VerticalPanel children;
	VerticalPanel v;
	ListBox parentsListbox = new ListBox();
	ListBox childrenListbox = new ListBox();
	TextColumn<String> t;
	TextColumn<String> t2;
	Button deleteParent;
	Button deleteChildren;
	CellTable<String> parentsTable;
	CellTable<String> childrenTable;
	FlexTable custom;
	JsonEntitySummary info;
	
	
	ListDataProvider<String> parentDataProvider;
	SimplePager pager;
	ListDataProvider<String> childrenDataProvider;
	SimplePager childrenPager;
	VerticalPanel propertiesPanel = new VerticalPanel();
	
	
	protected void loadProperties( JSONValue response ) {
		rasLoaded = false;
		info = new JsonEntitySummary( response );
		
		confDialog = new RDCConfDialog(module);
		confDialog.setSelectedEntity(entity);
		
		layer = info.getLayer();
		
		parentList = new ArrayList<>();
		childrenList = new ArrayList<>();
		parentsListbox = new ListBox();
		childrenListbox = new ListBox();
		
		for (int i = 0; i < info.getParentList().size(); ++i) {
			parentList.add(info.getParentList().get(i).isString().stringValue());
		}
		for (int i = 0; i < info.getChildrenList().size(); ++i) {
			childrenList.add(info.getChildrenList().get(i).isString().stringValue());
		}
		
		
		//Properties Panel
		v = new VerticalPanel();
		propertiesPanel = new VerticalPanel();
		
		Label layerInfoLabel = new Label("Layer information");
		layerInfoLabel.setStyleName("smallTitle");
		Label parentInfoLabel = new Label("Hierarchy information");
		parentInfoLabel.setStyleName("smallTitle");
		
		loadContextualInfoData();
		propertiesPanel.add(layerInfoLabel);
		propertiesPanel.add(tb);
		propertiesPanel.add(parentInfoLabel);
		propertiesPanel.add(v);
		
		
		v.setWidth("100%");
		{	
			HorizontalPanel hPanel = new HorizontalPanel();
			hPanel.setWidth("100%");
			parents = new VerticalPanel();
			children = new VerticalPanel();
			
			HorizontalPanel data = new HorizontalPanel();
			
			Label l = new Label("Parent");
			l.setStyleName("bold");
			data.add(l);
			RiscossJsonClient.getCandidateChildren(entity, new JsonCallback() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					Window.alert(exception.getMessage());
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					for (int i = 0; i < response.isArray().size(); ++i) {
						childrenListbox.addItem(response.isArray().get(i).isString().stringValue());
					}
					RiscossJsonClient.getCandidateParents(entity, new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert(exception.getMessage());
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							for (int i = 0; i < response.isArray().size(); ++i) {
								parentsListbox.addItem(response.isArray().get(i).isString().stringValue());
							}
						}
					});
				}
			});
			/*for (int i = 0; i < entitiesList.size(); ++i) {
				if (!entitiesList.get(i).equals(entity)) {
					parentsListbox.addItem(entitiesList.get(i));
					childrenListbox.addItem(entitiesList.get(i));
				}
			}*/
			data.add(parentsListbox);
			Button b = new Button("Add parent", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					String newParent = parentsListbox.getItemText(parentsListbox.getSelectedIndex());
					if (parentList.contains(newParent)) {
						Window.alert("The selected entity is already a parent");
						return;
					}
					if (childrenList.contains(newParent)) {
						Window.alert("The selected entity is already a children. An entity can not be parent and child of the same entity.");
						return;
					}
					parentList.add(newParent);
					parentsTable.setRowData(0, parentList);
					changedData = true;
				}
			});
			b.setStyleName("Button");
			data.add(b);
			data.setStyleName("marginTopBottom");
			parents.add(data);
			
			parentsTable = new CellTable<String>(15, (Resources) GWT.create(TableResources.class));
			parentsTable.setWidth("100%");
			t = new TextColumn<String>() {
				@Override
				public String getValue(String arg0) {
					return arg0;
				}
			};
			deleteParent = new Button("Delete parent");
			deleteParent.setStyleName("deleteButton");
			final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>();
		    parentsTable.setSelectionModel(selectionModel);
		    selectionModel.addSelectionChangeHandler(new Handler() {
				@Override
				public void onSelectionChange(SelectionChangeEvent arg0) {
					parents.remove(deleteParent);
					deleteParent.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							parentList.remove(selectionModel.getSelectedObject());
							parents.remove(parentsTable);
							parents.remove(pager);
							parentsTable = new CellTable<String>(15, (Resources) GWT.create(TableResources.class));
							parentsTable.setWidth("100%");
							 parentsTable.setSelectionModel(selectionModel);
							t = new TextColumn<String>() {
								@Override
								public String getValue(String arg0) {
									return arg0;
								}
							};
							parentsTable.addColumn(t, "Parents");
							if (parentList.size() > 0) parentsTable.setRowData(0, parentList);
							else {
								parentList.add("");
								parentsTable.setRowData(0, parentList);
								parentList.remove(0);
							}
							parentsTable.setStyleName("table");
							
							parentDataProvider = new ListDataProvider<String>();
							parentDataProvider.addDataDisplay( parentsTable );
							
							for( int i = 0; i < parentList.size(); i++ ) {
								parentDataProvider.getList().add( parentList.get(i) );
							}
							
							pager = new SimplePager();
						    pager.setDisplay( parentsTable );
							
							parents.add(parentsTable);
							parents.add(pager);
							
							parents.remove(deleteParent);
							changedData = true;
						}
					});
					parents.add(deleteParent);
				}
		    });
			
			parentsTable.addColumn(t, "Parents");
			
			if (parentList.size() > 0) parentsTable.setRowData(0, parentList);
			else {
				parentList.add("");
				parentsTable.setRowData(0, parentList);
				parentList.remove(0);
			}
			parentsTable.setStyleName("table");
			
			parentDataProvider = new ListDataProvider<String>();
			parentDataProvider.addDataDisplay( parentsTable );
			
			for( int i = 0; i < parentList.size(); i++ ) {
				parentDataProvider.getList().add( parentList.get(i) );
			}
			
			pager = new SimplePager();
		    pager.setDisplay( parentsTable );
			
			parents.add(parentsTable);
			parents.add(pager);
			
			HorizontalPanel data2 = new HorizontalPanel();
			
			Label l2 = new Label("Children");
			l2.setStyleName("bold");
			data2.add(l2);
			data2.add(childrenListbox);
			Button b2 = new Button("Add children", new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					String newChildren = childrenListbox.getItemText(childrenListbox.getSelectedIndex());
					if (childrenList.contains(newChildren)) {
						Window.alert("The selected entity is already a children");
						return;
					}
					if (parentList.contains(newChildren)) {
						Window.alert("The selected entity is already a children. An entity can not be parent and child of the same entity.");
						return;
					}
					childrenList.add(newChildren);
					childrenTable.setRowData(0, childrenList);
					changedData = true;
				}
			});
			b2.setStyleName("Button");
			data2.add(b2);
			data2.setStyleName("marginTopBottom");
			children.add(data2);
			
			childrenTable = new CellTable<String>(15, (Resources) GWT.create(TableResources.class));
			childrenTable.setWidth("100%");
			t2 = new TextColumn<String>() {
				@Override
				public String getValue(String arg0) {
					return arg0;
				}
			};
			deleteChildren = new Button("Delete children");
			deleteChildren.setStyleName("deleteButton");
			final SingleSelectionModel<String> selectionModel2 = new SingleSelectionModel<String>();
		    childrenTable.setSelectionModel(selectionModel2);
		    selectionModel2.addSelectionChangeHandler(new Handler() {
				@Override
				public void onSelectionChange(SelectionChangeEvent arg0) {
					children.remove(deleteChildren);
					deleteChildren.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							childrenList.remove(selectionModel2.getSelectedObject());
							children.remove(childrenTable);
							children.remove(childrenPager);
							childrenTable = new CellTable<String>(15, (Resources) GWT.create(TableResources.class));
							childrenTable.setWidth("100%");
							childrenTable.setSelectionModel(selectionModel2);
							t2 = new TextColumn<String>() {
								@Override
								public String getValue(String arg0) {
									return arg0;
								}
							};
							childrenTable.addColumn(t, "Children");
							if (childrenList.size() > 0) childrenTable.setRowData(0, childrenList);
							else {
								childrenList.add("");
								childrenTable.setRowData(0, childrenList);
								childrenList.remove(0);
							}
							childrenTable.setStyleName("table");
							
							childrenDataProvider = new ListDataProvider<String>();
							childrenDataProvider.addDataDisplay( childrenTable );
							
							for( int i = 0; i < childrenList.size(); i++ ) {
								childrenDataProvider.getList().add( childrenList.get(i) );
							}
							
							childrenPager = new SimplePager();
						    childrenPager.setDisplay( childrenTable );
							
							children.add(childrenTable);
							children.add(childrenPager);
							
							children.remove(deleteChildren);
							changedData = true;
						}
					});
					children.add(deleteChildren);
				}
		    });
			
			childrenTable.addColumn(t, "Children");
			
			if (childrenList.size() > 0) childrenTable.setRowData(0, childrenList);
			else {
				childrenList.add("");
				childrenTable.setRowData(0, childrenList);
				childrenList.remove(0);
			}
			childrenTable.setStyleName("table");
			
			childrenDataProvider = new ListDataProvider<String>();
			childrenDataProvider.addDataDisplay( childrenTable );
			
			for( int i = 0; i < childrenList.size(); i++ ) {
				childrenDataProvider.getList().add( childrenList.get(i) );
			}
			
			childrenPager = new SimplePager();
		    childrenPager.setDisplay( childrenTable );
			
			children.add(childrenTable);
			children.add(childrenPager);
			
			hPanel.setWidth("100%");
			hPanel.add(parents);
			HorizontalPanel h = new HorizontalPanel();
			h.setWidth("100px");
			hPanel.add(h);
			hPanel.add(children);
			v.add(hPanel);
			
			
			
		}
		
		entityDataBox = new EntityDataBox();
		entityDataBox.setSelectedEntity(entity);
		rdr.setWidget(entityDataBox);
		loadRASWidget();
	}
	
	public void runDC() {
		confDialog.saveAndRun();
	}
	
	public void refreshDC() {
		entityDataBox = new EntityDataBox();
		entityDataBox.setSelectedEntity(entity);
		rdr.setWidget(entityDataBox);
	}
	
	List<String> types;
	TextBox newID;
	TextBox newValue;
	
	private void loadContextualInfoData() {
		tb = new FlexTable();
		userForm = new ContextualInfoTable();
		custom = new FlexTable();
		types = new ArrayList<>();
		
		int row = 0;
		int rowC = 0;
		
		for( int i = 0; i < info.getUserData().size(); i++ ) {
			JsonRiskDataList.RiskDataItem item = info.getUserData().get( i );
			
			if (item.getDataType().equals("CUSTOM")) {
				userForm.addField(item.getId(), item.getValue());
			}
			
			else {
				
				String val = item.getValue();
				contextualInfo = val.split(";");
				
				String extrainfo = "";
				for (int k = 1; k < contextualInfo.length; ++k) {
					extrainfo += ";" + contextualInfo[k];
				}
				extraInfoList.add(extrainfo);
				
				tb.insertRow(row);
				tb.insertCell(row, 0);
				tb.insertCell(row, 1);
				Label id = new Label(item.getId());
				id.setStyleName("bold");
				tb.setWidget(row, 0, id);
				
				if (item.getDataType().equals("Integer")) {
					TextBox t = new TextBox();
					t.setText(contextualInfo[0]);
					t.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							changedData = true;	
						}
					});
					tb.setWidget(row, 1, t);
					types.add("Integer");
				}
				else if (item.getDataType().equals("Boolean")) {
					CheckBox c = new CheckBox();
					if (Integer.parseInt(contextualInfo[0]) == 1) c.setChecked(true);
					c.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
						@Override
						public void onValueChange(
								ValueChangeEvent<Boolean> event) {
							changedData = true;							
						}
					});
					tb.setWidget(row, 1, c);
					types.add("Boolean");
				}
				else if (item.getDataType().equals("Date")) {
					DateTimeFormat dateFormat = DateTimeFormat.getLongDateFormat();
				    DateBox dateBox = new DateBox();
				    dateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
				    dateBox.getDatePicker().setYearArrowsVisible(true);
				    dateBox.addValueChangeHandler(new ValueChangeHandler<Date>() {
						@Override
						public void onValueChange(ValueChangeEvent<Date> event) {
							changedData = true;
						}
				    });
				    
				    Grid g = new Grid(1,7);
				    
					g.setWidget(0, 0, dateBox);
					
					String inf[] = contextualInfo[0].split(":");
					
					TextBox t = new TextBox();
					TextBox t2 = new TextBox();
					TextBox t3 = new TextBox();
					
					if (inf.length > 1) {
						String date[] = inf[0].split("-");
						String time[] = inf[1].split("-");
						
						int year = Integer.parseInt(date[0]) - 1900;
						int month = Integer.parseInt(date[1]) - 1;
						if (month == 0) {month = 12;--year;}
						int day = Integer.parseInt(date[2]);
						Date d = new Date(year, month, day);
						dateBox.setValue(d);
						
						t.setText(String.valueOf(time[0]));
						t2.setText(String.valueOf(time[1]));
						t3.setText(String.valueOf(time[2]));
					}
					
					t.setWidth("30px");
					
					t.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							changedData = true;
						}
					});
					g.setWidget(0, 1, t);
					g.setWidget(0, 2, new Label("hh"));
					
					t2.setWidth("30px");
					
					t2.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							changedData = true;	
						}
					});
					g.setWidget(0, 3, t2);
					g.setWidget(0, 4, new Label("mm"));
					
					t3.setWidth("30px");
					
					t3.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							changedData = true;	
						}
					});
					g.setWidget(0, 5, t3);
					g.setWidget(0, 6, new Label("ss"));
					
					tb.setWidget(row, 1, g);
					types.add("Date");
				}
				else if (item.getDataType().equals("List")) {
					ListBox lb = new ListBox();
					for (int k = 1; k < contextualInfo.length; ++k) {
						lb.addItem(contextualInfo[k]);
					}
					lb.setSelectedIndex(Integer.parseInt(contextualInfo[0]));
					lb.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							changedData = true;
						}
					});
					
					tb.setWidget(row, 1, lb);
					types.add("List");
				}
				else if (item.getDataType().equals("Text")) {
					TextBox t = new TextBox();
					t.setText(contextualInfo[0]);
					t.addValueChangeHandler(new ValueChangeHandler<String>() {
						@Override
						public void onValueChange(ValueChangeEvent<String> event) {
							changedData = true;	
						}
					});
					tb.setWidget(row, 1, t);
					types.add("Text");
				}
				++row;
			}
			
		}
		
	
		HorizontalPanel newCIElement = new HorizontalPanel();
		newCIElement.setStyleName("layerData");
		Label newIDL = new Label("ID");
		newIDL.setStyleName("bold");
		newID = new TextBox();
		Label newValueL = new Label("Value");
		newValueL.setStyleName("bold");
		newValue = new TextBox();
		newCIElement.add(newIDL);
		newCIElement.add(newID);
		newCIElement.add(newValueL);
		newCIElement.add(newValue);
		
		Button newCustomInfo = new Button("New custom information");
		newCustomInfo.setStyleName("button");
		newCustomInfo.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				boolean b = userForm.newField(newID.getText(), newValue.getText());
				if (b) {
					newID.setText("");
					newValue.setText("");
				}
			}
		});
		
		VerticalPanel vPanel = new VerticalPanel();
		vPanel.add(newCIElement);
		vPanel.add(newCustomInfo);
		vPanel.add(userForm.getWidget());
		
		summaryPanel.setWidget( propertiesPanel );
		dataCollectors.setWidget(confDialog.getDock());
		ciPanel.setWidget( vPanel );
	}
	
	String newN;
	String newL;
	
	public void saveEntityData(String newName, String newLayer) {
		newN = newName;
		newL = newLayer;
		RiscossJsonClient.listRiskAnalysisSessions(entity, "", new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				if (!newL.equals(layer) && (parentList.size() > 0 || childrenList.size() > 0)) {
					Window.alert("Entities with parents or children cannot modify the layer");
					return;
				}
				if( response == null ) return;
				if( response.isObject() == null ) return;
				response = response.isObject().get( "list" );
				if (!newL.equals(layer) && response.isArray().size() > 0) {
					Window.alert("Entities with associated risk analysis sessions cannot modify the layer");
					return;
				}
				if (!newL.equals(layer) && response.isArray().size() == 0) {
					RiscossJsonClient.editLayer(entity, newL, new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {Window.alert(exception.getMessage());
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							if (!newN.equals(entity)) {
								renameAndSave(newN);
							} else {
								saveEntity();
							}
						}
					});
				}
				else if (newL.equals(layer)) {
					if (!newN.equals(entity)) {
						renameAndSave(newN);
					} else {
						saveEntity();
					}
				}
			}
		});
		
	}
	
	private void renameAndSave(String newName) {
		newN = newName;
		RiscossJsonClient.listEntities(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				if (newN == null || newN.equals("") ) {
					return;
				}
				//String s = RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
				if (!RiscossUtil.sanitize(newN).equals(newN)){
					//info: firefox has some problem with this window, and fires assertion errors in dev mode
					Window.alert("Entity name contains prohibited characters (##,@,\") \nPlease re-enter name");
					return;
				}
				for(int i=0; i<response.isArray().size(); i++){
					JSONObject o = (JSONObject)response.isArray().get(i);
					if (newN.equals(o.get( "name" ).isString().stringValue())) {
						Window.alert("Entity name already in use.\nPlease re-enter name.");
						return;
					}
				}
				RiscossJsonClient.renameEntity(entity, newN, new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert(exception.getMessage());
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						entity = newN;
						saveEntity();
					}
				});
			}
		});
	}
	
	private void saveEntity() {
		saveContextualInfo();
		changedData = false;
		confDialog.setChangedData();
	}
	
	private void saveDataCollectors() {
		confDialog.setLModule(l);
		confDialog.save(entity);
	}
	
	private void saveParentyInfo() {
		RiscossJsonClient.setChildren(entity, childrenList, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				RiscossJsonClient.setParents(entity, parentList, new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert(exception.getMessage());
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						saveDataCollectors();
					}
				});
			}
		});
	}
	
	private void saveContextualInfo() {
		userForm.save(entity);
		for (int i = 0; i < tb.getRowCount(); ++i) {
			JSONObject o = new JSONObject();
			o.put( "id", new JSONString( ((Label)tb.getWidget(i, 0)).getText() ) );
			o.put( "target", new JSONString( EntityPropertyPage.this.entity ) );
			String datatype = types.get(i);
			String value = "";
			if (datatype.equals("Integer")) {
				if (!((TextBox) tb.getWidget(i, 1)).getText().equals("")) {
					int cValue = Integer.parseInt(((TextBox) tb.getWidget(i, 1)).getText());
					String[] extra = extraInfoList.get(i).split(";");
					int min = Integer.parseInt(extra[1]);
					int max = Integer.parseInt(extra[2]);
					if (cValue < min || cValue > max) {
						Window.alert(((Label)tb.getWidget(i, 0)).getText()  + " value must be within limits (min = " + min + ", max = " + max + ")");
						return;
					}
				}
				value += ((TextBox) tb.getWidget(i, 1)).getText();
				value += extraInfoList.get(i);
			}
			else if (datatype.equals("Boolean")) {
				if (((CheckBox) tb.getWidget(i, 1)).isChecked()) value += "1";
				else value += "0";
			}
			else if (datatype.equals("Date")) {
				if (!((TextBox) ((Grid) tb.getWidget(i, 1)).getWidget(0, 1)).getText().equals("")) {
					int hour = Integer.parseInt(((TextBox) ((Grid) tb.getWidget(i, 1)).getWidget(0, 1)).getText());
					int minute = Integer.parseInt(((TextBox) ((Grid) tb.getWidget(i, 1)).getWidget(0, 3)).getText());
					int second = Integer.parseInt(((TextBox) ((Grid) tb.getWidget(i, 1)).getWidget(0, 5)).getText());
					Date date = ((DateBox) ((Grid) tb.getWidget(i,1)).getWidget(0, 0)).getValue();
					date.setHours(hour);
					date.setMinutes(minute);
					date.setSeconds(second);
					
					DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd:HH-mm-ss");
				    value += fmt.format(date);
				}
			}
			else if (datatype.equals("List")){
				value += ((ListBox) tb.getWidget(i, 1)).getSelectedIndex();
				value += extraInfoList.get(i);
			}
			else if (datatype.equals("Text")) {
				value += ((TextBox) tb.getWidget(i, 1)).getText();
			}
			o.put( "value", new JSONString( value ) );
			o.put( "datatype", new JSONString ( datatype ) );
			o.put( "type", new JSONString( "custom" ) );
			o.put( "origin", new JSONString( "user" ) );
			JSONArray array = new JSONArray();
			array.set( 0, o );
			RiscossJsonClient.postRiskData( array, new JsonCallback() {
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					
				}
			} );
			if (i == tb.getRowCount()-1) saveParentyInfo();
		}
		if (tb.getRowCount() == 0) saveParentyInfo();
	}
	
	TreeWidget 		riskTree;
	List<String>	models;
	String			nextRisk;
	String			risksses;
	List<JRASInfo> 	list = new ArrayList<>();
	int 			count = 0;
	VerticalPanel 	panel;
	
	private void loadRASWidget() {
		RiscossJsonClient.listRCs(entity, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, JSONValue response) {
				models = new ArrayList<String>();
				for( int i = 0; i < response.isArray().size(); i++ ) {
					JSONObject o = (JSONObject)response.isArray().get( i );
					models.add( o.get( "name" ).isString().stringValue() );
				}
				riskTree = new TreeWidget();
				for (int i = 0; i < models.size(); ++i) {
					nextRisk = models.get(i);
					RiscossJsonClient.listRiskAnalysisSessions( entity, models.get(i), new JsonCallback() {
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
							Label a = new Label("> " + risk);
							a.setWidth("100%");
							a.setStyleName("bold");
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
				panel = new VerticalPanel();
				panel.add(riskTree);
				newRasPanel.setWidget(panel);
			}
		});
		
	}
	
	LayersModule l;
	
	public void setLayerModule(LayersModule l) {
		this.l = l;
	}
	
	private void setSelectedRiskSes(String name, int k) {
		if (l != null) l.setSelectedRiskSes(list.get(k).getId(), this);
		else module.setSelectedRiskSes(list.get(k).getId(), this);
	}

	public void back(Boolean entityB) {
		if (entityB) module.back();
		else l.back();
	}
	
	public void delete(String ras, Boolean entityB) {
		if (entityB) module.deleteRiskSes(ras);
		else l.deleteRiskSes(ras);
	}
	
	/*protected void loadRAS( JSONValue response ) {
		if( rasPanel.getWidget() != null ) {
			rasPanel.getWidget().removeFromParent();
		}
		if( response == null ) {
			rasLoaded = true;
			return;
		}
		try {
			RiskAnalysisReport report = new RiskAnalysisReport();
			report.showResults( 
					response.isObject().get( "results" ).isArray(),
					response.isObject().get( "argumentation" ) );
			rasPanel.setWidget( report.asWidget() );
			rasLoaded = true;
		}
		catch( Exception ex ) {
			Window.alert( ex.getMessage() );
		}
	}*/

	protected void onChildEntitySelected( List<String> entities ) {
		RiscossJsonClient.setChildren( entity, entities, new JsonCallback() {
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}

			@Override
			public void onSuccess( Method method, JSONValue response ) {
			}} );
	}

	protected void onParentEntitySelected( List<String> parents ) {
		RiscossJsonClient.setParents( entity, parents, new JsonCallback() {
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}

			@Override
			public void onSuccess( Method method, JSONValue response ) {
				
			}} );
	}

	public boolean hasParents() {
		if (parentList.size() > 0) return true;
		else return false;
	}
	
}