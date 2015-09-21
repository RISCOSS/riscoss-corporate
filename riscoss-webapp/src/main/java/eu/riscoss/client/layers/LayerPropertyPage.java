package eu.riscoss.client.layers;

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
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.datepicker.client.DateBox;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SingleSelectionModel;

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecLayerContextualInfo;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.shared.JLayerContextualInfo;
import eu.riscoss.shared.JLayerContextualInfoElement;
import eu.riscoss.shared.RiscossUtil;

public class LayerPropertyPage implements IsWidget {
	
	
	TabPanel			tab				= new TabPanel();
	SimplePanel 		panel 			= new SimplePanel();
	SimplePanel			ciPanel			= new SimplePanel();
	HorizontalPanel		hPanel 			= new HorizontalPanel();
	VerticalPanel		vPanel			= new VerticalPanel();
	HorizontalPanel		cInfoPanel		= new HorizontalPanel();
	SimplePanel			ciItemPanel 	= new SimplePanel();
	Grid				ciList			= new Grid(3,1);
	Grid				newElement;
	PopupPanel	 		simplePopup 	= new PopupPanel(true);
	
	
	private String  	layer;
	private	String		parent;
	
	TextBox				name			= new TextBox();
	TextBox				id				= new TextBox();
	TextBox				description		= new TextBox();
	SimplePanel			defvalue 		= new SimplePanel();
	String				defvaluestring 	= new String();
	DateBox				dateBox;
	Button 				add;
	Button				newElemButton;
	
	HorizontalPanel		integerItem		= new HorizontalPanel();
	ArrayList<TextBox>	elems 			= new ArrayList<>();
	TextBox				min				= new TextBox();
	TextBox				max 			= new TextBox();
	FlexTable			enumeration 	= new FlexTable();
	ArrayList<String>	elements;
	FlexTable			list;
	int 				count;
	
	ListBox				lBox;
	
	CellTable<String>	table = new CellTable<String>(15, (Resources) GWT.create(TableResources.class));
	
	JLayerContextualInfo			info;
	JLayerContextualInfoElement 	jElement;
	
	public LayerPropertyPage() {
		
		//tab.add( panel , "Properties");
		tab.add( ciPanel , "Contextual Information");
		tab.selectTab(0);
		tab.setSize( "100%", "100%" );
		tab.setVisible(false);
		
		Label minL = new Label("Min");
		minL.setStyleName("bold2");
		integerItem.add(minL);
		integerItem.add(min);
		Label maxL = new Label("Max");
		maxL.setStyleName("bold2");
		integerItem.add(maxL);
		integerItem.add(max);
		
		newType.addItem("Integer");
		newType.addItem("Boolean");
		newType.addItem("Date");
		newType.addItem("List");
		newDefValBool.addItem("false");
		newDefValBool.addItem("true");
		newHour.setWidth("20px");
		newMinute.setWidth("20px");
		newSecond.setWidth("20px");
		
		simplePopup.setPopupPosition(500, 100);
		simplePopup.setWidth("180px");
		simplePopup.setHeight("50px");
	}
	
	@Override
	public Widget asWidget() {
		return this.tab;
	}
	
	public void setParent( String parent ) {
		this.parent = parent;
	}
	
	public String getParent( ) {
		return this.parent;
	}
	
