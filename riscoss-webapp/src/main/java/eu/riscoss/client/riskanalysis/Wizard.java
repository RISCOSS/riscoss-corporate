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

package eu.riscoss.client.riskanalysis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

class Wizard implements IsWidget {
	
	public interface PanelSelectionListener {
		void onPanelSelected();
	}
	
	static class Pair {
		String title = "";
		IsWidget widget;
		
		public Pair( String title, IsWidget w ) {
			this.title = title;
			this.widget = w;
		}
	}
	
	DockPanel dock = new DockPanel();
	SimplePanel panel = new SimplePanel();
	HorizontalPanel			bottomPanel = new HorizontalPanel();
	Button btnNext;
	Button btnPrev;
	
	Map<String,ArrayList<Wizard.PanelSelectionListener>> listeners = new HashMap<String,ArrayList<Wizard.PanelSelectionListener>>();
	
	ArrayList<Wizard.Pair> panels = new ArrayList<Wizard.Pair>();
	int currentIndex = -1;
	
	public Wizard() {
		btnPrev = new Button( "Back" );
		btnPrev.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( ClickEvent event ) {
				setSelectedIndex( getSelectedIndex() -1 );
			}
		});
		bottomPanel.add( btnPrev );
		btnNext = new Button( "Next" );
		btnNext.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( ClickEvent event ) {
				setSelectedIndex( getSelectedIndex() +1 );
			}
		});
		bottomPanel.add( btnNext );
		bottomPanel.setCellHorizontalAlignment( btnNext, HorizontalPanel.ALIGN_RIGHT );
		
		dock.add( bottomPanel, DockPanel.SOUTH );
		dock.add( panel, DockPanel.CENTER );
	}
	
	public void addPanel( String title, IsWidget w ) {
		panels.add( new Pair( title, w ) );
	}
	
	public void setSelectedIndex( int i ) {
		if( panel.getWidget() != null ) {
			panel.getWidget().removeFromParent();
		}
		this.currentIndex = -1;
		if( i >= panels.size() ) return;
		if( i < 0 ) return;
		
		if( i >= panels.size() -1 ) {
			btnNext.setVisible( false );
		}
		else {
			btnNext.setVisible( true );
		}
		
		if( i <= 0 ) {
			btnPrev.setVisible( false );
		}
		else {
			btnPrev.setVisible( true );
		}
		
		Wizard.Pair w = panels.get( i );
		if( w != null ) {
			panel.setWidget( w.widget );
			ArrayList<Wizard.PanelSelectionListener> list = listeners.get( w.title );
			if( list != null ) {
				for( Wizard.PanelSelectionListener l : list ) {
					l.onPanelSelected();
				}
			}
		}
		this.currentIndex = i;
	}
	
	public int getSelectedIndex() {
		return this.currentIndex;
	}
	
	@Override
	public Widget asWidget() {
		return this.dock;
	}
	
	public void addPanelSelectionListener( String title, Wizard.PanelSelectionListener listener ) {
		ArrayList<Wizard.PanelSelectionListener> list = listeners.get( title );
		if( list == null ) {
			list = new ArrayList<Wizard.PanelSelectionListener>();
			listeners.put( title, list );
		}
		list.add( listener );
	}
	
}