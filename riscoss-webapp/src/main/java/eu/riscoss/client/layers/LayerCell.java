package eu.riscoss.client.layers;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import eu.riscoss.client.layers.LayersModule.LayerInfo;

public class LayerCell extends AbstractCell<LayersModule.LayerInfo> {

	@Override
	public void render(com.google.gwt.cell.client.Cell.Context context,
			LayerInfo value, SafeHtmlBuilder sb) {
		// Value can be null, so do a null check..
		if (value == null) {
			return;
		}
		
		
	}

}
