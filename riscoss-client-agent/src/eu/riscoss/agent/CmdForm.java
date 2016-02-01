package eu.riscoss.agent;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JTextField;

public class CmdForm {
	
	JFrame frame;
	JList<String> list;
	JTextField text;
	
	RiscossRESTClient client = new RiscossRESTClient( "http://127.0.0.1:8888" );
	
	public static void main( String[] args ) {
		new CmdForm().show();
	}
	
	public void show() {
		
		frame = new JFrame( "RISCOSS cmmand shell" );
		
		list = new JList<>( new DefaultListModel<String>() );
		text = new JTextField();
		
		text.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed( ActionEvent e ) {
				onCommand( text.getText() );
			}} );
		
		frame.getRootPane().setLayout( new BorderLayout() );
		frame.getRootPane().add( text, BorderLayout.SOUTH );
		frame.getRootPane().add( list, BorderLayout.CENTER );
		
		frame.setSize( 800, 600 );
		frame.setVisible( true );
		
	}
	
	protected void append( String string ) {
		((DefaultListModel<String>)list.getModel()).addElement( string );
	}
	
	protected void done() {
		text.setText( "" );
	}
	
	protected void onCommand( String string ) {
		
		string = string.trim();
		
		if( string.length() < 1 ) return;
		
		String[] tokens = string.split( "[ ]" );
		
		append( "> " + string );
		
		try {
			if( "login".equalsIgnoreCase( tokens[0] ) ) {
				client.login( tokens[1], tokens[2] );
				append( "token: " + client.getToken() );
			}
		}
		catch( Exception ex ) {
			append( ex.getMessage() );
		}
		
		done();
		
	}
	
}
