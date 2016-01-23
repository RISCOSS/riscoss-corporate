package eu.riscoss.client.ui;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;

import eu.riscoss.client.JsonCallbackWrapper;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.entities.TableResources;
import eu.riscoss.client.riskanalysis.JsonRiskAnalysis;
import eu.riscoss.shared.Pair;
import eu.riscoss.shared.RiscossUtil;

public class ContextualInfoTable {
	
	CellTable<Pair<String, String>>			table;
	ListDataProvider<Pair<String, String>>	dataProvider;
	SimplePager pager;
	
	VerticalPanel tablePanel = new VerticalPanel();
	
	//Llista d'elements 
	List<Pair<String, String>> elements;
	List<String> removeElements;
	
	Column<Pair<String, String>, String> idC;
	Column<Pair<String, String>, String> valueC;
	
	Button deleteItem;
	int selectedItem;

	public ContextualInfoTable() {
		//Generate UI table
		elements = new ArrayList<>();
		removeElements = new ArrayList<>();
		
		table = new CellTable<>(15, (Resources) GWT.create(TableResources.class));
		
		idC = new Column<Pair<String, String>, String>(new TextCell()) {
			@Override
			public String getValue(Pair<String, String> arg0) {
				return arg0.getLeft();
			}
		};
		valueC = new Column<Pair<String, String>, String>(new TextInputCell()) {
			@Override
			public String getValue(Pair<String, String> object) {
				return object.getRight();
			}
		};
		
		valueC.setFieldUpdater(new FieldUpdater<Pair<String, String>, String>() {
			@Override
			public void update(int index, Pair<String, String> object,
					String value) {
				Pair<String, String> p = new Pair<String,String>(object.getLeft(), value);
				dataProvider.getList().set(index, p);
			}
		});
		
		deleteItem = new Button("Delete");
		deleteItem.setStyleName("button");
		deleteItem.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				removeField();
			}
		});
		
		final SelectionModel<Pair<String, String>> selectionModel = new SingleSelectionModel<Pair<String, String>>();
	    table.setSelectionModel(selectionModel);
	    selectionModel.addSelectionChangeHandler(new Handler() {
			@Override
			public void onSelectionChange(SelectionChangeEvent arg0) {
				List<Pair<String, String>> l = dataProvider.getList();
				for (int i = 0; i < l.size(); ++i) {
					if (selectionModel.isSelected(l.get(i))) {
						selectedItem = i;
						tablePanel.remove(deleteItem);
						tablePanel.add(deleteItem);
					}
				}
			}
	    });
		
		table.addColumn(idC, "ID");
		table.addColumn(valueC, "Value");
		
		pager = new SimplePager();
	    pager.setDisplay( table );
	    table.setWidth("100%");
	}
	
	public void addField(String id, String value) {
		Pair p = new Pair(id, value);
		elements.add(p);
		refresh();
	}
	
	public boolean newField(String id, String value) {
		if (id == null || id.equals("") ) 
			return false;
		
		//String s = RiscossUtil.sanitize(txt.getText().trim());//attention:name sanitation is not directly notified to the user
		if (!RiscossUtil.sanitize(id).equals(id)){
			//info: firefox has some problem with this window, and fires assertion errors in dev mode
			Window.alert("Name contains prohibited characters (##,@,\") \nPlease re-enter name");
			return false;
		}
		
		for(int i=0; i<elements.size(); i++){
			if (elements.get(i).getLeft().equals(id)){
				//info: firefox has some problem with this window, and fires assertion errors in dev mode
				Window.alert("Custom information ID already in use.\nPlease re-enter name");
				return false;
			}
		}
		addField(id, value);
		return true;
	}
	
	public void removeField() {
		removeElements.add(elements.get(selectedItem).getLeft());
		elements.remove(selectedItem);
		tablePanel.remove(deleteItem);
		
		refresh();
	}
	
	public void refresh() {
		table.setRowData(elements);
		
		dataProvider = new ListDataProvider<Pair<String, String>>();
		dataProvider.addDataDisplay( table );
		
		for( int i = 0; i < elements.size(); i++ ) {
			dataProvider.getList().add( elements.get(i) );
		}
	    
	    tablePanel.add(table);
	    tablePanel.add(pager);
	}
	
	public void save(String entity) {
		for (int i = 0; i < removeElements.size(); ++i) {
			JSONObject o = new JSONObject();
			o.put( "id", new JSONString( removeElements.get(i) ) );
			o.put( "target", new JSONString( entity ) );
			JSONArray array = new JSONArray();
			array.set( 0, o );
			RiscossJsonClient.postRiskData( array,  new JsonCallbackWrapper<String>( removeElements.get(i) ) {
				@Override
				public void onSuccess( Method method, JSONValue response ) {
					
				}
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}
			});
		}
		removeElements = new ArrayList<>();
		for (int i = 0; i < elements.size(); ++i) {
			JSONObject o = new JSONObject();
			o.put( "id", new JSONString( dataProvider.getList().get(i).getLeft() ) );
			o.put( "target", new JSONString( entity ) );
			o.put( "value", new JSONString( dataProvider.getList().get(i).getRight() ) );
			o.put( "datatype", new JSONString ( "CUSTOM" ));
			o.put( "type", new JSONString( "custom" ) );
			o.put( "origin", new JSONString( "user" ) );
			JSONArray array = new JSONArray();
			array.set( 0, o );
			RiscossJsonClient.postRiskData( array, new JsonCallback() {
				@Override
				public void onFailure(Method method, Throwable exception) {
					// TODO Auto-generated method stub
					
				}
				@Override
				public void onSuccess(Method method, JSONValue response) {
					// TODO Auto-generated method stub
					
				}} );
		}
	}
	
	public Widget getWidget() {
		return tablePanel;
	}
	
}
