package eu.riscoss.client.ras;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ComparisonPanel {

	VerticalPanel 		page;
	
	public ComparisonPanel() {
		
		page = new VerticalPanel();
		page.setWidth("100%");
		
		Label title = new Label("Risk analysis session comparisons");
		title.setStyleName("title");
		page.add(title);
		
	}
	
}
