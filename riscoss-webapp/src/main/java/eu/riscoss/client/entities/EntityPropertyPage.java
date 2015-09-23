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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
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

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.JsonEntitySummary;
import eu.riscoss.client.JsonRiskDataList;
import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.report.RiskAnalysisReport;
import eu.riscoss.client.ui.CustomizableForm;
import eu.riscoss.client.ui.CustomizableForm.CustomField;

public class EntityPropertyPage implements IsWidget {
	
	class RDCConfDialog {
		
		DialogBox dialog = new DialogBox( false, true );
		DockPanel dock = new DockPanel();
		RDCConfigurationPage ppg = new RDCConfigurationPage();
		String RDCEntity;
		
		RDCConfDialog() {
			Button done = new Button( "Done");
			done.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
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
						public void onSuccess(Method method, JSONValue response) {
							Window.alert("Risk Data Collectors saved");
						}
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert( exception.getMessage() );
						}} );
				}} );
			done.setStyleName("button");
			Button run = new Button( "Run now" );
			run.setStyleName("button");
			run.addClickHandler( new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
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
			});
			dock.add(run , DockPanel.NORTH);
			dock.add( ppg.asWidget(), DockPanel.CENTER );
			dock.add(done, DockPanel.SOUTH);
			dialog.setWidget( dock );
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
	
	TabPanel			tab				= new TabPanel();
	SimplePanel			summaryPanel	= new SimplePanel();
	SimplePanel			ciPanel			= new SimplePanel();
	SimplePanel			rasPanel		= new SimplePanel();
	SimplePanel			dataCollectors 	= new SimplePanel();
	CustomizableForm	userForm 		= new CustomizableForm();
	FlexTable			tb;
	
	String				layer;
	
	Anchor				rdcAnchor;
	
	private String		entity;
	
	RDCConfDialog		confDialog;
	ArrayList<String>	entitiesList;
	ArrayList<String>	parentList;
	ArrayList<String>	childrenList;
	
	String[]			contextualInfo;
	ArrayList<String>	extraInfoList;
	boolean				rasLoaded		= false;
	EntitiesModule		module;
	
	public EntityPropertyPage(EntitiesModule m) {
		this.module = m;
		tab.add( summaryPanel, "Properties" );
		tab.add( ciPanel, "Contextual Information" );
		tab.add( dataCollectors, "Data Collectors");
		tab.add( rasPanel, "RAS" );
		tab.selectTab( 0 );
		tab.setSize( "100%", "100%" );
		tab.addSelectionHandler( new SelectionHandler<Integer>() {
			@Override
			public void onSelection( SelectionEvent<Integer> event ) {
				if( rasLoaded == true ) return;
				RiscossJsonClient.getRASResults( entity, new JsonCallback() {
					@Override
					public void onFailure( Method method, Throwable exception ) {
						Window.alert( exception.getMessage() );
					}
					@Override
					public void onSuccess( Method method, JSONValue response ) {
						loadRAS( response );
					}} );
			}
		});
		
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

	protected void loadProperties( JSONValue response ) {
		rasLoaded = false;
		JsonEntitySummary info = new JsonEntitySummary( response );
		
		confDialog = new RDCConfDialog();
		confDialog.setSelectedEntity(entity);
		
		layer = info.getLayer();
		
		parentList = new ArrayList<>();
		childrenList = new ArrayList<>();
		
		for (int i = 0; i < info.getParentList().size(); ++i) {
			parentList.add(info.getParentList().get(i).isString().stringValue());
		}
		for (int i = 0; i < info.getChildrenList().size(); ++i) {
			childrenList.add(info.getChildrenList().get(i).isString().stringValue());
		}
		
		VerticalPanel v = new VerticalPanel();
		{
			Grid grid = new Grid( 1, 2 );
			Grid grid2 = new Grid( 1, 2 );
			
			
			grid.setWidget( 0, 0, new Label( "Owned by:" ) );
			{
				Grid g = new Grid(entitiesList.size(), 1);
				for (int i = 0; i < entitiesList.size(); ++i) {
					CheckBox cb = new CheckBox(entitiesList.get(i));
					cb.setName(entitiesList.get(i));
					cb.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							CheckBox chk = (CheckBox)event.getSource();
							boolean value = chk.getValue();
							if( value == true ) {
								parentList.add( chk.getName() );
							}
							else {
								parentList.remove( chk.getName() );
							}
						}
					});
					if (parentList.contains(entitiesList.get(i))) {
						cb.setChecked(true);
					}
					g.setWidget(i, 0, cb);
				}
				grid.setWidget(0, 1, g);
			}
			{
				grid2.setWidget( 0, 0, new Label( "Owns:" ) );
				Grid g = new Grid(entitiesList.size(), 1);
				for (int i = 0; i < entitiesList.size(); ++i) {
					CheckBox cb = new CheckBox(entitiesList.get(i));
					cb.setName(entitiesList.get(i));
					cb.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							CheckBox chk = (CheckBox)event.getSource();
							boolean value = chk.getValue();
							if( value == true ) {
								childrenList.add( chk.getName() );
							}
							else {
								childrenList.remove( chk.getName() );
							}
						}
					});
					if (childrenList.contains(entitiesList.get(i))) {
						cb.setChecked(true);
					}
					g.setWidget(i, 0, cb);
				}
				grid2.setWidget(0, 1, g);
			}
			HorizontalPanel hPanel = new HorizontalPanel();
			hPanel.add(grid);
			hPanel.add(grid2);
			v.add( hPanel );
			Button upload = new Button("Upload parents");
			upload.setStyleName("button");
			upload.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					RiscossJsonClient.setParents(entity, parentList, new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							RiscossJsonClient.setChildren(entity, childrenList, new JsonCallback() {
								@Override
								public void onFailure(Method method, Throwable exception) {
								}
								@Override
								public void onSuccess(Method method, JSONValue response) {
									if (module != null) module.reloadData();
								}
							});
						}
					});
				}
			});
			v.add(upload);
			grid.setWidth( "100%" );
			v.setWidth( "100%" );
			grid.getColumnFormatter().setWidth( 0, "20%" );
			grid.getColumnFormatter().setWidth( 1, "80%" );
		}
		
		tb = new FlexTable();
		userForm = new CustomizableForm();
		
		int row = 0;
		
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
				tb.insertCell(row, 2);
				tb.setWidget(row, 0, new Label(item.getId()));
				tb.setWidget(row, 1, new Label(item.getDataType()));
				
				if (item.getDataType().equals("Integer")) {
					TextBox t = new TextBox();
					t.setText(contextualInfo[0]);
					
					tb.setWidget(row, 2, t);
				}
				else if (item.getDataType().equals("Boolean")) {
					CheckBox c = new CheckBox();
					if (Integer.parseInt(contextualInfo[0]) == 1) c.setChecked(true);
					tb.setWidget(row, 2, c);
				}
				else if (item.getDataType().equals("Date")) {
					DateTimeFormat dateFormat = DateTimeFormat.getLongDateFormat();
				    DateBox dateBox = new DateBox();
				    dateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
				    dateBox.getDatePicker().setYearArrowsVisible(true);
				    
				    Grid g = new Grid(1,7);
				    
					g.setWidget(0, 0, dateBox);
					
					String inf[] = contextualInfo[0].split(":");
					String date[] = inf[0].split("-");
					String time[] = inf[1].split("-");
					
					int year = Integer.parseInt(date[0]) - 1900;
					int month = Integer.parseInt(date[1]) - 1;
					if (month == 0) {month = 12;--year;}
					int day = Integer.parseInt(date[2]);
					Date d = new Date(year, month, day);
					dateBox.setValue(d);
					
					TextBox t = new TextBox();
					t.setWidth("30px");
					t.setText(String.valueOf(time[0]));
					g.setWidget(0, 1, t);
					g.setWidget(0, 2, new Label("hh"));
					t = new TextBox();
					t.setWidth("30px");
					t.setText(String.valueOf(time[1]));
					g.setWidget(0, 3, t);
					g.setWidget(0, 4, new Label("mm"));
					t = new TextBox();
					t.setWidth("30px");
					t.setText(String.valueOf(time[2]));
					g.setWidget(0, 5, t);
					g.setWidget(0, 6, new Label("ss"));
					
					tb.setWidget(row, 2, g);
				}
				else {
					ListBox lb = new ListBox();
					for (int k = 1; k < contextualInfo.length; ++k) {
						lb.addItem(contextualInfo[k]);
					}
					lb.setSelectedIndex(Integer.parseInt(contextualInfo[0]));
					
					tb.setWidget(row, 2, lb);
				}
				++row;
			}
			
		}
		
		userForm.enableFastInsert();
