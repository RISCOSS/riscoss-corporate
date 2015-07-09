package eu.riscoss.client.riskanalysis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.shared.JDataItem;
import eu.riscoss.shared.JMissingData;
import eu.riscoss.shared.JValueMap;

public class MultiLayerInputMatrix implements MultiLayerInputForm {
	
	static class MultiLayerInputMatrixCell {
		
		TextBox	textBox;
		String	id;
		String	entity;
		
	}
	
	static class MultiLayerInputMatrixGrid {
		
		Map<String,Integer> rows = new HashMap<String,Integer>();
		Map<String,Integer> cols = new HashMap<String,Integer>();
		Map<String,MultiLayerInputMatrixCell> cells = new HashMap<>();
		
		Grid grid = new Grid( 1, 1 );
		
		public MultiLayerInputMatrixGrid() {
			rows.put( "", 0 );
			cols.put( "", 0 );
		}
		
		public int addColumn( String name ) {
			int col = cols.size();
			cols.put( name, col );
			grid.resize( grid.getRowCount(), grid.getColumnCount() +1 );
			grid.setWidget( 0, col, new Label( name ) );
			return col;
		}
		
		public int addRow( String name ) {
			int row = rows.size();
			rows.put( name, row );
			grid.resize( grid.getRowCount() +1, grid.getColumnCount() );
			grid.setWidget( row, 0, new Label( name ) );
			return row;
		}
		
		public Collection<MultiLayerInputMatrixCell> cells() {
			return cells.values();
		}
		
		public int getRow( String id ) {
			Integer row = rows.get( id );
			if( row == null ) return -1;
			return row;
		}
		
		public MultiLayerInputMatrixCell getCell( int row, int col ) {
			MultiLayerInputMatrixCell cell = cells.get( row + ":" + col );
			if( cell == null ) {
				cell = new MultiLayerInputMatrixCell();
				cells.put( row + ":" + col, cell );
			}
			return cell;
		}
		
	}
	
	
	Map<String,MultiLayerInputMatrixGrid> tabs = new HashMap<>();
	
	TabPanel tab = new TabPanel();
	
	public MultiLayerInputMatrix() {
		
	}
	
	@Override
	public Widget asWidget() {
		return this.tab;
	}
	
	@Override
	public void load( JMissingData md ) {
		
		fill( md );
		
		if( tab.getTabBar().getTabCount() > 0 ); {
			tab.selectTab( 0 );
		}
		
	}
	
	private void fill( JMissingData md ) {
		
		MultiLayerInputMatrixGrid matrix = tabs.get( md.getLayer() );
		if( matrix == null ) {
			matrix = new MultiLayerInputMatrixGrid();
			tabs.put( md.getLayer(), matrix );
			tab.add( matrix.grid, md.getLayer() );
		}
		fill( matrix, md );
		
	}
	
	private void fill( MultiLayerInputMatrixGrid matrix, JMissingData md ) {
		
		int col = matrix.addColumn( md.getEntity() );
		
		for( JDataItem item : md.items() ) {
			
			int row = matrix.getRow( item.getId() );
			
			if( row == -1 ) {
				row = matrix.addRow( item.getId() );
			}
			
			MultiLayerInputMatrixCell cell = matrix.getCell( row, col );
			
			cell.id = item.getId();
			cell.entity = md.getEntity();
			
			TextBox txt = new TextBox();
			txt.setText( item.getValue() );
			cell.textBox = txt;
			
			txt.setWidth( "32px" );
			matrix.grid.setWidget( row, col, txt );
			
		}
		
		for( JMissingData child : md.children() ) {
			fill( child );
		}
		
	}
	
	@Override
	public JValueMap getValueMap() {
		
		JValueMap m = new JValueMap();
		
		for( MultiLayerInputMatrixGrid matrix : tabs.values() ) {
			for( MultiLayerInputMatrixCell cell : matrix.cells() ) {
				String text = cell.textBox.getText().trim();
				if( "".equals( text ) ) continue;
				m.add( cell.entity, cell.id, text );
			}
		}
		
		return m;
	}
	
}
