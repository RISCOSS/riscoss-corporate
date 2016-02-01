package eu.riscoss.agent;

import java.io.File;
import java.lang.reflect.Type;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import eu.riscoss.agent.services.AdminService;
import eu.riscoss.agent.services.BackboneService;
import eu.riscoss.agent.services.DomainService;
import eu.riscoss.agent.services.DomainsService;
import eu.riscoss.agent.services.UserService;
import eu.riscoss.shared.JUserInfo;

public class RiscossRESTClient {
	
	static abstract class SendOption<T> {
		private T value;
		public SendOption( T t ) {
			this.value = t;
		}
		public T getValue() {
			return this.value;
		}
		public abstract void applyTo( HttpRequestBase req );
	}
	
	public class Sender {
		
		String url = "";
		
		HttpRequestBase method;
		
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		Sender( HttpRequestBase method ) {
			this.method = method;
		}

		public Sender header( String key, String value ) {
			method.addHeader( key, value );
			return this;
		}
		
		String mklength( String string, int length ) {
			return String.format("%1$"+length+ "s", string);
		}
		
		private String send( SendOption<?> opt ) {
			
			try {
				
				method.setConfig(requestConfig);
				
				if( params.size() != 0 ) {
					StringBuilder requestUrl = new StringBuilder( method.getURI().toString() );
					String querystring = URLEncodedUtils.format( params, "utf-8");
					requestUrl.append("?");
					requestUrl.append(querystring);
					method.setURI( new URI( requestUrl.toString() ) );
				}
				
				System.out.print( mklength( method.getMethod(), 6 ) + " " + method.getURI() );
				
				if( opt != null ) {
					opt.applyTo( method );
					System.out.print( "     PAYLOAD: " + opt.getValue() );
				}
				
				System.out.println();
				
				HttpResponse response = client.execute( method );
				if (response.getStatusLine().getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					String ret = EntityUtils.toString(entity);
					System.out.println( "      RETURNED: " + ret );
					return ret;
				} else {
					System.out.println( "      RETURNED:");
					// something has gone wrong...
					return null; //response.getStatusLine().toString();
				}
			}
			catch( Exception ex ) {
				throw new RuntimeException( ex );
			}
		}
		
		public String send( String body ) {
			return send( new SendOption<String>( body ) {
				@Override
				public void applyTo( HttpRequestBase req ) {
					try {
						((HttpPost)req).setEntity( new StringEntity( getValue() ) );
					}
					catch( Exception ex ) {
					}
				}} );
		}
		
		public String send() {
			return send( (SendOption<?>)null );
		}
		
		public String send( File file ) {
			return send( new SendOption<File>( file ) {
				@Override
				public void applyTo( HttpRequestBase req ) {
					try {
						((HttpPost)req).setEntity( new FileEntity( getValue() ) );
					}
					catch( Exception ex ) {
					}
				}} );
		}
		
		public Sender param( String key, String value ) {
			params.add( new BasicNameValuePair( key, value ) );
			return this;
		}
		
	}
	
	String addr = "";
	String token = null;
	
	String service = "";
	
	Map<String,String> map = new HashMap<>();
	
	private HttpClient client;
	RequestConfig requestConfig = RequestConfig.custom()
			.setExpectContinueEnabled(false)
			.setCookieSpec(CookieSpecs.BEST_MATCH)
			.setRedirectsEnabled(false)
//			.setSocketTimeout(5000)
//			.setConnectTimeout(5000)
//			.setConnectionRequestTimeout(5000)
			.setStaleConnectionCheckEnabled(true)
			.build();
	
	
	Gson gson = new Gson();
	
	public RiscossRESTClient() {
		this( "http://127.0.0.1:8888" );
	}
	
