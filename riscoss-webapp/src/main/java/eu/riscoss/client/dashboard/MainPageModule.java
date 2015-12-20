package eu.riscoss.client.dashboard;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class MainPageModule implements EntryPoint {

	
	VerticalPanel page = new VerticalPanel();
	HorizontalPanel v = new HorizontalPanel();
	Image logo;
	
	@Override
	public void onModuleLoad() {
		page.setWidth("100%");
		page.setStyleName("mainViewLayer");
		
		v = new HorizontalPanel();
		v.setWidth("100%");
		v.setStyleName("mainImage");
		logo = new Image( "resources/main_view.jpg" );
		logo.setStyleName("mainImageLogo");
		v.add(logo);
		
		page.add(v);
		
		RootPanel.get().add(page);
		
		Scheduler.get().scheduleDeferred(new ScheduledCommand() {
	    	  @Override
	    	  public void execute() {
	    		  int totalWidth = v.getOffsetWidth();
	    		  int imageWidth = logo.getOffsetWidth();
	    		  int height = logo.getOffsetHeight();
	    		  
	    		  int spacers = (totalWidth - imageWidth)/2;
	    		  
	    		  HorizontalPanel h1 = new HorizontalPanel();
	    		  HorizontalPanel h2 = new HorizontalPanel();
	    		  
	    		  h1.setStyleName("mainImageLogo");
	    		  h2.setStyleName("mainImageLogo");
	    		  
	    		  h1.setWidth(spacers + "px");
	    		  h1.setHeight(height + "px");
	    		  h2.setHeight(height + "px");
	    		  h2.setWidth(spacers + "px");
	    		  
	    		  v.clear();
	    		  v.add(h1);
	    		  v.add(logo);
	    		  v.add(h2);
	    	  }
	    });
	}

}
