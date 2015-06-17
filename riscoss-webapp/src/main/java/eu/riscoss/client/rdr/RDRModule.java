package eu.riscoss.client.rdr;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellList;
import com.google.gwt.user.cellview.client.HasKeyboardPagingPolicy.KeyboardPagingPolicy;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import eu.riscoss.client.EntityInfo;

class ContactCell extends AbstractCell<EntityInfo> {

	public ContactCell(ImageResource image) {
	}

	@Override
	public void render(Context context, EntityInfo value, SafeHtmlBuilder sb) {
		// Value can be null, so do a null check..
		if (value == null) {
			return;
		}

//		sb.appendHtmlConstant("<table><tr><td style='font-size:95%;'>");
		sb.appendHtmlConstant("<a class='gwt-Anchor' href='javascript:'>");
		sb.appendEscaped( value.getName() );
		sb.appendHtmlConstant("</a>");
//		sb.appendHtmlConstant("</td></tr></table>");
	}
}

public class RDRModule implements EntryPoint {

	DockPanel						dock = new DockPanel();

	ListDataProvider<EntityInfo>	dataProvider;

	CellList<EntityInfo>			cellList;

	EntityDataBox					ppg = null;

	public RDRModule() {
	}

	public void onModuleLoad() {

		String layer = Window.Location.getParameter( "layer" );

		ContactCell contactCell = new ContactCell(null); //images.contact());
		cellList = new CellList<EntityInfo>(contactCell);
		cellList.setPageSize(30);
		cellList.setKeyboardPagingPolicy(KeyboardPagingPolicy.INCREASE_RANGE);
		cellList.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.BOUND_TO_SELECTION);

		final SingleSelectionModel<EntityInfo> selectionModel = 
				new SingleSelectionModel<EntityInfo>();
		cellList.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {
			public void onSelectionChange(SelectionChangeEvent event) {
				ppg.setSelectedEntity( selectionModel.getSelectedObject().getName() );
			}
		});

		dataProvider = new ListDataProvider<EntityInfo>();
		dataProvider.addDataDisplay(cellList);
		ppg = new EntityDataBox();
		
		dock.setWidth( "100%" );
		dock.add(cellList,DockPanel.CENTER);
		dock.add( ppg.asWidget(), DockPanel.EAST );
		dock.setCellWidth( ppg.asWidget(), "60%" );

		RootPanel.get().add( dock );


		String url = ( layer != null ?
				"api/entities/list/" + layer :
				"api/entities/list" );

		Resource resource = new Resource( GWT.getHostPageBaseURL() + url );

		resource.get().send( new JsonCallback() {

			public void onSuccess(Method method, JSONValue response) {
				if( response.isArray() != null ) {
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						insertEntityIntoTable(
								o.get( "name" ).isString().stringValue() );
					}
				}
			}

			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	protected void insertEntityIntoTable( String name ) {
		dataProvider.getList().add( new EntityInfo( name ) );
	}
}
