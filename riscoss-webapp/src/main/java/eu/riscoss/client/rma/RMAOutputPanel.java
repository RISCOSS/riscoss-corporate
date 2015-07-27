package eu.riscoss.client.rma;

import java.util.List;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class RMAOutputPanel implements IsWidget {
	
	VerticalPanel vp = new VerticalPanel();
	
	@Override
	public Widget asWidget() {
		return vp;
	}
	
	public void setOutput( List<String> list ) {
		
		vp.clear();
		
		for( String item : list ) {
			vp.add( new Label( item ) );
		}
	}
	
}
