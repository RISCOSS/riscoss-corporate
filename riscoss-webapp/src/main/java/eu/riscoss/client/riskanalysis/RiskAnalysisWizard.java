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
