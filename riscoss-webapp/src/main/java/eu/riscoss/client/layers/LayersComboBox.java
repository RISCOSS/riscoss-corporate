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

package eu.riscoss.client.layers;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.Log;
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

	/**
	 * 
	 * @return null if none selected
	 */
	public String getSelectedLayer() {
		if (combo.getSelectedIndex() >= 0)
			return combo.getItemText( combo.getSelectedIndex() );
		return null;
	}

	public void setSelectedLayer( String layer ) {
		if( layer == null ) return;
		for( int i = 0; i < combo.getItemCount(); i++ ) {
			if( layer.equals( combo.getItemText( i ) ) ) {
				combo.setSelectedIndex( i );
			}
		}
	}
	
}
