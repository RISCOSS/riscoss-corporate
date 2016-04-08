package eu.riscoss.client.riskanalysis;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RootPanel;

import eu.riscoss.client.RiscossJsonClient;

public class RASReport implements EntryPoint {

	HTMLPanel html;
	
	@Override
	public void onModuleLoad() {
		String selectedRAS = Window.Location.getParameter("id");
		
		RiscossJsonClient.generateHTMLReport(selectedRAS, new JsonCallback() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				exception.printStackTrace();
			}
			@Override
			public void onSuccess(Method method, JSONValue response) {
				String htmlString = response.isObject().get("hml").isString().stringValue();
		           
		        html = new HTMLPanel(htmlString);
		        
		        RootPanel.get().add(html);
			}
		});
	}

}
