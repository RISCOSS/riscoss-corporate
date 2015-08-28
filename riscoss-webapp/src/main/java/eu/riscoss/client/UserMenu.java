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

package eu.riscoss.client;

import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.PopupPanel.PositionCallback;
import com.google.gwt.user.client.ui.Widget;


public class UserMenu implements IsWidget {
	
	static UserMenu	instance	= null;
	
	PopupPanel			panel		= new PopupPanel( false, false );
	
	private UserMenu() {
		panel.setStyleName( "" );
		panel.setWidth( "100px" );
		panel.setHeight( "20px" );
//		panel.getElement().getStyle().setBackgroundColor( "rgb(255,0,0)" );
		panel.setWidget( new HTML( " <span style='color:white;padding:1px;'>Loading...</span>" ) );
	}
	
	public static UserMenu get() {
		if( instance == null ) {
			instance = new UserMenu();
		}
		
		return instance;
	}
	
	public void show() {
		this.panel.setPopupPositionAndShow( new PositionCallback(){
			@Override
			public void setPosition( int offsetWidth, int offsetHeight )
			{
				
				panel.getElement().getStyle().setPosition( Position.FIXED );
				panel.getElement().getStyle().setTop( 0, Unit.PX );
				panel.getElement().getStyle().setRight( 0, Unit.PX );
				panel.getElement().getStyle().setProperty( "left", "" );
				panel.getElement().getStyle().setProperty( "bottom", "" );
			}
		} );
	}
	
	public Widget asWidget() {
		return this.panel;
	}
	
	public void hide() {
		panel.hide();
	}
	
}
