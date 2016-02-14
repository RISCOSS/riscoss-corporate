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

package eu.riscoss.client.riskconfs;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.SimpleRiskCconf;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.ui.LinkHtml;
import eu.riscoss.shared.RiscossUtil;

public class RiskConfsModule implements EntryPoint {
	
	static class RCInfo {
		public RCInfo( String name ) {
			this.name = name;
		}
		String name;
	}
	
	CellTable<RCInfo>			table;
	ListDataProvider<RCInfo>	dataProvider;
	
	SimplePanel					rightPanel2;
	
	VerticalPanel		page = new VerticalPanel();
	HorizontalPanel		mainView = new HorizontalPanel();
	VerticalPanel		leftPanel = new VerticalPanel();
	VerticalPanel		rightPanel = new VerticalPanel();
	VerticalPanel		tablePanel = new VerticalPanel();
	TextBox				riskConfName = new TextBox();
	
	RCPropertyPage 		ppg;
	
	Grid				grid;
	
	JSONValue			riskconfsList;
	JSONObject			currentRC = null;
	String				selectedRC = null;
	
	SimplePanel			spTable;
	
	public RiskConfsModule() {
	}
	
	/* (non-Javadoc)
	 * @see eu.riscoss.client.ActivablePanel#getWidget()
	 */
	public Widget getWidget() {
		return page;
	}
	
	private <C> Column<RCInfo, C> addColumn(Cell<C> cell,final GetValue<C> getter, FieldUpdater<RCInfo, C> fieldUpdater) {
		Column<RCInfo, C> column = new Column<RCInfo, C>(cell) {
			@Override
			public C getValue(RCInfo object) {
				return getter.getValue(object);
			}
		};
		column.setFieldUpdater(fieldUpdater);
		
		return column;
	}
	
	private static interface GetValue<C> {
		C getValue(RCInfo contact);
	}
	
	public native void exportJS() /*-{
	var that = this;
	$wnd.selectRC = $entry(function(amt) {
  	that.@eu.riscoss.client.riskconfs.RiskConfsModule::onRCSelected(Ljava/lang/String;)(amt);
	});
}-*/;
	
