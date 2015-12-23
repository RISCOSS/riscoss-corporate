package eu.riscoss.client.roles;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.riscoss.shared.KnownRoles;

public class RolesModule implements EntryPoint {

	VerticalPanel		page = new VerticalPanel();
	HorizontalPanel		mainView = new HorizontalPanel();
	VerticalPanel		leftPanel = new VerticalPanel();
	//VerticalPanel		rightPanel = new VerticalPanel();
	
	Grid 				g;
	
	@Override
	public void onModuleLoad() {
		
		mainView.setStyleName("mainViewLayer");
		mainView.setWidth("100%");
		leftPanel.setStyleName("leftPanelLayer");
		leftPanel.setWidth("100%");
		page.setWidth("100%");
		
		Label title = new Label("Roles permissions");
		title.setStyleName("title");
		page.add(title);
		
		mainView.add(leftPanel);
		page.add(mainView);
		
		generateTable();
		
		RootPanel.get().add( page );
		
	}
	
	String pages[] = new String[]{
			"Layers Management",
			"Entities Management",
			"Models Management",
			"Risk configurations Management",
			"Multi-layer Analysis",
			"What-if Analysis",
			"AHP Sessions Analysis",
			"Risk Data Repository",
			"Risk Analysis Sessions",
			"Admin Management"
	};
	
	public void generateTable() {
		g = new Grid(5, 11);
		//g.setWidth("100%");
		
		//Roles (rows)
		for (int i = 1; i < 5; ++i) {
			Label l = new Label(KnownRoles.values()[i-1].name());
			l.setStyleName("bold");
			g.setWidget(i, 0, l);
		}
		
		//Pages (columns)
		for (int i = 0; i < 10; ++i) {
			Label l = new Label(pages[i]);
			l.setStyleName("bold");
			g.setWidget(0, i+1, l);
		}
		
		//Guest
		for (int i = 5; i < 10; ++i) {
			Image tick = new Image( "resources/good_or_tick.png" );
			g.setWidget(1, i, tick);
		}
		
//		//Consumer
//		for (int i = 5; i < 10; ++i) {
//			Image tick = new Image( "resources/good_or_tick.png" );
//			g.setWidget(2, i, tick);
//		}
		
		//Producer
		{
			Image tick = new Image( "resources/good_or_tick.png" );
			g.setWidget(2, 2, tick);
		}
		for (int i = 5; i < 10; ++i) {
			Image tick = new Image( "resources/good_or_tick.png" );
			g.setWidget(2, i, tick);
		}
		
		//Modeler
		for (int i = 2; i < 10; ++i) {
			Image tick = new Image( "resources/good_or_tick.png" );
			g.setWidget(3, i, tick);
		}
		
		//Administrator
		for (int i = 1; i < 11; ++i) {
			Image tick = new Image( "resources/good_or_tick.png" );
			g.setWidget(4, i, tick);
		}
		
		/*for (int i = 0; i < 11; ++i) {
			g.getWidget(0, i).setStyleName("header");
		}*/
		g.setCellPadding(5);
		g.setCellSpacing(0);
//		g.setBorderWidth(1);
		
		for (int i = 0; i < 5; ++i) {
			for (int j = 0; j < 11; ++j) {
				g.getCellFormatter().setHorizontalAlignment(i, j, HasHorizontalAlignment.ALIGN_CENTER);
				g.getCellFormatter().setStyleName(i, j, "cellGrid");
			}
		}
		
		g.getColumnFormatter().setStyleName(0, "headerGrid");
		g.getRowFormatter().setStyleName(0, "headerGrid");
		
		leftPanel.add(g);
		
	}

}
