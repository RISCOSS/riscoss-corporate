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

import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.CallbackWrapper;
import eu.riscoss.client.RiscossJsonClient;
import eu.riscoss.client.SimpleRiskCconf;
import eu.riscoss.client.ui.ClickWrapper;
import eu.riscoss.client.ui.TreeWidget;

public class ModelList implements IsWidget {
	
	public interface Listener {
		void onModelsSelected( String layer, List<String> models );
	}
	
	TreeWidget root = new TreeWidget();
	Listener listener;
	SimpleRiskCconf rc;
	String layer;
	
	public ModelList( Listener l ) {
		this.listener = l;
	}
	
	public void init( List<String> layers ) {
		
		root.clear();
		
		RiscossJsonClient.listModels(new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {

			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				// TODO Auto-generated method stub
			}
		});
		
		for( int i = 0; i < layers.size(); i++ ) {
			//		RCModelPack.AnalysisPhase packLayer = pack.getLayer( i );
			HorizontalPanel hp = new HorizontalPanel();
			Anchor anchor = new Anchor( layers.get( i ) );
			hp.add( anchor );
			layer = layers.get(i);
			anchor.addClickHandler( new ClickWrapper<String>( layers.get( i ) ) {
				String s = layer;
				@Override
				public void onClick( ClickEvent event ) {
					ModelSelectionDialog dialog = new ModelSelectionDialog();
					dialog.show(s, rc, new CallbackWrapper<List<String>,String>( getValue() ) {
						@Override
						public void onDone( List<String> pack ) {
							setModels( getValue(), pack );
							if( listener != null ) {
								listener.onModelsSelected( getValue(), pack );
							}
						}
						@Override
						public void onError( Throwable t ) {
							Window.alert( t.getMessage() );
						}} );
				}
			});
			TreeWidget w = new TreeWidget( hp );
			root.addChild( layers.get( i ), w );
		}
	}
	
	public void setModels( String layer, List<String> models ) {
		if( models == null ) return;
		TreeWidget w = root.getChild( layer );
		if( w == null ) return;
		w.clear();
		for( int m = 0; m < models.size(); m++ ) {
			w.addChild( models.get( m ), 
					new TreeWidget( new Label( models.get( m ) ) ) );
		}
	}

	@Override
	public Widget asWidget() {
		return root.asWidget();
	}
	
	public void setRC(SimpleRiskCconf rconf) {
		this.rc = rconf;
	}
	
}
