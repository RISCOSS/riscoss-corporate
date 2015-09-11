package eu.riscoss.client.auth;

import java.util.Date;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

import eu.riscoss.client.Log;
import eu.riscoss.client.auth.LoginWindow.LoginListener;
import eu.riscoss.shared.CookieNames;

public class ModAuth implements EntryPoint {
	LoginWindow loginWindow = new LoginWindow();
	public void onModuleLoad() {
		loginWindow.setLoginListener( new LoginListener(){
			@Override
			public void onLoginSucceeded( String token ) {
				Window.Location.reload();
			}
			@Override
			public void onLoginFailed() {
				Window.alert( "Wrong username and/or password" );
			}
			@Override
			public void onAccountRequested( RegistrationInfo info ) {
				new Resource( GWT.getHostPageBaseURL() + "api/auth/register" ).
				post().header( "username", info.getEmail() ).
				header( "password", info.getPassword() ).send( new JsonCallback() {
					@Override
					public void onSuccess(Method method, JSONValue response) {
//						Window.alert( "" + response );
						loginWindow.hideRegistrationForm();
					}
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert( exception.getMessage() );
					}
				});
			}
			@Override
			public void onLoginRequested( Credentials cred ) {
				new Resource( GWT.getHostPageBaseURL() + "api/auth/login" ).post().
				header( "username", cred.getUserId() ).
				header( "password", cred.getPassword() ).
				send( new JsonCallback() {
					
					@Override
					public void onSuccess(Method method, JSONValue response) {
						
						try {
							String result = response.isString().stringValue();
							
							if( result == null )
								return;
							
							// TODO
//							if( loginWindow.isRememberMeChecked() ) {
								Date d = new Date();
								// One year (365 days) expiration
								d.setTime( d.getTime() + (1000 * 60 * 60 * 24 * 365) );
								Cookies.setCookie( CookieNames.TOKEN_KEY, result, d );
								Log.println( result );
								Log.println( "" + result.length() );
								Log.println( Cookies.getCookie( CookieNames.TOKEN_KEY ) );
//							}
							
								onLoginSucceeded( result );
								
						}
						catch( Exception ex ) {
							Window.alert( ex.getMessage() );
						}
					}
					
					@Override
					public void onFailure(Method method, Throwable exception) {
						Window.alert( exception.getMessage() );
					}
				});
			}
		} );
		loginWindow.show();
	}
}
