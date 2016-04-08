package eu.riscoss.client.riskanalysis;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import eu.riscoss.client.Log;
import eu.riscoss.client.RiscossJsonClient;

public class RASReport implements EntryPoint {

	VerticalPanel vPanel;
	
	@Override
	public void onModuleLoad() {
		Log.println("Load module");
		String selectedRAS = Window.Location.getParameter("id");
		Log.println("Get parameter id");
		vPanel = new VerticalPanel();
		vPanel.setWidth("100%");
		
		RiscossJsonClient.generateHTMLReport(selectedRAS, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				exception.printStackTrace();
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				String htmlString = response.isObject().get("hml").isString().stringValue();
				Log.println("Generated html");
		           
		        HTMLPanel htmlPanel = new HTMLPanel(htmlString);
		        
		        vPanel.add(htmlPanel);
		        Log.println("Integrated");
			}
		});
		
		RootPanel.get().add(vPanel);
	}

}
