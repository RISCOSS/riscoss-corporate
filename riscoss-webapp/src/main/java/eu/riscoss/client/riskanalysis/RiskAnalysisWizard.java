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

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;

public class RiskAnalysisWizard implements EntryPoint {
	
	DockPanel dock			= new DockPanel();
	
	EntitySelectionPanel	entitySelectionPanel = new EntitySelectionPanel();
	RCSelectionPanel		rcSelectionPanel = new RCSelectionPanel();
	RASSelectionPanel		rasSelectionPanel = new RASSelectionPanel();
	RASPanel				rasPanel = new RASPanel();
	
	HorizontalPanel			topPanel = new HorizontalPanel();
//	HorizontalPanel			bottomPanel = new HorizontalPanel();
	
	Wizard					wizard = new Wizard();
	
	
	public void onModuleLoad() {
		
		try {
			
			wizard.addPanel( "Select Entity", entitySelectionPanel );
			wizard.addPanel( "Select Risk Configuration", rcSelectionPanel );
			wizard.addPanel( "Select Risk Session", rasSelectionPanel );
			wizard.addPanel( "Risk Analysis Session", rasPanel );
			
			wizard.addPanelSelectionListener( "Select Risk Configuration", new Wizard.PanelSelectionListener() {
				@Override
				public void onPanelSelected() {
					rcSelectionPanel.loadRCs( entitySelectionPanel.getSelectedEntity() );
				}
			} );
			wizard.addPanelSelectionListener( "Select Risk Session", new Wizard.PanelSelectionListener() {
				@Override
				public void onPanelSelected() {
					rasSelectionPanel.loadRASList( entitySelectionPanel.getSelectedEntity(), rcSelectionPanel.getSelectedRC() );
				}
			} );
			wizard.addPanelSelectionListener( "Risk Analysis Session", new Wizard.PanelSelectionListener() {
				@Override
				public void onPanelSelected() {
					rasPanel.loadRAS( rasSelectionPanel.getSelectedRAS() );
				}
			} );
			
		}
		catch( Exception ex ) {
			
			Window.alert( ex.getMessage() );
		}
//		Button button = new Button( "Back" );
//		button.addClickHandler( new ClickHandler() {
//			@Override
//			public void onClick( ClickEvent event ) {
//				wizard.setSelectedIndex( wizard.getSelectedIndex() -1 );
//			}
//		});
//		bottomPanel.add( button );
//		button = new Button( "Next" );
//		button.addClickHandler( new ClickHandler() {
//			@Override
//			public void onClick( ClickEvent event ) {
//				wizard.setSelectedIndex( wizard.getSelectedIndex() +1 );
//			}
//		});
//		bottomPanel.add( button );
//		bottomPanel.setCellHorizontalAlignment( button, HorizontalPanel.ALIGN_RIGHT );
		
//		dock.add( bottomPanel, DockPanel.SOUTH );
		dock.add( wizard, DockPanel.CENTER );
		
		entitySelectionPanel.loadEntities();
		
		RootPanel.get().add( dock );
		
		wizard.setSelectedIndex( 0 );
	}
	
}
