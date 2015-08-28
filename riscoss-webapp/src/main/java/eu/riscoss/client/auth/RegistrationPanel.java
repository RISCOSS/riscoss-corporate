package eu.riscoss.client.auth;

import java.util.HashMap;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;


public class RegistrationPanel extends DialogBox
{
	public static class Info
	{
		Credentials				credentials;
		HashMap<String, String>	info	= new HashMap<String, String>();
	}
	
	private final AsyncCallback<Info>	cb;
	
	private final TextBox				txtEmail	= new TextBox();
	private final TextBox				txtPassword	= new PasswordTextBox();
	private final TextBox				txtName		= new TextBox();
	private final TextBox				txtSurname	= new TextBox();
	
	// DockLayoutPanel panel;
	
	public RegistrationPanel( AsyncCallback<Info> cb )
	{
		super( false, true );
		
		this.cb = cb;
		
		setText( "Register" );
		
		FlexTable flexTable = new FlexTable();
		FlexCellFormatter cellFormatter = flexTable.getFlexCellFormatter();
		flexTable.setWidth( "32em" );
		flexTable.setCellSpacing( 5 );
		flexTable.setCellPadding( 3 );
		
		// Add some text
		cellFormatter.setHorizontalAlignment( 0, 1, HasHorizontalAlignment.ALIGN_LEFT );
		// flexTable.setHTML( 0, 0, "Register" );
		// cellFormatter.setColSpan( 0, 0, 2 );
		
		addRow( flexTable, "First name", txtName );
		addRow( flexTable, "Last name", txtSurname );
		addRow( flexTable, "username", txtEmail );
		addRow( flexTable, "password", txtPassword );
		addRow( flexTable, "re-type password", new PasswordTextBox() );
		
		Button btn = new Button( "Ok" );
		btn.addClickHandler( new ClickHandler(){
			
			@Override
			public void onClick( ClickEvent event )
			{
				Info info = new Info();
				
				info.credentials = new Credentials( txtEmail.getText(), txtPassword.getText() );
				
				info.info.put( "name", txtName.getText() );
				info.info.put( "surname", txtSurname.getText() );
				
				RegistrationPanel.this.cb.onSuccess( info );
			}
		} );
		Anchor a = new Anchor( "Cancel" );
		a.addClickHandler( new ClickHandler(){
			
			@Override
			public void onClick( ClickEvent event )
			{
				hide();
			}
		} );
		addRow( flexTable, a, btn );
		
		add( flexTable );
	}
	
	private void addRow( FlexTable flexTable, Widget w1, Widget w2 )
	{
		int numRows = flexTable.getRowCount();
		flexTable.setWidget( numRows, 0, w1 );
		flexTable.setWidget( numRows, 1, w2 );
		flexTable.getFlexCellFormatter().setRowSpan( 0, 1, numRows + 1 );
	}
	
	private void addRow( FlexTable flexTable, String label, Widget w2 )
	{
		addRow( flexTable, new Label( label ), w2 );
	}
}
