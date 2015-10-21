package eu.riscoss.client.auth;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;

import eu.riscoss.client.auth.RegistrationPanel.Info;

public class LoginWindow extends DialogBox {
	
	public interface LoginListener {
		void onLoginRequested( Credentials cred );
		void onLoginSucceeded( String token );
		void onLoginFailed();
		void onAccountRequested( RegistrationInfo info );
	}
	
	TextBox			usernameBox;
	PasswordTextBox	pwdBox;
	CheckBox		chk;
	
	RegistrationPanel registrationPanel = null;
	
	LoginListener	listener;
	
	boolean			rememberMe = false;
	
	public static void showLoginDialog( LoginListener l ) {
		new LoginWindow( l ).show();
	}
	
	public LoginWindow() {
		this( null );
	}
	
	public LoginWindow( LoginListener l ) {
		
		super( false, false );
		
		setText( "Login" );
		
		listener = l;
		
		FlexTable table = new FlexTable();
		
		this.usernameBox = new TextBox();
		
		table.setWidget( 0, 0, new Label( "username:" ) );
		table.setWidget( 0, 1, usernameBox );
		
		this.pwdBox = new PasswordTextBox();
		
		table.setWidget( 1, 0, new Label( "password:" ) );
		table.setWidget( 1, 1, pwdBox );
		
		KeyUpHandler kh = new KeyUpHandler(){
			@Override
			public void onKeyUp( KeyUpEvent event ) {
				if( event.getNativeKeyCode() == KeyCodes.KEY_ENTER ) {
					checkFieldsAndLogin();
				}
			}
		};
		usernameBox.addKeyUpHandler( kh );
		pwdBox.addKeyUpHandler( kh );
		
		chk = new CheckBox( "Remember me" );
		table.setWidget( 2, 0, new Label( "" ) );
		table.setWidget( 2, 1, chk );
		
		Anchor a = new Anchor( "Register" );
		a.addClickHandler( new ClickHandler(){ 
			@Override
			public void onClick( ClickEvent event ) {
				showRegistrationForm();
			}
		} );
		
		Button btn = new Button( "Login" );
		btn.addClickHandler( new ClickHandler(){
			@Override
			public void onClick( ClickEvent event )
			{
				checkFieldsAndLogin();
			}
		} );
		table.setWidget( 3, 0, a );
		table.setWidget( 3, 1, btn );
		
		FlexCellFormatter f = table.getFlexCellFormatter();
		f.setHorizontalAlignment( 0, 0, HasHorizontalAlignment.ALIGN_RIGHT );
		f.setHorizontalAlignment( 1, 0, HasHorizontalAlignment.ALIGN_RIGHT );
		f.setHorizontalAlignment( 3, 0, HasHorizontalAlignment.ALIGN_RIGHT );
		f.setHorizontalAlignment( 3, 1, HasHorizontalAlignment.ALIGN_RIGHT );
		
		add( table );
		
		this.center();
		
		usernameBox.setFocus( true );
	}
	
	protected void checkFieldsAndLogin()
	{
		rememberMe = chk.getValue();
		
		if( listener != null )
			listener.onLoginRequested( 
					new Credentials( usernameBox.getText().trim(), pwdBox.getText().trim() ) );
		
	}
	
//	private void onLoginSucceeded( String token )
//	{
//		if( listener != null )
//			listener.onLoginSucceeded( token );
//	}
	
	void onLoginFailed() {
		Window.alert( "Invalid login/password" );
	}
	
	public void setLoginListener( LoginListener l ) {
		this.listener = l;
	}
	
	void showRegistrationForm()
	{
		registrationPanel = new RegistrationPanel( new AsyncCallback<RegistrationPanel.Info>(){
			
			@Override
			public void onSuccess( Info result )
			{
				if( listener != null ) {
					RegistrationInfo info = new RegistrationInfo();
					info.email = result.credentials.getUserId();
					info.password = result.credentials.getPassword();
					listener.onAccountRequested( info );
				}
			}
			
			@Override
			public void onFailure( Throwable caught )
			{
				Window.alert( caught.getMessage() );
			}
		} );
		
		registrationPanel.center();
	}
	
	public void hideRegistrationForm() {
		
		if( registrationPanel == null ) {
			return;
		}
		
		registrationPanel.hide();
		
	}
	
	public boolean isRememberMeChecked() {
		return rememberMe;
	}
	
}
