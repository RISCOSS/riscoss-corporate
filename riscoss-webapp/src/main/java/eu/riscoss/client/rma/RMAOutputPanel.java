package eu.riscoss.client.rma;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.shared.JAHPResult;

public class RMAOutputPanel implements IsWidget {
	
	VerticalPanel vp = new VerticalPanel();
	
	@Override
	public Widget asWidget() {
		return vp;
	}
	
	public void setOutput( JAHPResult result ) {
		
		vp.clear();
		
		for( String item : result.values.keySet() ) {
			vp.add( new Label( item + ": " + result.values.get( item ) ) );
		}
	}
	
}
