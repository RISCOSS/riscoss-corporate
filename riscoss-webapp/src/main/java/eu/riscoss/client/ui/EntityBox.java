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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.JsonModelList;
import eu.riscoss.client.ModelInfo;
import eu.riscoss.client.RiscossJsonClient;

public class EntityBox implements IsWidget {
	
	public interface Listener {
		void entitySelected( List<String> entities );
	}
	
	class EntityDialog {
		
		DialogBox dialog = new DialogBox();
		
		ArrayList<String> selection = new ArrayList<String>();
		
		public void show() {
			RiscossJsonClient.listEntities( new JsonCallback() {
				@Override
				public void onFailure( Method method, Throwable exception ) {
					Window.alert( exception.getMessage() );
				}

				@Override
				public void onSuccess( Method method, JSONValue response ) {
					JsonModelList list = new JsonModelList( response );
					dialog = new DialogBox( true, true ); //, new HtmlCaption( "Add model" ) );
					dialog.setText( "Available entities" );
					Grid grid = new Grid();
					grid.resize( list.getModelCount(), 1 );
					for( int i = 0; i < list.getModelCount(); i++ ) {
						ModelInfo info = list.getModelInfo( i );
						CheckBox chk = new CheckBox( info.getName() );
						chk.setName( info.getName() );
						chk.addClickHandler( new ClickHandler() {
							@Override
							public void onClick(ClickEvent event) {
								CheckBox chk = (CheckBox)event.getSource();
								boolean value = chk.getValue();
								if( value == true ) {
									selection.add( chk.getName() );
								}
								else {
									selection.remove( chk.getName() );
								}
							}
						});
						grid.setWidget( i, 0, chk );
					}
					DockPanel dock = new DockPanel();
					dock.add( grid, DockPanel.CENTER );
					dock.add( new Button( "Ok", new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							
							dialog.hide();
							
							for( Listener l : listeners ) {
								l.entitySelected( selection );
							}
							
						}} ), DockPanel.SOUTH );
					dialog.add( dock );
					dialog.show();
				} } );
		}
	}
	
	class EntityLabel implements IsWidget {
		
		HorizontalPanel p = new HorizontalPanel();
		
		public EntityLabel( String str ) {
			p.setWidth( "1%" );
			Label label = new Label( str );
			Anchor a = new Anchor( "-" );
			a.setTitle( "Remove " + str );
			p.add( label );
			p.add( a );
		}
		
		@Override
		public Widget asWidget() {
			return p;
		}
		
	}
	
	FlowPanel panel = new FlowPanel();
	
	Map<String,EntityLabel> map = new HashMap<String,EntityLabel>();
	
	Anchor plus = new Anchor( " + " );
	
	List<Listener> listeners = new ArrayList<Listener>();
	
	public EntityBox() {
		panel.setWidth( "100%" );
		plus.addClickHandler( new ClickHandler() {
			@Override
			public void onClick( ClickEvent event ) {
				onPlusButtonClicked();
			}
		});
		panel.add( plus );
	}
	
	public void addListener( Listener l ) {
		this.listeners.add( l );
	}
	
	protected void onPlusButtonClicked() {
		new EntityDialog().show();
	}

	public String getSelectedEntity() {
//		if (combo.getSelectedIndex() >= 0)
//			return combo.getItemText( combo.getSelectedIndex() );
//		return null;
		return "";
	}
	
	@Override
	public Widget asWidget() {
		return panel;
	}

	public void add( String p ) {
		panel.insert( new EntityLabel( p ), panel.getWidgetCount() -1 );
	}
	
	public void setSelectedEntities( List<String> entities ) {
		for( EntityLabel lbl : map.values() ) {
			lbl.asWidget().removeFromParent();
		}
		map.clear();
		for( String e : entities ) {
			add( e );
		}
	}
	
	public Collection<String> getSelectedEntities() {
		return map.keySet();
	}
	
}
