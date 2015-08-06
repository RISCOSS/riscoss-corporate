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

import java.util.List;

import javax.swing.SpinnerModel;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
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
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.JsonEntitySummary;
import eu.riscoss.client.JsonRiskDataList;
import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.report.RiskAnalysisReport;
import eu.riscoss.client.ui.CustomizableForm;
import eu.riscoss.client.ui.CustomizableForm.CustomField;
import eu.riscoss.client.ui.EntityBox;

public class EntityPropertyPage implements IsWidget {
	
	class RDCConfDialog {
		
		DialogBox dialog = new DialogBox( false, true );
		DockPanel dock = new DockPanel();
		RDCConfigurationPage ppg = new RDCConfigurationPage();
		
		RDCConfDialog() {
			dock.add( new Button( "Done", new ClickHandler() {
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
					rdcAnchor.setText( str + " ... " );
					
					RiscossJsonClient.saveRDCs( json, entity, new JsonCallback() {
						@Override
						public void onSuccess(Method method, JSONValue response) {
							dialog.hide();
						}
						@Override
						public void onFailure(Method method, Throwable exception) {
							Window.alert( exception.getMessage() );
						}} );
				}} ), DockPanel.NORTH );
			dock.add( ppg.asWidget(), DockPanel.CENTER );
			dialog.setWidget( dock );
		}
		
