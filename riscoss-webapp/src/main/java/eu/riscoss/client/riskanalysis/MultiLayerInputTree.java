package eu.riscoss.client.riskanalysis;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.ui.TreeWidget;
import eu.riscoss.shared.JDataItem;
import eu.riscoss.shared.JMissingData;
import eu.riscoss.shared.JValueMap;

public class MultiLayerInputTree implements MultiLayerInputForm {
	
	TreeWidget tree;
	
	@Override
	public void load( JMissingData md ) {
		
		tree = mkTree( md );
		
	}
	
	private TreeWidget mkTree( JMissingData md ) {
		
		KeyValueGrid grid = new KeyValueGrid();
		grid.add( "", new Label( md.getEntity() ) );
		
		for( JDataItem item : md.items() ) {
			grid.add( item.getLabel(), new TextBox() );
		}
		
		TreeWidget node = new TreeWidget( grid );
		
		for( JMissingData child : md.children() ) {
			
			node.addChild( mkTree( child ) );
			
		}
		
		return node;
		
	}

	@Override
	public Widget asWidget() {
		return tree.asWidget();
	}

	@Override
	public JValueMap getValueMap() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