	public void setSelectedLayer( String layer ) {
		
		if (panel.getWidget() != null) {
			panel.getWidget().removeFromParent();
		}
		
		if (ciPanel.getWidget() != null) {
			ciPanel.getWidget().removeFromParent();
		}
		
		this.layer = layer;
		
		if (this.layer == null) {
			return;
		}
		
		tab.setVisible(true);
		
		RiscossJsonClient.getLayerContextualInfo(layer, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				if( response != null ) {
					CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
					info = codec.decode( response );
				}
				else {
					info = new JLayerContextualInfo();
				}
				loadProperties();	
			}
		});
		
	}
	
	protected void loadProperties () {
		
		Grid grid = new Grid(2,2);
		
		Label nameL = new Label("Name:");
		nameL.setStyleName("tag");
		grid.setWidget(0,0,nameL);
		TextBox tb = new TextBox();
		tb.setText(layer);
		tb.setWidth("120px");
		grid.setWidget(0,1,tb);
		Label parentN = new Label("Parent:");
		parentN.setStyleName("tag");
		grid.setWidget(1,0,parentN);
		Label parentNv = new Label(parent);
		parentNv.setStyleName("greentag");
		grid.setWidget(1,1,parentNv);
		
		panel.add(grid);
		
		this.add = new Button("Add", new ClickHandler() {

			@SuppressWarnings("deprecation")
			@Override
			public void onClick(ClickEvent arg0) {
				
				if (id.getText().equals("") || name.getText().equals("") || description.getText().equals("")) {
					simplePopup.setWidget(new Label("No field can be empty"));
					simplePopup.show();
					return;
				}
				
				int type = lBox.getSelectedIndex();
				if (type == 0) {
					if (min.getText().equals("") || max.getText().equals(""))  {
						simplePopup.setWidget(new Label("No field can be empty"));
						simplePopup.show();
						return;
					}
					if (Integer.parseInt(min.getText()) > Integer.parseInt(max.getText())) {
						simplePopup.setWidget(new Label("Min cannot be greater than max"));
						simplePopup.show();
						return;
					}
					defvaluestring = ((TextBox) defvalue.getWidget()).getText();
					if (Integer.parseInt(defvaluestring) > Integer.parseInt(max.getText()) ||
							Integer.parseInt(defvaluestring) < Integer.parseInt(min.getText())) {
						simplePopup.setWidget(new Label("Value must be in limits"));
						simplePopup.show();
						return;
					}
					info.addContextualInfoInteger(id.getText(), name.getText(), description.getText(), defvaluestring, min.getText(), max.getText());
					((TextBox) defvalue.getWidget()).setText("");
				}
				
				else if (type == 1) {
					defvaluestring = String.valueOf(((ListBox) defvalue.getWidget()).getSelectedIndex());
					info.addContextualInfoBoolean(id.getText(), name.getText(), description.getText(), defvaluestring);
				}
				
				else if (type == 2) {
					if (((TextBox) ((Grid) defvalue.getWidget()).getWidget(0, 1)).getText().equals("") ||
							((TextBox) ((Grid) defvalue.getWidget()).getWidget(0, 3)).getText().equals("") ||
							((TextBox) ((Grid) defvalue.getWidget()).getWidget(0, 5)).getText().equals("")) {
						simplePopup.setWidget(new Label("No field can be empty"));
						simplePopup.show();
						return;
					}
					int hour = Integer.parseInt(((TextBox) ((Grid) defvalue.getWidget()).getWidget(0, 1)).getText());
					int minute = Integer.parseInt(((TextBox) ((Grid) defvalue.getWidget()).getWidget(0, 3)).getText());
					int second = Integer.parseInt(((TextBox) ((Grid) defvalue.getWidget()).getWidget(0, 5)).getText());
					Date date = dateBox.getValue();
					date.setHours(hour);
					date.setMinutes(minute);
					date.setSeconds(second);
					
					DateTimeFormat fmt = DateTimeFormat.getFormat("yyyy-MM-dd:HH-mm-ss");
				    defvaluestring = fmt.format(date);
					
					info.addContextualInfoCalendar(id.getText(), name.getText(), description.getText(), defvaluestring);
				}
				
				else {
					elements = new ArrayList<>();
					int rCount = enumeration.getRowCount();
					for (int i = 1; i < rCount; ++i) {
						TextBox elem = (TextBox) enumeration.getWidget(i, 0);
						elements.add(elem.getText());
					}
					defvaluestring = String.valueOf(((ListBox) defvalue.getWidget()).getSelectedIndex());
					info.addContextualInfoList(id.getText(), name.getText(), description.getText(), defvaluestring , elements);
					Widget w = enumeration.getWidget(0,0);
					enumeration.removeAllRows();;
					enumeration.insertRow(0);
					enumeration.insertCell(0, 0);
					enumeration.setWidget(0, 0, w);
					
					defvalue.setWidget(new ListBox());
				}
				
				CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
				JSONValue json = codec.encode( info );
				
				RiscossJsonClient.setLayerContextualInfo(layer, json, new JsonCallback() {
					@Override
					public void onFailure( Method method, Throwable exception ) {
						Window.alert( exception.getMessage());
					}
					@Override
					public void onSuccess( Method method, JSONValue response ) {
						
					}
					
				});
				
				RiscossJsonClient.listEntities(layer, new JsonCallback() {
					
					public void onSuccess(Method method, JSONValue response) {
						int type = lBox.getSelectedIndex();
						if( response.isArray() != null ) {
							for( int i = 0; i < response.isArray().size(); i++ ) {
								JSONObject ent = (JSONObject)response.isArray().get( i );
								String entity = ent.get( "name" ).isString().stringValue();
								JSONObject o = new JSONObject();
								o.put( "id", new JSONString( id.getText() ) );
								o.put( "target", new JSONString( entity ) );
								String value = defvaluestring;
								if (type == 0) value+=";"+min.getText()+";"+max.getText();
								else if (type == 3) {
									for (int k = 0; k < elements.size(); ++k) {
										value+=";"+elements.get(k);
									}
								}
								o.put( "value", new JSONString( value ) );
								o.put( "type", new JSONString( "custom" ) );
								o.put( "datatype", new JSONString( lBox.getItemText(type) ));
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
									}} );
							}
						}
						
						reloadData();
						contextualInfoPanel(id.getText());
						name.setText("");
						id.setText("");
						description.setText("");
						min.setText("");
						max.setText("");
					}
					
					public void onFailure(Method method, Throwable exception) {
						Window.alert( exception.getMessage() );
					}
				});
				
			}
			
		});
		this.add.setStyleName("deleteButton");
		
		newElement = new Grid(4,1);
		
		HorizontalPanel hPanel = new HorizontalPanel();
		lBox = new ListBox();
		lBox.addItem("Integer");
		lBox.addItem("Boolean");
		lBox.addItem("Date");
		lBox.addItem("List");
		lBox.setSelectedIndex(0);
		
		lBox.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent arg0) {
				
				if (lBox.getSelectedIndex() == 0) {
					ciList.setWidget(1, 0, null);
					TextBox tb = new TextBox();
					tb.setWidth("40px");
					defvalue.setWidget(tb);
					ciItemPanel.setWidget(integerItem);
				}
				
				else if (lBox.getSelectedIndex() == 1) {
					ciList.setWidget(1, 0, null);
					ListBox lb = new ListBox();
					lb.addItem("false");
					lb.addItem("true");
					defvalue.setWidget(lb);
					ciItemPanel.setWidget(null);
				}
				
				else if (lBox.getSelectedIndex() == 2) {
					ciList.setWidget(1, 0, null);
					Grid g = new Grid(1,7);
					g.setCellSpacing(5);
					
					TextBox tb = new TextBox();
					tb.setWidth("30px");
					DateTimeFormat dateFormat = DateTimeFormat.getLongDateFormat();
				    dateBox = new DateBox();
				    dateBox.setFormat(new DateBox.DefaultFormat(dateFormat));
				    dateBox.getDatePicker().setYearArrowsVisible(true);
				    
					g.setWidget(0, 0, dateBox);
					
					tb = new TextBox();
					tb.setWidth("30px");
					g.setWidget(0, 1, tb);
					Label hh = new Label("hh");
					hh.setStyleName("bold2");
					g.setWidget(0, 2, hh);
					tb = new TextBox();
					tb.setWidth("30px");
					g.setWidget(0, 3, tb);
					Label mm = new Label("mm");
					mm.setStyleName("bold2");
					g.setWidget(0, 4, mm);
					tb = new TextBox();
					tb.setWidth("30px");
					g.setWidget(0, 5, tb);
					Label ss = new Label("ss");
					ss.setStyleName("bold2");
					g.setWidget(0, 6, ss);
					
					defvalue.setWidget(g);
					ciItemPanel.setWidget(null);
				}
				
				else {
					enumeration = new FlexTable();
					enumeration.insertRow(0);
					enumeration.insertCell(0, 0);
					defvalue.setWidget(new ListBox());
					
					HorizontalPanel buttons = new HorizontalPanel();
					buttons.setSpacing(5);
					Button addEnum = new Button("+");
					addEnum.setStyleName("deleteButton");
					addEnum.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent arg0) {
							int k = enumeration.getRowCount();
							enumeration.insertRow(k);
							enumeration.insertCell(k,0);
							enumeration.setWidget(k, 0, new TextBox());
							((ListBox) defvalue.getWidget()).addItem(String.valueOf(k));
						}
						
					});
					Button deleteLastEnum = new Button("-");
					deleteLastEnum.setStyleName("deleteButton");
					deleteLastEnum.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent arg0) {
							int k = enumeration.getRowCount();
							if (k > 1) enumeration.removeRow(k-1);
							if (k > 0) ((ListBox) defvalue.getWidget()).removeItem(k-1);
						}
						
					});
					buttons.add(addEnum);
					buttons.add(deleteLastEnum);
					enumeration.setWidget(0, 0, buttons);
					ciList.setWidget(1, 0, enumeration);
					ciItemPanel.setWidget( null );
				}
				
			}
			
		});
		
		min.setWidth("30px");
		max.setWidth("30px");
		id.setWidth("100px");
		name.setWidth("100px");
		description.setWidth("315px");
		
		Label type = new Label("Type");
		type.setStyleName("bold2");
		hPanel.add(type);
		hPanel.add(lBox);
		Label idL = new Label("ID");
		idL.setStyleName("bold2");
		hPanel.add(idL);
		hPanel.add(id);
		Label nameTag = new Label("Name");
		nameTag.setStyleName("bold2");
		hPanel.add(nameTag);
		hPanel.add(name);
		
		newElement.setWidget(0, 0, hPanel);
		
		HorizontalPanel hPanel2 = new HorizontalPanel();
		
		Label descriptionL = new Label("Description");
		descriptionL.setStyleName("bold2");
		hPanel2.add(descriptionL);
		hPanel2.add(description);
		newElement.setWidget(1, 0, hPanel2);
		
		HorizontalPanel hPanel3 = new HorizontalPanel();
		TextBox tb2 = new TextBox();
		tb2.setWidth("40px");
		defvalue.setWidget(tb2);
		Label defvalTag = new Label("Default value");
		defvalTag.setStyleName("bold2");
		hPanel3.add(defvalTag);
		hPanel3.add(defvalue);
		ciItemPanel.setWidget(integerItem);
		hPanel3.add(ciItemPanel);
		newElement.setWidget(2, 0, hPanel3);
		newElement.setWidget(3, 0, this.add);
		
		ciList.setWidget(0, 0, newElement);
		
		hPanel.setSpacing(5);
		hPanel2.setSpacing(5);
		hPanel3.setSpacing(5);
		
		ciList.setWidget(1, 0, null);
		
		reloadData();
		
		ciPanel.add(ciList);
		
	}
	
	public void reloadData() {
		
		List<String> cInfo = new ArrayList<>();
		for (int i = 0; i < info.getSize(); ++i) {
			cInfo.add(info.getContextualInfoElement(i).getId());
		}
		
		table = new CellTable<String>(15, (Resources) GWT.create(TableResources.class));
		TextColumn<String> t = new TextColumn<String>() {
			@Override
			public String getValue(String arg0) {
				return arg0;
			}
		};
		
		final SingleSelectionModel<String> selectionModel = new SingleSelectionModel<String>();
	    table.setSelectionModel(selectionModel);
	    selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent arg0) {
				contextualInfoPanel(selectionModel.getSelectedObject());
			}
	    });
		
		table.addColumn(t, "Contextual Information");
		
		if (cInfo.size() > 0) table.setRowData(0, cInfo);
		else {
			cInfo.add("");
			table.setRowData(0, cInfo);
		}
		table.setStyleName("table");
		table.setWidth("300px");
		cInfoPanel = new HorizontalPanel();
		cInfoPanel.add(table);
		ciList.setWidget(2, 0, cInfoPanel);
		
	}
	
	TextBox newId = new TextBox();
	TextBox newName = new TextBox();
	ListBox newType = new ListBox();
	TextBox newDescription = new TextBox();
	TextBox newDefValInt = new TextBox();
	TextBox newMin = new TextBox();
	TextBox newMax = new TextBox();
	ListBox newDefValBool = new ListBox();
	ListBox newDefValList = new ListBox();
	DateBox	newDate = new DateBox();
	TextBox newHour = new TextBox();
	TextBox newMinute = new TextBox();
	TextBox newSecond = new TextBox();
	int element;
	
	private void contextualInfoPanel(String contextualInfo) {
		jElement = null;
		for (int i = 0; i < info.getSize(); ++i) {
			if (info.getContextualInfoElement(i).getId().equals(contextualInfo)) {
				count = i;
				jElement = info.getContextualInfoElement(i);
			}
		}
		cInfoPanel.remove(vPanel);
		vPanel = new VerticalPanel();
		
		Label title = new Label(jElement.getId() + " (" + jElement.getType() + ")");
		title.setStyleName("smallTitle");
		vPanel.add(title);
		
		Button delete = new Button("Delete");
		delete.setStyleName("deleteButton");
		delete.addClickHandler(new ClickHandler() {
			
			String idEnt = jElement.getId();
			int i = count;
			@Override
			public void onClick(ClickEvent arg0) {
					
				RiscossJsonClient.listEntities(layer, new JsonCallback() {
					public void onSuccess(Method method, JSONValue response) {
						if( response.isArray() != null ) {
							
							for( int k = 0; k < response.isArray().size(); k++ ) {
								
								JSONObject ent = (JSONObject)response.isArray().get( k );
								String entity = ent.get( "name" ).isString().stringValue();
								
								JSONObject o = new JSONObject();
								o.put( "id", new JSONString( idEnt ) );
								o.put( "target", new JSONString( entity ) );
								JSONArray array = new JSONArray();
								array.set( 0, o );
								RiscossJsonClient.postRiskData( array,  new JsonCallbackWrapper<String>( idEnt ) {
									@Override
									public void onSuccess( Method method, JSONValue response ) {
										
									}
									@Override
									public void onFailure( Method method, Throwable exception ) {
										Window.alert( exception.getMessage() );
									}
								});
							}
							
						}
					}
					
					public void onFailure(Method method, Throwable exception) {
						Window.alert( exception.getMessage() );
					}
				});
				
				info.deleteContextualInfoElement(i);
				CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
				JSONValue json = codec.encode( info );
				RiscossJsonClient.setLayerContextualInfo(layer, json, new JsonCallback() {

					@Override
					public void onFailure(Method method,
							Throwable exception) {
						Window.alert( exception.getMessage());
					}

					@Override
					public void onSuccess(Method method,
							JSONValue response) {
						reloadData();
					}
					
				});	
			}
		});
		vPanel.add(delete);
		
		Grid g = new Grid(4,2);
		
		Label idL = new Label("ID");
		idL.setStyleName("bold");
		g.setWidget(0, 0, idL);
		newId.setText(jElement.getId());
		newId.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				int elem = count;
				if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
					String id = newId.getText();
					if (id == null || id.equals("") ) 
						return;
					if (!RiscossUtil.sanitize(id).equals(id)){
						Window.alert("Id contains prohibited characters (##,@,\") \nPlease re-enter name");
						return;
					}
					for (int i = 0; i < info.getSize(); ++i) {
						if (info.getContextualInfoElement(i).getId().equals(id)) {
							//Window.alert("Id already in use");
							return;
						}
					}
					info.getContextualInfoElement(elem).setId(id);
					CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
					JSONValue json = codec.encode( info );
					RiscossJsonClient.setLayerContextualInfo(layer, json, new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							String id = newId.getText();
							reloadData();
							contextualInfoPanel(id);
						}
					});
				}
			}
		});
		g.setWidget(0, 1, newId);
		
		Label nameL = new Label("Name");
		nameL.setStyleName("bold");
		g.setWidget(1, 0, nameL);
		newName.setText(jElement.getName());
		newName.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				int elem = count;
				if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
					String name = newName.getText();
					if (name == null || name.equals("") ) 
						return;
					if (!RiscossUtil.sanitize(name).equals(name)){
						Window.alert("Id contains prohibited characters (##,@,\") \nPlease re-enter name");
						return;
					}
					info.getContextualInfoElement(elem).setName(name);
					CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
					JSONValue json = codec.encode( info );
					element = elem;
					RiscossJsonClient.setLayerContextualInfo(layer, json, new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							reloadData();
							contextualInfoPanel(info.getContextualInfoElement(element).getId());
						}
					});
				}
			}
		});
		g.setWidget(1, 1, newName);
		
		Label descriptionL = new Label("Description");
		descriptionL.setStyleName("bold");
		g.setWidget(2, 0, descriptionL);
		newDescription.setText(jElement.getDescription());
		newDescription.addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				int elem = count;
				if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
					String description = newDescription.getText();
					if (description == null || description.equals("") ) 
						return;
					if (!RiscossUtil.sanitize(description).equals(description)){
						Window.alert("Id contains prohibited characters (##,@,\") \nPlease re-enter name");
						return;
					}
					info.getContextualInfoElement(elem).setDescription(description);
					CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
					JSONValue json = codec.encode( info );
					element = elem;
					RiscossJsonClient.setLayerContextualInfo(layer, json, new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							reloadData();
							contextualInfoPanel(info.getContextualInfoElement(element).getId());
						}
					});
				}
			}
		});
		g.setWidget(2, 1, newDescription);
		
		Label defvalL = new Label("Default value");
		defvalL.setStyleName("bold");
		g.setWidget(3, 0, defvalL);
		String s = jElement.getDefval();
		
		if (jElement.getType().equals("Integer")) {
			newDefValInt.setText(s);
			newDefValInt.addKeyPressHandler(new KeyPressHandler() {
				@Override
				public void onKeyPress(KeyPressEvent event) {
					int elem = count;
					if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
						String s = newDefValInt.getText();
						if (s == null || s.equals("") ) 
							return;
						if (!RiscossUtil.sanitize(s).equals(s)){
							Window.alert("Id contains prohibited characters (##,@,\") \nPlease re-enter name");
							return;
						}
						if (Integer.parseInt(s) < Integer.parseInt(info.getContextualInfoElement(elem).getInfo().get(0)) ||
								Integer.parseInt(s) > Integer.parseInt(info.getContextualInfoElement(elem).getInfo().get(1))) {
							Window.alert("Default value must be within limits of min and max");
							return;
						}
						info.getContextualInfoElement(elem).setDefval(s);
						CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
						JSONValue json = codec.encode( info );
						element = elem;
						RiscossJsonClient.setLayerContextualInfo(layer, json, new JsonCallback() {
							@Override
							public void onFailure(Method method, Throwable exception) {
							}
							@Override
							public void onSuccess(Method method, JSONValue response) {
								reloadData();
								contextualInfoPanel(info.getContextualInfoElement(element).getId());
							}
						});
					}
				}
			});
			g.setWidget(3, 1, newDefValInt);
			g.resize(6, 2);
			newMin.setText(jElement.getInfo().get(0));
			newMin.addKeyPressHandler(new KeyPressHandler() {
				@Override
				public void onKeyPress(KeyPressEvent event) {
					int elem = count;
					if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
						String s = newMin.getText();
						if (s == null || s.equals("") ) 
							return;
						if (!RiscossUtil.sanitize(s).equals(s)){
							Window.alert("Id contains prohibited characters (##,@,\") \nPlease re-enter name");
							return;
						}
						if (Integer.parseInt(s) > Integer.parseInt(info.getContextualInfoElement(elem).getDefval()) ||
								Integer.parseInt(s) > Integer.parseInt(info.getContextualInfoElement(elem).getInfo().get(1))) {
							Window.alert("Default value and max must be greater than min value");
							return;
						}
						info.getContextualInfoElement(elem).getInfo().set(0, s);
						CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
						JSONValue json = codec.encode( info );
						element = elem;
						RiscossJsonClient.setLayerContextualInfo(layer, json, new JsonCallback() {
							@Override
							public void onFailure(Method method, Throwable exception) {
							}
							@Override
							public void onSuccess(Method method, JSONValue response) {
								reloadData();
								contextualInfoPanel(info.getContextualInfoElement(element).getId());
							}
						});
					}
				}
			});
			newMax.setText(jElement.getInfo().get(1));
			newMax.addKeyPressHandler(new KeyPressHandler() {
				@Override
				public void onKeyPress(KeyPressEvent event) {
					int elem = count;
					if (KeyCodes.KEY_ENTER == event.getNativeEvent().getKeyCode()) {
						String s = newMax.getText();
						if (s == null || s.equals("") ) 
							return;
						if (!RiscossUtil.sanitize(s).equals(s)){
							Window.alert("Id contains prohibited characters (##,@,\") \nPlease re-enter name");
							return;
						}
						if (Integer.parseInt(s) < Integer.parseInt(info.getContextualInfoElement(elem).getDefval()) ||
								Integer.parseInt(s) < Integer.parseInt(info.getContextualInfoElement(elem).getInfo().get(0))) {
							Window.alert("Default value and min must be less than min value");
							return;
						}
						info.getContextualInfoElement(elem).getInfo().set(1, s);
						CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
						JSONValue json = codec.encode( info );
						element = elem;
						RiscossJsonClient.setLayerContextualInfo(layer, json, new JsonCallback() {
							@Override
							public void onFailure(Method method, Throwable exception) {
							}
							@Override
							public void onSuccess(Method method, JSONValue response) {
								reloadData();
								contextualInfoPanel(info.getContextualInfoElement(element).getId());
							}
						});
					}
				}
			});
			Label minLab = new Label("Min");
			minLab.setStyleName("bold");
			g.setWidget(4, 0, minLab);
			g.setWidget(4, 1, newMin);
			Label maxLab = new Label("Max");
			maxLab.setStyleName("bold");
			g.setWidget(5, 0, maxLab);
			g.setWidget(5, 1, newMax);
		}
		else if (jElement.getType().equals("Boolean")) {
			newDefValBool.setSelectedIndex(Integer.parseInt(s));
			newDefValBool.addChangeHandler(new ChangeHandler() {
				int elem = count;
				@Override
				public void onChange(ChangeEvent event) {
					info.getContextualInfoElement(elem).setDefval(String.valueOf(newDefValBool.getSelectedIndex()));
					CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
					JSONValue json = codec.encode( info );
					element = elem;
					RiscossJsonClient.setLayerContextualInfo(layer, json, new JsonCallback() {
						@Override
						public void onFailure(Method method, Throwable exception) {
						}
						@Override
						public void onSuccess(Method method, JSONValue response) {
							reloadData();
							contextualInfoPanel(info.getContextualInfoElement(element).getId());
						}
					});
				}
			});
			g.setWidget(3, 1, newDefValBool);
		}
		else if (jElement.getType().equals("Date")) {
			DateTimeFormat dateFormat = DateTimeFormat.getLongDateFormat();
		    newDate = new DateBox();
		    newDate.setFormat(new DateBox.DefaultFormat(dateFormat));
		    newDate.getDatePicker().setYearArrowsVisible(true);
		    
		    VerticalPanel dfval = new VerticalPanel();
		    
			dfval.add(newDate);
			
			String inf[] = jElement.getDefval().split(":");
			String date[] = inf[0].split("-");
			String time[] = inf[1].split("-");
			
			int year = Integer.parseInt(date[0]) - 1900;
			int month = Integer.parseInt(date[1]) - 1;
			if (month == 0) {month = 12;--year;}
			int day = Integer.parseInt(date[2]);
			Date d = new Date(year, month, day);
			newDate.setValue(d);
			
			HorizontalPanel timePanel = new HorizontalPanel();
			newHour.setText(String.valueOf(time[0]));
			timePanel.add(newHour);
			timePanel.add(new Label("hh"));
			newMinute.setText(String.valueOf(time[1]));
			timePanel.add(newMinute);
			timePanel.add(new Label("mm"));
			newSecond.setText(String.valueOf(time[2]));
			timePanel.add(newSecond);
			timePanel.add(new Label("ss"));
			
			dfval.add(timePanel);
			g.setWidget(3, 1, dfval);
		}
		else {
			for (int i = 0; i < jElement.getInfo().size(); ++i) {
				newDefValList.addItem(jElement.getInfo().get(i));
			}
			newDefValList.setSelectedIndex(Integer.parseInt(s));
			g.setWidget(3, 1, newDefValList);
		}
		
		vPanel.add(g);
		vPanel.setStyleName("rightPanelLayer");
		
		cInfoPanel.add(vPanel);
	}
	
}