		public void show(String entity) {
			ppg.setSelectedEntity( entity );
			dialog.show();
		}
		
	}
	
	TabPanel			tab				= new TabPanel();
	SimplePanel			summaryPanel	= new SimplePanel();
	SimplePanel			ciPanel			= new SimplePanel();
	SimplePanel			rasPanel		= new SimplePanel();
	CustomizableForm	userForm 		= new CustomizableForm();
	
	Anchor				rdcAnchor;
	
	private String		entity;
	
	RDCConfDialog		confDialog;
	
	String[]			contextualInfo;
	boolean				rasLoaded		= false;
	
	public EntityPropertyPage() {
		tab.add( summaryPanel, "Properties" );
		tab.add( ciPanel, "Contextual Information" );
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

	protected void loadProperties( JSONValue response ) {
		rasLoaded = false;
		JsonEntitySummary info = new JsonEntitySummary( response );
		VerticalPanel v = new VerticalPanel();
		{
			Grid grid = new Grid( 5, 2 );
			grid.setWidget( 0, 0, new Label( "Name:" ) );
			grid.setWidget( 0, 1, new Label( info.getEntityName() ) );
			grid.setWidget( 1, 0, new Label( "Layer:" ) );
			grid.setWidget( 1, 1, new Label( info.getLayer() ) );
			Label lbl = new Label( "Data collectors:" );
			grid.setWidget( 2, 0, lbl );
			VerticalPanel vp = new VerticalPanel();
			String str = info.getRDCString() + " ... ";
			rdcAnchor = new Anchor( str );
			rdcAnchor.addClickHandler( new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					if( confDialog == null ) {
						confDialog = new RDCConfDialog();
					}
					confDialog.show( EntityPropertyPage.this.entity );
				}
			});
			vp.add( rdcAnchor );
			Anchor a = new Anchor( "Run now" );
			a.addClickHandler( new ClickHandler() {
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
			vp.add( a );
			grid.setWidget( 2, 1, vp );
			
			grid.setWidget( 3, 0, new Label( "Owned by:" ) );
			{
				EntityBox ebox = new EntityBox();
				JSONArray parents = info.getParentList();
				for( int i = 0; i < parents.size(); i++ ) {
					String p = parents.get( i ).isString().stringValue();
					ebox.add( p );
				}
				ebox.addListener( new EntityBox.Listener() {
					@Override
					public void entitySelected( List<String> entities ) {
						onParentEntitySelected( entities );
					}
				});;
				grid.setWidget( 3, 1, ebox );
			}
			{
				grid.setWidget( 4, 0, new Label( "Owns:" ) );
				EntityBox cbox = new EntityBox();
				JSONArray children = info.getChildrenList();
				for( int i = 0; i < children.size(); i++ ) {
					String p = children.get( i ).isString().stringValue();
					cbox.add( p );
				}
				cbox.addListener( new EntityBox.Listener() {
					@Override
					public void entitySelected( List<String> entities ) {
						onChildEntitySelected( entities );
					}
				});
				grid.setWidget( 4, 1, cbox );
			}
			v.add( grid );
			grid.setWidth( "100%" );
			v.setWidth( "100%" );
			grid.getColumnFormatter().setWidth( 0, "20%" );
			grid.getColumnFormatter().setWidth( 1, "80%" );
		}
		/*userForm = new FlexTable();
		for( int i = 0; i < info.getUserData().size(); i++ ) {
			JsonRiskDataList.RiskDataItem item = info.getUserData().get( i );
			userForm.insertRow(i);
			userForm.insertCell(i, 0);
			userForm.insertCell(i, 1);
			userForm.insertCell(i, 2);
			userForm.setWidget(i, 0, new Label(item.getId()));
			userForm.setWidget(i, 1, new Label(item.getDataType()));
			TextBox tb = new TextBox();
			String vv = item.getValue();
			String[] values = vv.split(";");
			contextualInfo = "";
			for (int k = 1; k < values.length; ++k) contextualInfo+=values[k];
			tb.setText(values[0]);
			userForm.setWidget(i, 2, tb);
	
		}
		userForm.insertRow(info.getUserData().size());
		userForm.insertCell(info.getUserData().size(), 0);
		Button save = new Button("Save");
		userForm.setWidget(info.getUserData().size(), 0, save);
		save.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				
				int k;
				JSONArray array = new JSONArray();
				for (k = 0; k < userForm.getRowCount()-1; k++) {
					String type = ((Label) userForm.getWidget(k, 1)).getText();
					String value = ((TextBox) userForm.getWidget(k, 2)).getText();
					value+=contextualInfo;
					JSONObject o = new JSONObject();
					o.put( "id", new JSONString( ((Label) userForm.getWidget(k, 0)).getText() ) );
					o.put( "target", new JSONString( entity ) );
					o.put( "value", new JSONString( value ) );
					o.put( "type", new JSONString( "custom" ) );
					o.put( "datatype", new JSONString( type ) );
					o.put( "origin", new JSONString( "user" ) );
					array.set( k, o );
				}
				RiscossJsonClient.postRiskData( array, new JsonCallback() {
					@Override
					public void onFailure( Method method, Throwable exception ) {
						Window.alert( exception.getMessage() );
					}
					@Override
					public void onSuccess( Method method, JSONValue response ) {
							//						Window.alert( "Ok" );
					}} );
			}
			
		});
		
		summaryPanel.setWidget( v );
		ciPanel.setWidget( userForm );*/
		
		userForm = new CustomizableForm();
		for( int i = 0; i < info.getUserData().size(); i++ ) {
			JsonRiskDataList.RiskDataItem item = info.getUserData().get( i );
			String val = item.getValue();
			contextualInfo = val.split(";");
			
			if (item.getDataType().equals("List")) val = contextualInfo[Integer.parseInt(contextualInfo[0])+1];
			userForm.addField( item.getId(), contextualInfo[0] );
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
				o.put( "type", new JSONString( "NUMBER" ) );
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
				// TODO Auto-generated method stub
				
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
		ciPanel.setWidget( userForm );
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

	protected void onParentEntitySelected( List<String> entities ) {
		RiscossJsonClient.setParent( entity, entities, new JsonCallback() {
			@Override
			public void onFailure( Method method, Throwable exception ) {
				Window.alert( exception.getMessage() );
			}

			@Override
			public void onSuccess( Method method, JSONValue response ) {
				// TODO Auto-generated method stub
				
			}} );
	}
	
}