//		v.add( userForm );
		userForm.addFieldListener( new CustomizableForm.FieldListener() {
			@Override
			public void valueChanged( CustomizableForm.CustomField field ) {
				JSONObject o = new JSONObject();
				o.put( "id", new JSONString( field.getName() ) );
				o.put( "target", new JSONString( EntityPropertyPage.this.entity ) );
				o.put( "value", new JSONString( field.getValue() ) );
				o.put( "datatype", new JSONString ( "CUSTOM" ));
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
						//								Window.alert( "Ok" );
					}} );
			}
			
			@Override
			public void labelChanged( CustomField field ) {
				
			}

			@Override
			public void fieldDeleted( CustomField field ) {
				JSONObject o = new JSONObject();
				o.put( "id", new JSONString( field.getName() ) );
				o.put( "target", new JSONString( EntityPropertyPage.this.entity ) );
				JSONArray array = new JSONArray();
				array.set( 0, o );
				RiscossJsonClient.postRiskData( array,  new JsonCallbackWrapper<String>( field.getName() ) {
					@Override
					public void onSuccess( Method method, JSONValue response ) {
						userForm.removeField( getValue() );
					}
					@Override
					public void onFailure( Method method, Throwable exception ) {
						Window.alert( exception.getMessage() );
					}
				});
			}
		});
		summaryPanel.setWidget( v );
		VerticalPanel vPanel = new VerticalPanel();
		vPanel.add(tb);
		vPanel.add(userForm);
		Button save = new Button("SAVE");
		save.setStyleName("button2");
		save.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				for (int i = 0; i < tb.getRowCount(); ++i) {
					JSONObject o = new JSONObject();
					o.put( "id", new JSONString( ((Label)tb.getWidget(i, 0)).getText() ) );
					o.put( "target", new JSONString( EntityPropertyPage.this.entity ) );
					String datatype = ((Label)tb.getWidget(i, 1)).getText();
					String value = "";
					if (datatype.equals("Integer")) {
						value += ((TextBox) tb.getWidget(i, 2)).getText();
						value += extraInfoList.get(i);
					}
					else if (datatype.equals("Boolean")) {
						if (((CheckBox) tb.getWidget(i, 2)).isChecked()) value += "1";
						else value += "0";
					}
					else if (datatype.equals("Date")) {
						int hour = Integer.parseInt(((TextBox) ((Grid) tb.getWidget(i, 2)).getWidget(0, 1)).getText());
						int minute = Integer.parseInt(((TextBox) ((Grid) tb.getWidget(i, 2)).getWidget(0, 3)).getText());
						int second = Integer.parseInt(((TextBox) ((Grid) tb.getWidget(i, 2)).getWidget(0, 5)).getText());
						Date date = ((DateBox) ((Grid) tb.getWidget(i,2)).getWidget(0, 0)).getValue();
						date.setHours(hour);
						date.setMinutes(minute);
						date.setSeconds(second);
						
						DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd:HH-mm-ss");
					    value += fmt.format(date);
					}
					else {
						value += ((ListBox) tb.getWidget(i, 2)).getSelectedIndex();
						value += extraInfoList.get(i);
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
							//								Window.alert( "Ok" );
						}} );
				}
				
			}
			
		});
		//vPanel.add(save);
		dataCollectors.setWidget(confDialog.getDock());
		ciPanel.setWidget( vPanel );
	}

	protected void loadRAS( JSONValue response ) {
		if( rasPanel.getWidget() != null ) {
			rasPanel.getWidget().removeFromParent();
		}
		if( response == null ) {
			rasLoaded = true;
			return;
		}
		try {
			RiskAnalysisReport report = new RiskAnalysisReport();
			report.showResults( response.isObject().get( "results" ).isArray() );
			rasPanel.setWidget( report.asWidget() );
			rasLoaded = true;
		}
		catch( Exception ex ) {
			Window.alert( ex.getMessage() );
		}
	}

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
	
}
