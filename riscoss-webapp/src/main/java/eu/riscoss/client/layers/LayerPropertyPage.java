package eu.riscoss.client.layers;

import java.util.ArrayList;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.codec.CodecLayerContextualInfo;
import eu.riscoss.shared.JLayerContextualInfo;

public class LayerPropertyPage implements IsWidget {
	
	
	TabPanel			tab				= new TabPanel();
	SimplePanel 		panel 			= new SimplePanel();
	SimplePanel			ciPanel			= new SimplePanel();
	HorizontalPanel		hPanel 			= new HorizontalPanel();
	SimplePanel			ciItemPanel 	= new SimplePanel();
	Grid				ciList			= new Grid(3,1);
	
	
	private String  	layer;
	
	TextBox				name			= new TextBox();
	Button 				add;
	
	HorizontalPanel		integerItem		= new HorizontalPanel();
	ArrayList<TextBox>	elems 			= new ArrayList<>();
	TextBox				min				= new TextBox();
	TextBox				max 			= new TextBox();
	FlexTable			enumeration 	= new FlexTable();
	ArrayList<String>	elements;
	
	ListBox				lBox;
	
	JSONValue			res;
	
	public LayerPropertyPage() {
		tab.add( panel , "Properties");
		tab.add( ciPanel , "Contextual Information");
		tab.selectTab(0);
		tab.setSize( "100%", "100%" );
		tab.setVisible(false);
		
		integerItem.add(new Label("Min"));
		integerItem.add(min);
		integerItem.add(new Label("Max"));
		integerItem.add(max);
		
	}
	
	@Override
	public Widget asWidget() {
		return this.tab;
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
				res = response;
				loadProperties();	
			}
		});
		
	}
	
	protected void loadProperties () {
		
		Grid grid = new Grid(2,2);
		
		grid.setText(0,0,"Name:");
		grid.setText(0,1,layer);
		grid.setText(1,0,"Parent:");
		grid.setText(1,1,"parent_name");
		
		panel.add(grid);
		
		//TODO get existing attributes
		
		this.add = new Button("Add", new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
						
				CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
				JLayerContextualInfo info = codec.decode( res );
				
				if (lBox.getSelectedIndex() == 0) {
					info.addContextualInfoInteger(name.getText(), min.getText(), max.getText());
				}
				
				else if (lBox.getSelectedIndex() == 3) {
					elements = new ArrayList<>();
					for (int i = 0; i < enumeration.getRowCount(); ++i) {
						TextBox elem = (TextBox) enumeration.getWidget(i, 0);
						elements.add(elem.getText());
					}
					info.addContextualInfoList(name.getText(), elements);
				}
				
				else {
					info.addContextualInfoBoolean(name.getText());
				}
				
				RiscossJsonClient.setLayerContextualInfo(layer, info, new JsonCallback() {

					@Override
					public void onFailure(Method method,
							Throwable exception) {
						Window.alert( exception.getMessage());
						
					}

					@Override
					public void onSuccess(Method method,
							JSONValue response) {
						
						//reloadData();
						
					}
					
				});
					
				min.setText("");
				max.setText("");
				name.setText("");
				
			}
			
		});
		
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
					ciItemPanel.setWidget(integerItem);
				}
				
				else if (lBox.getSelectedIndex() == 1) {
					ciList.setWidget(1, 0, null);
					ciItemPanel.setWidget(null);
				}
				
				else if (lBox.getSelectedIndex() == 2) {
					ciList.setWidget(1, 0, null);
					ciItemPanel.setWidget(null);
				}
				
				else {
					enumeration = new FlexTable();
					enumeration.insertRow(0);
					enumeration.insertCell(0, 0);
					
					HorizontalPanel buttons = new HorizontalPanel();
					buttons.setSpacing(5);
					Button addEnum = new Button("Add");
					addEnum.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent arg0) {
							int k = enumeration.getRowCount();
							enumeration.insertRow(k);
							enumeration.insertCell(k,0);
							enumeration.setWidget(k, 0, new TextBox());
						}
						
					});
					Button deleteLastEnum = new Button("Remove");
					deleteLastEnum.addClickHandler(new ClickHandler() {

						@Override
						public void onClick(ClickEvent arg0) {
							int k = enumeration.getRowCount();
							if (k > 1) enumeration.removeRow(k-1);
							
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
		
		min.setWidth("20px");
		max.setWidth("20px");
		
		hPanel.add(new Label("Type"));
		hPanel.add(lBox);
		hPanel.add(new Label("Name"));
		hPanel.add(name);
		
		ciItemPanel.setWidget(integerItem);
		hPanel.add(ciItemPanel);
		hPanel.add(this.add);
		
		hPanel.setSpacing(5);
		ciList.setWidget(0, 0, hPanel);
		
		ciList.setWidget(1, 0, null);
		
		reloadData();
		
		ciPanel.add(ciList);
		
	}
	
	public void reloadData() {
		
		CodecLayerContextualInfo codec = GWT.create( CodecLayerContextualInfo.class );
		JLayerContextualInfo info = codec.decode( res );
		
		FlexTable list = new FlexTable();
		int i;
		name.setText(Integer.toString(info.getSize()));
		for (i = 0; i < info.getSize(); ++i) {
			list.insertRow(i);
			list.insertCell(i, 0);
			list.add(new Label(info.getContextualInfoElement(i).getName()));
		}
		
		ciList.setWidget(2, 0, list);
		
	}
	
}
