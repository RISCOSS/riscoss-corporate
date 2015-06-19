/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/**
 * @author 	Alberto Siena
**/

package eu.riscoss.client.ui;

import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import eu.riscoss.client.JsonInputChunk;
import eu.riscoss.shared.ChunkDataType;

public class ManualInputField implements IsWidget {
	
	interface FieldMask extends IsWidget {
		String getValue();
	}
	
	class NumberMask implements IsWidget, FieldMask {
		
		TextBox textBox = new TextBox();
		
		@Override
		public Widget asWidget() {
			return textBox;
		}
		
		public String getValue() {
			return textBox.getText().trim();
		}
		
	}
	
	class EvidenceMask implements IsWidget, FieldMask {
		
		HorizontalPanel panel = new HorizontalPanel();
		
		TextBox txtPositive = new TextBox();
		TextBox txtNegative = new TextBox();
		
		public EvidenceMask() {
			panel.add( txtPositive );
			panel.add( txtNegative );
		}
		
		@Override
		public Widget asWidget() {
			return panel;
		}
		
		public String getValue() {
			double p = 0;
			double m = 0;
			try {
				p = Double.parseDouble( txtPositive.getText() );
			} catch( Exception ex ) {}
			try {
				m = Double.parseDouble( txtNegative.getText() );
			} catch( Exception ex ) {}
			return p + ";" + m;
		}
		
	}
	
	class DistributionMask implements IsWidget, FieldMask {
		
		HorizontalPanel panel = new HorizontalPanel();
		
		TextBox[] textboxes = new TextBox[] {};
		
		public DistributionMask() {
			
			String[] values = chunk.getDistributionValues();
			
			if( values != null ) {
				textboxes = new TextBox[ values.length ];
				for( int i = 0; i < values.length; i++ ) {
					TextBox txt = new TextBox();
					txt.setText( values[i] );
					panel.add( txt );
					textboxes[i] = txt;
				}
			}
			
		}
		
		@Override
		public Widget asWidget() {
			return panel;
		}
		
		public String getValue() {
			String str = "";
			String sep = "";
			for( int i = 0; i < textboxes.length; i++ ) {
				TextBox txt = textboxes[i];
				double val = 0;
				try {
					val = Double.parseDouble( txt.getText() );
				}
				catch( Exception ex ) {}
				str += sep + val;
				sep = ";";
			}
			return str;
		}
		
	}
	
	private JsonInputChunk chunk;
	
	SimplePanel panel = new SimplePanel();
	
	FieldMask mask;

	public ManualInputField( JsonInputChunk c ) {
		
		this.chunk = c;
		
//		Window.alert( "" + c.getType() );
		
		switch( c.getType() ) {
		case DISTRIBUTION:
			mask = new DistributionMask();
			break;
		case EVIDENCE:
			mask = new EvidenceMask();
			break;
		case NaN:
		default:
			mask = new NumberMask();
			break;
		}
		panel.setWidget( mask );
		
	}

	@Override
	public Widget asWidget() {
		return panel;
	}
	
	public ChunkDataType getType() {
		return chunk.getType();
	}

	public String getValue() {
		return mask.getValue();
	}
	
}