	public RiscossRESTClient(String base_addr) {
		this.addr = base_addr;
		
		RegistryBuilder<ConnectionSocketFactory> connRegistryBuilder = RegistryBuilder.create();
		connRegistryBuilder.register("http", PlainConnectionSocketFactory.INSTANCE);
		try { // Fixing: https://code.google.com/p/crawler4j/issues/detail?id=174
		    // By always trusting the ssl certificate
		    SSLContext sslContext = SSLContexts.custom()
		            .loadTrustMaterial(null, new TrustStrategy() {
		                @Override
		                public boolean isTrusted(final X509Certificate[] chain, String authType) {
		                    return true;
		                }
		            }).build();
		    SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
		            sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		    connRegistryBuilder.register("https", sslsf);
		} catch (KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		Registry<ConnectionSocketFactory> connRegistry = connRegistryBuilder.build();
		PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(connRegistry);
		connectionManager.setMaxTotal(5);
		connectionManager.setDefaultMaxPerRoute(5);
		
		HttpClientBuilder clientBuilder = HttpClientBuilder.create();
		clientBuilder.setDefaultRequestConfig(requestConfig);
		clientBuilder.setConnectionManager(connectionManager);
		clientBuilder.setUserAgent("Cognitio");

		client = clientBuilder.build();
	}
	
	public void set( String key, String value ) {
		map.put(key, value);
	}
	
	public String get( String key, String def ) {
		String ret = map.get( key );
		if( ret == null ) ret = def;
		return ret;
	}
	
	public Sender service( HttpRequestBase method ) {
		Sender sender = new Sender( method );
		if( token != null ) sender.header( "token", token );
		return sender;
	}
	
	public Sender get( String path ) {
		return service( new HttpGet( addr + "/api/" + path ) );
	}
	
	public Sender post( String path ) {
		return service( new HttpPost( addr + "/api/" + path ) );
	}
	
	public Sender put( String path ) {
		return service( new HttpPut( addr + "/api/" + path ) );
	}
	
	public Sender delete( String path ) {
		return service( new HttpDelete( addr + "/api/" + path ) );
	}
	
	protected RiscossRESTClient access( String token ) {
		this.token = token;
		return this;
	}
	
	public List<JUserInfo> users() {
		String json = get( "admin/users/list" ).send();
		Type listType = new TypeToken<ArrayList<JUserInfo>>() {}.getType();
		List<JUserInfo> list = new Gson().fromJson(json, listType);
		return list;
	}
	
	public List<String> publicDomains() {
		String json = get( "admin/domains/public" ).send();
		Type listType = new TypeToken<ArrayList<String>>() {}.getType();
		List<String> list = new Gson().fromJson(json, listType);
		return list;
	}

	public List<JUserInfo> users( String domain ) {
		String json = get( "admin/" + domain + "/users/list" ).send();
		Type listType = new TypeToken<ArrayList<JUserInfo>>() {}.getType();
		List<JUserInfo> list = new Gson().fromJson(json, listType);
		return list;
	}

	public String getCachedToken() {
		return this.token;
	}

	public String getPredefinedRole( String domain ) {
		return get( "admin/" + domain + "/info" ).send();
	}
	
	public void login( String username, String pwd ) {
		
		String token = post( "auth/login" )
			.header( "username", username )
			.header( "password", pwd )
			.send();
		
		setToken( token );
		
	}
	
	public void logout() {
		setToken( null );
	}
	
	public AdminService admin() {
		return new AdminService( this );
	}
	
	public UserService user() {
		return new UserService( this );
	}

	public DomainService domain( String name ) {
		return new DomainService( this, name );
	}

	public void ensureUserExistence( String username, String pwd ) {
		login(username, pwd);
		if( this.token == null )
			user().register( username, pwd );
	}

	public void setToken( String token ) {
		this.token = token;
	}

	public DomainsService domains() {
		return new DomainsService( this );
	}

	public String getAddress() {
		return addr;
	}

	public String getToken() {
		return this.token;
	}
	
	public RiscossRESTClient clone() {
		
		RiscossRESTClient client = new RiscossRESTClient( "" );
		client.addr = this.addr;
		client.client = this.client;
		client.map.putAll( this.map );
		client.requestConfig = this.requestConfig;
		client.service = this.service;
		client.token = this.token;
		
		return client;
		
	}

	public BackboneService backbone() {
		return new BackboneService( this );
	}
	
}
