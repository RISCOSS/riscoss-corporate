package eu.riscoss.client.riskanalysis;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.cell.client.SafeHtmlCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

import eu.riscoss.client.ModelInfo;
import eu.riscoss.client.ui.LinkHtml;

class RCSelectionPanel implements IsWidget {
	
	CellTable<ModelInfo>		modelTable;
	ListDataProvider<ModelInfo>	modelDataProvider;
	String						selectedRC;
	
	public RCSelectionPanel() {
		
		exportJS();
		
		modelTable = new CellTable<ModelInfo>();
		
		modelTable.addColumn( new Column<ModelInfo,SafeHtml>(new SafeHtmlCell() ) {
			@Override
			public SafeHtml getValue(ModelInfo object) {
				return new LinkHtml( object.getName(), "javascript:setSelectedRC(\"" + object.getName() + "\")" ); };
		}, "Available Risk Configurations");
		
		modelDataProvider = new ListDataProvider<ModelInfo>();
		modelDataProvider.addDataDisplay(modelTable);
		
	}
	
	public native void exportJS() /*-{
	var that = this;
	$wnd.setSelectedRC = $entry(function(amt) {
		that.@eu.riscoss.client.riskanalysis.RCSelectionPanel::setSelectedRC(Ljava/lang/String;)(amt);
	});
	}-*/;
	
	public void setSelectedRC( String rc ) {
		this.selectedRC = rc;
	}
	
	void loadRCs( String entity ) {
		
		modelDataProvider.getList().clear();
		
		Resource resource = new Resource( GWT.getHostPageBaseURL() + "api/rcs/list");
		
		resource.addQueryParam( "entity", entity ).get().send( new JsonCallback() {
			
			public void onSuccess(Method method, JSONValue response) {
				if( response.isArray() != null ) {
					for( int i = 0; i < response.isArray().size(); i++ ) {
						JSONObject o = (JSONObject)response.isArray().get( i );
						modelDataProvider.getList().add( new ModelInfo( 
								o.get( "name" ).isString().stringValue() ) );
					}
				}
			}
			
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
	}

	@Override
	public Widget asWidget() {
		return modelTable;
	}

	public String getSelectedRC() {
		return this.selectedRC;
	}
}