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

package eu.riscoss.client.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

class IndexedTab implements IsWidget {
	
	public interface TabHandler {
		void onTabActivated();
	}
	
	TabPanel tab = new TabPanel();
	
	Map<Integer,String> index = new HashMap<Integer,String>();
	
	Map<String,ArrayList<IndexedTab.TabHandler>> listeners = new HashMap<String,ArrayList<IndexedTab.TabHandler>>();
	
	public IndexedTab() {
		tab.addSelectionHandler( new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				onTabActivated( event );
			}
		});
	}
	
	protected void onTabActivated( SelectionEvent<Integer> event ) {
		
		String tabName = index.get( event.getSelectedItem() );
		
		if( tabName == null ) return;
		
		List<IndexedTab.TabHandler> list = listeners.get( tabName );
		
		if( list == null ) return;
		
		for( IndexedTab.TabHandler h : list ) {
			h.onTabActivated();
		}
		
	}

	public void addTab( String title, IsWidget widget ) {
		
		int n = tab.getTabBar().getTabCount();
		
		tab.add( widget, title );
		
		index.put( n, title );
		
	}
	
	@Override
	public Widget asWidget() {
		return tab;
	}
	
	public void addTabHandler( String tabName, IndexedTab.TabHandler h ) {
		ArrayList<IndexedTab.TabHandler> list = listeners.get( tabName );
		if( list == null ) {
			list = new ArrayList<IndexedTab.TabHandler>();
			listeners.put( tabName, list );
		}
		list.add( h );
	}

	public TabPanel getTabPanel() {
		return this.tab;
	}

	public void removeListeners( String string ) {
		listeners.remove( string );
	}
	
}