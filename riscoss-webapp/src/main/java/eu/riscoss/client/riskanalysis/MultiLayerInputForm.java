package eu.riscoss.client.riskanalysis;

import com.google.gwt.user.client.ui.IsWidget;

import eu.riscoss.shared.JMissingData;
import eu.riscoss.shared.JValueMap;

public interface MultiLayerInputForm extends IsWidget {
	
	public void load( JMissingData md );

	public JValueMap getValueMap();
	
}
