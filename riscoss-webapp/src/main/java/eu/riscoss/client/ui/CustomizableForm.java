package eu.riscoss.client.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.ui.DeckPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class CustomizableForm implements IsWidget {
	
	interface FieldEditor extends IsWidget {
		
		String getValue();
		
	}
	
	class TextEditor implements FieldEditor, KeyUpHandler {
		
		CustomField field;
		
		TextBox textbox = new TextBox();
		
		public TextEditor( CustomField f, String strValue ) {
			this.field = f;
			textbox.setText( strValue );
			textbox.addKeyUpHandler( this );
		}
		
		@Override
		public Widget asWidget() {
			return textbox;
		}
		
		@Override
		public String getValue() {
			return textbox.getText();
		}
		
		@Override
		public void onKeyUp( KeyUpEvent event ) {
			if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
				textbox.getElement().getStyle().clearBackgroundColor();
				fireValueChanged();
			}
			else {
				textbox.getElement().getStyle().setBackgroundColor( "red" );
			}
		}
		
		private void fireValueChanged() {
			field.fireValueChanged();
		}
		
	}
	
	public interface FieldListener {
		void labelChanged( CustomField field );
		
		void valueChanged( CustomField customField );
	}
	
	public class CustomField implements IsWidget {
		
		class FieldLabeler implements IsWidget, KeyUpHandler, BlurHandler {
			
			DeckPanel	panel = new DeckPanel();
			Label		label = new Label("");
			TextBox		labelEditor;
			
			boolean		editable = false;
			
			public FieldLabeler() {
				this( "" );
			}
			
			public FieldLabeler(String strLabel) {
				if( strLabel == null ) strLabel = "";
				label.setText( strLabel );
				panel.add( label );
				if( "".equals( strLabel ) ) {
					labelEditor = new TextBox();
					labelEditor.addKeyUpHandler( this );
					labelEditor.addBlurHandler( this );
					panel.add( labelEditor );
					panel.showWidget( 1 );
					editable = true;
				}
				else {
					panel.showWidget( 0 );
				}
			}
			
			@Override
			public Widget asWidget() {
				return panel;
			}
			
			public String getName() {
				return label.getText();
			}
			
			@Override
			public void onKeyUp( KeyUpEvent event ) {
				if( !editable ) return;
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					labelSet();
				}
			}
			
			@Override
			public void onBlur( BlurEvent event ) {
				labelSet();
			}
			
			protected void labelSet() {
				String str = labelEditor.getText().trim();
				if( "".equals( str ) ) return;
				label.setText( labelEditor.getText() );
				panel.showWidget( 0 );
				panel.remove( 1 );
				labelEditor = null;
				if( listener != null )
					listener.labelChanged( CustomField.this );
				addField();
			}
			
		}
		
		FieldLabeler labeler;
		FieldEditor editor;
		
		HorizontalPanel panel;
		FieldListener listener;
		
		public CustomField( String strLabel, String strValue ) {
			labeler = new FieldLabeler( strLabel );
			editor = new TextEditor( this, strValue );
		}
		
		public void fireValueChanged() {
			listener.valueChanged( this );
		}
		
		public IsWidget getLabeler() {
			return labeler;
		}
		
		public IsWidget getEditor() {
			return editor;
		}
		
		public String getName() {
			return labeler.getName();
		}
		
		public String getValue() {
			return editor.getValue();
		}
		
		@Override
		public Panel asWidget() {
			if( panel == null ) {
				panel = new HorizontalPanel();
				panel.add( labeler );
				labeler.asWidget().getElement().getStyle().setMarginRight( 6, Unit.PX );
				panel.add( editor );
			}
			return panel;
		}
		
		public void addListener( FieldListener listener ) {
			this.listener = listener;
		}
		
		public void removeListener( FieldListener listener ) {
			this.listener = null;
		}
		
	}
	
	VerticalPanel panel = new VerticalPanel();
	
	List<CustomField> fields = new ArrayList<CustomField>();
	
	List<FieldListener> listeners = new ArrayList<FieldListener>();
	
	FieldListener listener = new FieldListener() {
		@Override
		public void labelChanged( CustomField field ) {
			// TODO restore this?
//			Anchor a = new Anchor( "-" );
//			a.setTitle( "Remove" );
//			a.addClickHandler( new ClickWrapper<CustomField>( field ) {
//				@Override
//				public void onClick( ClickEvent event ) {
//					fields.remove( getValue() );
//					getValue().getEditor().asWidget().getParent().removeFromParent();
//				}} );
//			field.asWidget().add( a );
		}
		@Override
		public void valueChanged( CustomField field ) {
			for( FieldListener l : listeners ) {
				l.valueChanged( field );
			}
		}
	};
	
	public CustomizableForm() {
		
	}
	
	@Override
	public Widget asWidget() {
		return panel;
	}
	
	public void addFieldListener( FieldListener l ) {
		listeners.add( l );
	}
	
	public void addField() {
		addField( "", "" );
	}
	
	public CustomField addField( String strLabel, String strValue ) {
		CustomField field = new CustomField( strLabel, strValue );
		field.addListener( listener );
		panel.add( field.asWidget() );
		return field;
	}
	
	public Collection<CustomField> fields() {
		return this.fields;
	}
	
	public void enableFastInsert() {
		addField( "", "" );
	}
	
}
