package eu.riscoss.client;

import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Cookies;

import eu.riscoss.shared.CookieNames;

public class RiscossCall extends JSONCall{
	
	public static final String CONTEXTUALINFO = "ci";
	
	//called from the static methods
	private RiscossCall() {
		super(new JSONData());
	}


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
		return new RiscossCall().withDomain( domain );
	}
	
	/**
	 * This method creates a RiscossCall objects and initializes it with 
	 * token and domain currently stored in the cookies
	 * @return
	 */
	public static RiscossCall fromCookies() {
		return new RiscossCall().withToken( getToken() ).withDomain( getDomain() );
	}
	
	public static RiscossCall plainCall(){
		return new RiscossCall();
	}
	
	
	/**
	 * set the domain
	 * @param domainName
	 * @return
	 */
	public RiscossCall withDomain( String domainName ) {
		d.domain = domainName;
		return this;
	}
//	private RiscossCall domain( String domainName ) {
//	this.domain = domainName;
//	return this;
//}
	
	/**
	 * Sets the token. Token should be set only from cookie to align access.
	 * @param token
	 * @return
	 */
	@Deprecated
	RiscossCall withToken( String token ) {
		d.token = token;
		return this;
	}
	
	/**
	 * set the service. Deprecated. Use the specific service methods rcs(), layers(), entities(),...!
	 * @param name
	 * @return
	 */
	@Deprecated
	public RiscossCall withService( String serviceName ) {
		d.service = serviceName;
		return this;
	}
	
	//deleted. use the constructor!
//	private Service service( String name ) {
//		this.service = name;
//		return new Service();
//	}
	
	/**
	 * Please comment this when you use it!!
	 * @param value
	 * @return
	 */
	public RiscossCall withObject( JSONValue value ) {
		d.value = value;
		return this;
	}
	
	/**
	 * set service Risk Configurations "rcs"
	 * @return
	 */
	public Service rcs() {
		return new Service( "rcs" );
	}
	
	/**
	 * set service Risk Data Collectors "rdcs".
	 * The list of RDCS is domain-independent
	 * @return
	 */
	public Service rdcs() {
		return new Service( "rdcs" );
	}
	
	/**
	 * set service = "layers"
	 * @return 
	 */
	public Service layers() {
		return new Service( "layers" );
	}
	
	/**
	 * set service Authentication "auth"
	 * @return
	 */
	public Service auth() {
		return new Service( "auth" );
	}
	
	/**
	 * service = "entities"
	 * @return
	 */
	public Service entities() {
		return new Service( "entities" );
	}
	/**
	 * service = "models"
	 * @return
	 */
	public Service models() {
		return new Service( "models" );
	}
	/**
	 * service = "rdr"
	 * @return
	 */
	public Service rdr() {
		return new Service( "rdr" );
	}
	/**
	 * service = "analysis"
	 * @return
	 */
	public Service analysis() {
		return new Service( "analysis" );
	}
	/**
	 * service = "admin"
	 * service used by the SecurityManager
	 * @return
	 */
	public Service admin() {
		return new Service( "admin" ); //service used by the SecurityManager
	}
	
	/**
	 * Defines the possible functions to apply to a Service 
	 * 
	 */
	public class Service extends JSONCall{
		
		Service(String name) {
			super(RiscossCall.this.d);
			d.service = name;
		}
		
		public Function fx( String path ) {
			return new Function( path );
		}
		
//		/**
//		 * adds a function "/domains"
//		 * TODO: check if and why it fits here...
//		 * @return
//		 */
//		public Function domains() {
//			return new Function( "domains" );
//		}
		/**
		 * adds a function "/domainname"
		 * TODO: check if and why it fits here...
		 * @return
		 */
		public Function domain(String domainname) {
			return new Function( domainname );
		}
		/**
		 * lists all items available in the selected service
		 * @return
		 */
		public Function list() {
			return new Function().list(); //new Function( "/list" );
		}
		
//		/**
//		 * to delete...
//		 * @param selectedLayer
//		 * @return
//		 */
//		public Function layer( String selectedLayer ) {
//			return item( selectedLayer );
//		}
		
		/**
		 * access a single item for the selected service
		 * @param selectedLayer
		 * @return
		 */
		public Function item( String selectedItem ) {
			return new Function( selectedItem );
		}
//		/**
//		 * creates a new item in the selected service
//		 * @return
//		 */
//		public Function create() {
//			//return new Function( "/create" );
//			return new Function().create();
//		}
		/**
		 * creates a new item in the selected service
		 * @param name name of the item, needs to be present for every "create"
		 * @return
		 */
		public Argument create(String name) {		
			return new Function("create").arg("name", name );
			//return new Function().create(name); //( "/create" );
		}
	}
	
	public class Function extends JSONCall {
		
		public Function() {
			super(RiscossCall.this.d);
		}
		
		private Function( String path ) { //from outside, call fx()!
			super(RiscossCall.this.d);
			d.fx = d.fx + "/" + path;
		}
		/**
		 * Create additional own path parameters (e.g. parents, hierarchy, chunks, newrun, whatif,...)
		 * @param path the name of the function, a REST call path parameter
		 * @return
		 */
		public Function fx( String path ) {
			return new Function( path );
		}
		/**
		 * Used to list some sub-content for a specific item defined as service.
		 * @return
		 */
		public Function list() {
			return new Function("list");
		}
		
//		/**
//		 * adds a function "/create"
//		 * @return
//		 */
//		public Function create() {
//			return new Function("create");
//		}
		
//		/**
//		 * adds a function "/create"
//		 * @param name added as argument
//		 * @return
//		 */
//		public Function create(String name) {
//			arg("name", name );
//			return new Function("create");
//		}
		/**
		 * adds a function "/delete"
		 * @return
		 */
		public Function delete() {
			return new Function("delete");
		}
		/**
		 * adds a function "/rename"
 		 * @param newname new name of the item, needs to be present for any "rename"
		 * @return
		 */
		public Argument rename(String newname) {
			return new Function("rename").arg("newname", newname );
		}
		/**
		 * adds a function "/store"
		 * @return
		 */
		public Function store() {
			return new Function("store");
		}
		/**
		 * creates a new item for the selected function
		 * @param name name of the item, needs to be present for every "create"
		 * @return
		 */
		public Argument create(String name) {		
			return new Function("create").arg("name", name );
		}
		/**
		 * adds a function "/get"
		 * @return
		 */
		public Function get() {
			return new Function("get");
		}
		/**
		 * adds a function "/blob" that returns a large object (e.g. file content)
		 * @return
		 */
		public Function blob() {
			return new Function("blob");
		}
		
//		/**
//		 * Adds a function "/properties/" + name
//		 * @param name
//		 * @return
//		 */
//		public Function property( String name ) {
//			return new Function( "properties/" + name );
//		}
		
		/**
		 * adds an argument to the call, as a key-value pair
		 * @param key
		 * @param value
		 * @return
		 */
		public Argument arg( String key, String value ) {
			return new Argument( key, value );
		}

	}
	
	public class Argument extends JSONCall{
		String key;
		String value;
		/**
		 * adds an argument to the call, as a key-value pair
		 * @param key
		 * @param value
		 * @return
		 */
		public Argument( String key, String value ) {
			super(RiscossCall.this.d);
			this.key = key;
			this.value = value;
			d.args.add( this );
		}
		/**
		 * adds an argument to the call, as a key-value pair
		 * @param key
		 * @param value
		 * @return
		 */
		public Argument arg( String key, String value ) {
			return new Argument( key, value );
		}


	}
}
