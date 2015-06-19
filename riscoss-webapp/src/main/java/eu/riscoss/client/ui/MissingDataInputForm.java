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

package eu.riscoss.client.ui;

import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;

import eu.riscoss.client.Callback;
import eu.riscoss.client.JsonInputChunk;

public class MissingDataInputForm {
	
	Callback<JSONObject> cb;
	
	DialogBox dialog = null;
	Map<String,ManualInputField> textboxes = new HashMap<String,ManualInputField>();
	
	public void show( Callback<JSONObject> cb ) {
		this.cb = cb;
		dialog.show();
	}
	
	public void load( JSONObject missingData ) {
		
		JSONArray array = missingData.get( "list" ).isArray();
		
		Grid grid = new Grid( array.size() +1, 2 );
		
		for( int i = 0; i < array.size(); i++ ) {
			JSONObject o = array.get( i ).isObject();
			String name = o.get( "question" ).isString().stringValue();
			grid.setWidget( i, 0, new Label( name ) );
			ManualInputField inputField = new ManualInputField( new JsonInputChunk( o ) );
			textboxes.put( o.get( "id" ).isString().stringValue(), inputField );
			grid.setWidget( i, 1, inputField );
		}
		
		grid.setWidget( array.size(), 1, new Button( "Continue", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				gatherUserEnteredDataAndRunAgain();
			}
		} ) );
		
		dialog = new DialogBox( false, false );
		dialog.setText( "Enter missing data" );
		
		dialog.setWidget( grid );
	}
	
	protected void gatherUserEnteredDataAndRunAgain() {
		
		JSONObject o = new JSONObject();
		
		for( String key : textboxes.keySet() ) {
			ManualInputField txt = textboxes.get( key );
			JSONObject val = new JSONObject();
			val.put( "id", new JSONString( key ) );
			val.put( "value", new JSONString( txt.getValue().trim() ) );
			o.put( key, val );
		}
		
		dialog.hide();
		
		if( cb != null ) {
			cb.onDone( o );
		}
		
	}

}