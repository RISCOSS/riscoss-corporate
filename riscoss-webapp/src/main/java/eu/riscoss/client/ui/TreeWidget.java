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

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TreeWidget implements IsWidget {
	
	VerticalPanel	panel = new VerticalPanel();
	HorizontalPanel	h = new HorizontalPanel();
	VerticalPanel	childrenPanel = new VerticalPanel();
	
	Map<String,TreeWidget> index = null;
	
	public TreeWidget( Widget w ) {
		this( w, 16 );
	}
	
	public TreeWidget( Widget w, int indentPx ) {
		panel.add( w );
		h.setVisible( false );
		panel.add( h );
		HTML html = new HTML( "<div style='float:left;margin:0;padding:0;width:" + indentPx + "px;height:100%'>&nbsp;</div>" );
		h.add( html );
		h.add( childrenPanel );
		panel.setWidth( "100%" );
		h.setWidth( "100%" );
		h.setCellWidth( html, indentPx + "px" );
		childrenPanel.setWidth( "100%" );
	}
	
	public TreeWidget() {
		panel.add( h );
		h.add( childrenPanel );
		panel.setWidth( "100%" );
		h.setWidth( "100%" );
		childrenPanel.setWidth( "100%" );
	}
	
	@Override
	public Widget asWidget() {
		return panel;
	}
	
	private void expand() {
		h.setVisible( true );
	}
	
	private void collapse() {
		h.setVisible( false );
	}
	
	public TreeWidget addChild( TreeWidget child ) {
		childrenPanel.add( child.asWidget() );
		expand();
		return child;
	}
	
	private Map<String,TreeWidget> getIndex() {
		if( index == null ) {
			index = new HashMap<String,TreeWidget>();
		}
		return index;
	}
	
	public TreeWidget addChild( String id, TreeWidget child ) {
		getIndex().put( id, child );
		return addChild( child );
	}
	
	public TreeWidget getChild( String id ) {
		return getIndex().get( id );
	}

	public void clear() {
		for( TreeWidget child : getIndex().values() ) {
			child.clear();
		}
		index.clear();
		while( childrenPanel.getWidgetCount() > 0 ) {
			childrenPanel.remove( 0 );
		}
		collapse();
	}
	
}