	/* (non-Javadoc)
	 * @see eu.riscoss.client.ActivablePanel#activate()
	 */
	public void onModuleLoad() {
		
		exportJS();
		tablePanel = new VerticalPanel();
		spTable = new SimplePanel();
		
		RiscossJsonClient.listRCs( new JsonCallback() {
			public void onSuccess(Method method, JSONValue response) {
				riskconfsList = response;
				loadRiskConfsTable(response);
			}
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		} );
		
		rightPanel2 = new SimplePanel();

		rightPanel2.setSize( "100%", "100%" );
		
		mainView.setStyleName("mainViewLayer");
		mainView.setWidth("100%");
		leftPanel.setStyleName("leftPanelLayer");
		leftPanel.setWidth("400px");
		//leftPanel.setHeight("100%");
		rightPanel.setStyleName("rightPanelLayer");
		
		Label title = new Label("Risk configuration management");
		title.setStyleName("title");
		page.add(title);
		
		HorizontalPanel data = new HorizontalPanel();
		data.setStyleName("layerData");
		Label name = new Label("Name");
		name.setStyleName("text");
		data.add(name);
		riskConfName.setWidth("120px");
		riskConfName.setStyleName("layerNameField");
		data.add(riskConfName);
		leftPanel.add(data);
		
		Button newRisk = new Button("New configuration");
		newRisk.setStyleName("button");
		newRisk.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent arg0) {
				String name = riskConfName.getText().trim();
				for(int i=0; i< riskconfsList.isArray().size(); i++){
					JSONObject o = (JSONObject)riskconfsList.isArray().get(i);
					if (name.equals(o.get( "name" ).isString().stringValue())){
						//info: firefox has some problem with this window, and fires assertion errors in dev mode
						Window.alert("Model name already in use.\nPlease rename file.");
						return;
					}
				}
				if( name == null || "".equals( name ) ) 
					return;
				while (!RiscossUtil.sanitize(name).equals(name)){
					name = Window.prompt( "Name contains prohibited characters (##,@,\") \nRe-enter name:", "" );
					if( name == null || "".equals( name ) ) 
						return;
				}
				
				RiscossJsonClient.createRC( name, new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert( exception.getMessage() );
					}
					
					@Override
					public void onSuccess(Method method, JSONValue response) {
						dataProvider.getList().add( new RCInfo( 
								response.isObject().get( "name" ).isString().stringValue() ) );
						//Window.Location.reload();
					}} );
				onRCSelected(name);
				riskConfName.setText("");
			}
		});
		leftPanel.add(newRisk);
		leftPanel.add(searchFields());
		leftPanel.add(spTable);
		
		mainView.add(leftPanel);
		mainView.add(rightPanel);
		page.add(mainView);
		page.setWidth("100%");

		save = new Button("Save");
		save.setStyleName("button");
		save.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				saveRiskConfData();
			}
		});
		
		RootPanel.get().add( page );
	}
	
	private void loadRiskConfsTable(JSONValue response) {
		tablePanel.clear();
		table = new CellTable<RCInfo>(15, (Resources) GWT.create(TableResources.class));
		
		table.addColumn( new Column<RCInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(RCInfo object) {
				return new LinkHtml( object.name, "javascript:selectRC(\"" + object.name + "\")" ); };
		}, "Risk Configuration");
		table.setWidth("100%");
		dataProvider = new ListDataProvider<RCInfo>();
		dataProvider.addDataDisplay(table);

		GWT.log( response.toString() );
		if( response.isArray() != null ) {
			for( int i = 0; i < response.isArray().size(); i++ ) {
				JSONObject o = (JSONObject)response.isArray().get( i );
				dataProvider.getList().add( new RCInfo( 
						o.get( "name" ).isString().stringValue() ) );
			}
		}

		SimplePager pager = new SimplePager();
	    pager.setDisplay( table );
		
		tablePanel.add( table );
		tablePanel.add( pager );
		tablePanel.setWidth("100%");
		
		spTable.setWidget(tablePanel);
		
	}
	
	TextBox entityFilterQuery = new TextBox();
	String entityQueryString;
	
	private HorizontalPanel searchFields() {
		HorizontalPanel h = new HorizontalPanel();
		
		Label filterlabel = new Label("Search risk configurations: ");
		filterlabel.setStyleName("bold");
		h.add(filterlabel);
		
		entityFilterQuery.setWidth("120px");
		entityFilterQuery.setStyleName("layerNameField");
		h.add(entityFilterQuery);
		
		entityFilterQuery.addKeyUpHandler(new KeyUpHandler() {
			
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (entityFilterQuery.getText() != null){
					String tmp = RiscossUtil.sanitize(entityFilterQuery.getText());
					if (!tmp.equals(entityQueryString)) {
						entityQueryString = tmp;
						RiscossJsonClient.searchRCs(entityQueryString, new JsonCallback() {

							@Override
							public void onFailure(Method method,
									Throwable exception) {
								Window.alert(exception.getMessage());
							}
							@Override
							public void onSuccess(Method method,
									JSONValue response) {
								loadRiskConfsTable(response);
							}
						});
					}
				}
			}
		});
		
		return h;
	}
	
	private void saveRiskConfData() {
		RiscossJsonClient.setRiskConfDescription(selectedRC, description.getText(), new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				ppg.saveRiskConfData();
			}
		});
	}
	
	protected void onAddNew() {
		//String name = Window.prompt( "Name:", "" );
		String name = riskConfName.getText().trim();
		if( name == null || "".equals( name ) ) 
			return;
		
		
		/**Button bOk = new Button( "Ok" ); //, new ClickHandler() {

		bOk.addClickHandler( new ClickWrapper<JSONArray>( (JSONArray)response.isArray() ) {
			@Override
			public void onClick(ClickEvent event) {
				String name = txt.getText().trim();

				if (name == null || name.equals("") ) 
					return;

				//String s = RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
				if (!RiscossUtil.sanitize(name).equals(name)){
					//info: firefox has some problem with this window, and fires asssertion errors in dev mode
					Window.alert("Name contains prohibited characters (##,@,\") \nPlease re-enter name");
					return;
				}

				for(int i=0; i<getValue().size(); i++){
					JSONObject o = (JSONObject)getValue().get(i);
					if (name.equals(o.get( "name" ).isString().stringValue())){
						//info: firefox has some problem with this window, and fires asssertion errors in dev mode
						Window.alert("Layer name already in use.\nPlease re-enter name.");
						return;
					}
				}

				RiscossJsonClient.createEntity( getChosenName(), getChosenLayer(), getChosenParent(), new JsonCallback() {
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert( exception.getMessage() );
					}
					@Override
					public void onSuccess(Method method, JSONValue response) {
						String entity = response.isObject().get( "name" ).isString().stringValue();
						EntityInfo info = new EntityInfo( entity );
						info.setLayer( JsonUtil.getValue( response, "layer", "" ) );
						insertEntityIntoTable( info );
						dialog.hide();
					}
				} );
			}
		});
		grid.setWidget( 3, 1, bOk);	
		
		grid.setWidget( 3, 2, new Button( "Cancel", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				//Window.Location.reload();
				dialog.hide();
			}
		} )) ; */
		
		
		
		
		while (!RiscossUtil.sanitize(name).equals(name)){
			name = Window.prompt( "Name contains prohibited characters (##,@,\") \nRe-enter name:", "" );
			if( name == null || "".equals( name ) ) 
				return;
		}
		
		RiscossJsonClient.createRC( name, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
			
			@Override
			public void onSuccess(Method method, JSONValue response) {
				dataProvider.getList().add( new RCInfo( 
						response.isObject().get( "name" ).isString().stringValue() ) );
			}} );
	}
	
	Button save;
	private VerticalPanel descriptionData;
	private TextBox description;
	
	public void onRCSelected( String item ) {
		
		rightPanel2.clear();
		if( item == null ) return;
		if( "".equals( item ) ) return;
		
		selectedRC = item;
		
		mainView.remove(rightPanel);
		rightPanel = new VerticalPanel();
		rightPanel.setStyleName("rightPanelLayer");
		rightPanel.setWidth("90%");
		
		Label title = new Label(selectedRC);
		title.setStyleName("subtitle");
		rightPanel.add(title);
		
		grid = new Grid(1,2);
		grid.setStyleName("properties");
		
		Label nameL = new Label("Name");
		nameL.setStyleName("bold");
		grid.setWidget(0,0,nameL);
		
		Label nameLy = new Label(selectedRC);
		nameLy.setStyleName("tag");
		grid.setWidget(0, 1, nameLy);
		
		rightPanel.add(grid);
		
		descriptionData = new VerticalPanel();
		descriptionData.setStyleName("description");
		descriptionData.setWidth("100%");
		Label descLabel = new Label("Description");
		descLabel.setStyleName("bold");
		descriptionData.add(descLabel);
		description = new TextBox();
		description.setWidth("100%");
		RiscossJsonClient.getRiskConfDescription(selectedRC, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				description.setText(response.isString().stringValue());
			}
		});
		//description.setWidth("100%");
		descriptionData.add(description);
		
		rightPanel.add(descriptionData);
		
		RiscossJsonClient.getRCContent( selectedRC, new JsonCallback() {
			@Override
			public void onSuccess(Method method, JSONValue response) {
				
				ppg = new RCPropertyPage( new SimpleRiskCconf( response ) );
				HorizontalPanel buttons = new HorizontalPanel();
				Button delete = new Button("Delete");
				delete.setStyleName("button");
				delete.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent arg0) {
						hasRiskSessions();
					}
				});
				buttons.add(save);
				buttons.add(delete);
				
				rightPanel.add(buttons);
				
				rightPanel.add( ppg.asWidget() );
				
				mainView.add(rightPanel);
			}
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	Boolean hasRisk;
	
	protected void hasRiskSessions() {
		hasRisk = false;
		RiscossJsonClient.listRiskAnalysisSessions("", selectedRC	, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert(exception.getMessage());	
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				if (response.isObject().get("list").isArray().size() > 0) hasRisk = true;
				deleteRC();
			}
		});
	}

	protected void deleteRC() {
		if (hasRisk) Window.alert("Risk configurations with associated risk sessions cannot be deleted");
		else {
			Boolean b = Window.confirm("Are you sure that you want to delete risk configuration " + selectedRC + "?");
			if (b) {
				RiscossJsonClient.deleteRC( selectedRC, new JsonCallback() {
					@Override
					public void onFailure(Method method,Throwable exception) {
						Window.alert( exception.getMessage() );
					}
					@Override
					public void onSuccess(Method method,JSONValue response) {
						onRCSelected( null );
						Window.Location.reload();
					}
				} );
			}
		}
	}
	
}
