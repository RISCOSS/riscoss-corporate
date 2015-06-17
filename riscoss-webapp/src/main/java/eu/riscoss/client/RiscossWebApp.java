package eu.riscoss.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.DockPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.riscoss.client.ui.FramePanel;
import eu.riscoss.client.ui.TreeWidget;

public class RiscossWebApp implements EntryPoint {
	
	DockPanel dock;
	
	FramePanel currentPanel = null;
	
	public void onModuleLoad() {
		
		TreeWidget root = new TreeWidget();
		
		TreeWidget item = root.addChild( new TreeWidget( new Label( "Configure" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Layers", "layers.html" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Entities", "entities.html" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Models", "models.html" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Risk Configurations", "riskconfs.html" ) ) );
		
		item = root.addChild( new TreeWidget( new Label( "Run" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "One-layer Analysis", "analysis.html" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Risk Analysis Session", "riskanalysis.html" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "What-If Analysis", "whatifanalysis.html" ) ) );
		
		item = root.addChild( new TreeWidget( new Label( "Browse" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Risk Data Repository", "rdr.html" ) ) );
		item.addChild( new TreeWidget( new OutlineLabel( "Data Collectors", "rdcs.html" ) ) );
		
		VerticalPanel left = new VerticalPanel();
		left.add( new Image( "logo3.png" ) );
		left.add( root );
		
		dock = new DockPanel();
		dock.setWidth( "100%" );
		dock.add( left, DockPanel.WEST );
		dock.setCellWidth( left, "222px" );
		dock.setHeight( "90%" ); // <- not 100% to allow the "recompile" icon of iframes to appear
		
		RootPanel.get().add( dock );
		
	}
	
	class OutlineLabel extends Anchor {
		String panelUrl;
		public OutlineLabel( String label, String panelName ) {
			super( label );
			this.panelUrl = panelName;
			addClickHandler( new ClickHandler() {
				public void onClick(ClickEvent event) {
					loadPanel( OutlineLabel.this.panelUrl );
				}
			});
		}
	}
	
	protected void loadPanel( String url ) {
		
		if( currentPanel != null ) {
			dock.remove( currentPanel.getWidget() );
			currentPanel = null;
		}
		
		currentPanel = new FramePanel( url );
		
		if( currentPanel != null ) {
			dock.add( currentPanel.getWidget(), DockPanel.CENTER );
//			currentPanel.getWidget().getParent().setHeight( "100%" );
			currentPanel.activate();
		}
	}

}
