package eu.riscoss.client.layers;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.RiscossJsonClient;

public class LayersComboBox implements IsWidget {
	
	ListBox combo = new ListBox();
	
	void loadLayers() {
		RiscossJsonClient.listLayers( new JsonCallback() {
			@Override
			public void onSuccess(Method method, JSONValue response) {
				for( int i = 0; i < response.isArray().size(); i++ ) {
					JSONObject o = (JSONObject)response.isArray().get( i );
					combo.addItem( o.get( "name" ).isString().stringValue() );
				}
			}
			@Override
			public void onFailure(Method method, Throwable exception) {
				Window.alert( exception.getMessage() );
			}
		});
	}
	
	public LayersComboBox preloaded() {
		loadLayers();
		return this;
	}
	
	@Override
	public Widget asWidget() {
		return combo;
	}

	public String getSelectedLayer() {
		return combo.getItemText( combo.getSelectedIndex() );
	}
	
}
