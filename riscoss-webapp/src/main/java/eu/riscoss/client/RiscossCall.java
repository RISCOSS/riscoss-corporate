package eu.riscoss.client;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Cookies;

import eu.riscoss.shared.CookieNames;

public class RiscossCall {
	
	public static final String ContextualInfo = "ci";


	public static String getDomain() {
		String domain = Cookies.getCookie( CookieNames.DOMAIN_KEY );
		return domain;
	}
	
	public static String getToken() {
		String token = Cookies.getCookie( CookieNames.TOKEN_KEY );
		// A null or empty string gives an error when setting it in the request header
		if( token == null ) token = "-";
		return token;
	}
	
	
	public static RiscossCall fromToken( String token ) {
		return new RiscossCall().withToken( token );
	}
	
	public static RiscossCall fromDomain( String domain ) {
		return new RiscossCall().domain( domain );
	}
	
	/**
	 * This method creates a RiscossCall objects and initializes it with 
	 * token and domain currently stored in the cookies
	 * @return
	 */
	public static RiscossCall fromCookies() {
		return new RiscossCall().withToken( getToken() ).domain( getDomain() );
	}
	
	public class Service {
		public Function fx( String path ) {
			return new Function( "/" + path );
		}
		public Function domains() {
			return new Function( "/domains" );
		}
		public Function list() {
			return new Function( "/list" );
		}
		public Function layer( String selectedLayer ) {
			return new Function( "/" + selectedLayer );
		}
		public Function create() {
			return new Function( "/create" );
		}
	}
	
	public class Function {
		Function() {}
		Function( String path ) {
			fx += path;
		}
		public Function fx( String path ) {
			return new Function( path );
		}
		public Function list() {
			fx += "/list";
			return new Function();
		}
		public void get( JsonCallback cb ) {
			get( null, cb );
		}
		
		public void post( JsonCallback cb ) {
			post( null, cb );
		}
		
		public void put( JsonCallback cb ) {
			put( null, cb );
		}
		
		public void delete( JsonCallback cb ) {
			delete( null, cb );
		}
		public void get( JSONValue json, JsonCallback cb ) {
			RiscossCall.this.get( json, cb );
		}
		
		public void post( JSONValue json, JsonCallback cb ) {
			RiscossCall.this.post( json, cb );
		}
		
		public void put( JSONValue json, JsonCallback cb ) {
			RiscossCall.this.put( json, cb );
		}
		
		public void delete( JSONValue json, JsonCallback cb ) {
			RiscossCall.this.delete( json, cb );
		}
		public Argument arg( String key, String value ) {
			return new Argument( key, value );
		}
		public Function property( String name ) {
			return new Function( "/properties/" + name );
		}
	}
	
	public class Argument {
		String key;
		String value;
		public Argument( String key, String value ) {
			this.key = key;
			this.value = value;
			args.add( this );
		}
		public Argument arg( String key, String value ) {
			return new Argument( key, value );
		}
		public void get( JsonCallback cb ) {
			get( null, cb );
		}
		
		public void post( JsonCallback cb ) {
			post( null, cb );
		}
		
		public void put( JsonCallback cb ) {
			put( null, cb );
		}
		
		public void delete( JsonCallback cb ) {
			delete( null, cb );
		}
		public void get( JSONValue json, JsonCallback cb ) {
			RiscossCall.this.get( json, cb );
		}
		
		public void post( JSONValue json, JsonCallback cb ) {
			RiscossCall.this.post( json, cb );
		}
		
		public void put( JSONValue json, JsonCallback cb ) {
			RiscossCall.this.put( json, cb );
		}
		
		public void delete( JSONValue json, JsonCallback cb ) {
			RiscossCall.this.delete( json, cb );
		}
	}
	
	private String		domain = null;
	private String		token = "";
	private String		service = "";
	private String		fx = "";
	private JSONValue	value;
	List<Argument> args = new ArrayList<Argument>();
	
	
	private void get( JSONValue json, JsonCallback cb ) {
		Resource res = new Resource( GWT.getHostPageBaseURL() + "api/" + service + ( domain != null? "/" + domain : "" ) + fx );
		for( Argument arg : args ) res = res.addQueryParam( arg.key, arg.value );
		if( json != null ) res.options().json( json );
		res.get().header( "token", token ).send( cb );
	}
	
	private void post( JSONValue json, JsonCallback cb ) {
		Resource res = new Resource( GWT.getHostPageBaseURL() + "api/" + service + ( domain != null? "/" + domain : "" ) + fx );
		for( Argument arg : args ) res = res.addQueryParam( arg.key, arg.value );
		if( json != null ) res.options().json( json );
		res.post().header( "token", token ).send( cb );
	}
	
	private void put( JSONValue json, JsonCallback cb ) {
		Resource res = new Resource( GWT.getHostPageBaseURL() + "api/" + service + ( domain != null? "/" + domain : "" ) + fx );
		for( Argument arg : args ) res = res.addQueryParam( arg.key, arg.value );
		if( json != null ) res.options().json( json );
		res.put().header( "token", token ).send( cb );
	}
	
	private void delete( JSONValue json, JsonCallback cb ) {
		Resource res = new Resource( GWT.getHostPageBaseURL() + "api/" + service + ( domain != null? "/" + domain : "" ) + fx );
		for( Argument arg : args ) res = res.addQueryParam( arg.key, arg.value );
		if( json != null ) res.options().json( json );
		res.delete().header( "token", token ).send( cb );
	}
	
	public RiscossCall domain( String domainName ) {
		this.domain = domainName;
		return this;
	}
	
	public RiscossCall withDomain( String domainName ) {
		this.domain = domainName;
		return this;
	}
	
	RiscossCall withToken( String token ) {
		this.token = token;
		return this;
	}
	
	public RiscossCall withService( String serviceName ) {
		this.service = serviceName;
		return this;
	}
	
	public RiscossCall withObject( JSONValue value ) {
		this.value = value;
		return this;
	}
	
	private Service service( String name ) {
		this.service = name;
		return new Service();
	}
	
	public Service rcs() {
		return service( "rcs" );
	}
	
	public Service layers() {
		return service( "layers" );
	}
	
	public Service auth() {
		return service( "auth" );
	}
	
	public Service entities() {
		return service( "entities" );
	}
	
}
