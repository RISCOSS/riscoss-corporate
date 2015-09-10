/**
 * 
 */
package eu.riscoss.client;

import java.util.ArrayList;
import java.util.List;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONValue;

import eu.riscoss.client.RiscossCall.Argument;

/**
 * Holds the data and executes JSON calls for the RISCOSS platform
 * @author morandini
 *
 */
abstract class JSONCall {
	
	protected JSONData d;
	
	public JSONCall(JSONData data) {
		d = data;
	}
	
	/**
	 * POST without body
	 * @param cb
	 */
	public void post( JsonCallback cb ) {
		//post( null, cb );
		Resource res = new Resource( GWT.getHostPageBaseURL() + "api/" + d.service + ( d.domain != null? "/" + d.domain : "" ) + d.fx );
		for (Argument arg : d.args)
			res = res.addQueryParam(arg.key, arg.value);
		res.post().header( "token", d.token ).send( cb );
	}
	
	//don't put a body inside!
	public void get( JsonCallback cb ) {
		Resource res = new Resource( GWT.getHostPageBaseURL() + "api/" + d.service + ( d.domain != null? "/" + d.domain : "" ) + d.fx );
		for (Argument arg : d.args)
			res = res.addQueryParam(arg.key, arg.value);
		res.get().header( "token", d.token ).send( cb );
		//get( null, cb );
	}
	
	public void put( JsonCallback cb ) {
		Resource res = new Resource(GWT.getHostPageBaseURL() + "api/" + d.service + (d.domain != null ? "/" + d.domain : "") + d.fx);
		for (Argument arg : d.args)
			res = res.addQueryParam(arg.key, arg.value);
		res.put().header( "token", d.token ).send( cb );
		//put( null, cb );
	}
	
	//don't put a body inside!
	public void delete( JsonCallback cb ) {
		Resource res = new Resource(GWT.getHostPageBaseURL() + "api/" + d.service + (d.domain != null ? "/" + d.domain : "") + d.fx);
		for (Argument arg : d.args)
			res = res.addQueryParam(arg.key, arg.value);
		res.delete().header( "token", d.token ).send( cb );
//		delete( null, cb );
	}
	
	/**
	 * 
	 * @param json set as headerparam
	 * @param cb
	 */
//	public void get( JSONValue json, JsonCallback cb ) {
//		Resource res = new Resource( GWT.getHostPageBaseURL() + "api/" + d.service + ( d.domain != null? "/" + d.domain : "" ) + d.fx );
//		for (Argument arg : d.args)
//			res = res.addQueryParam(arg.key, arg.value);
//		Log.println("[get] json:"+json);
//		if (json != null){
//			Log.println("[get] json:"+json);
//			res.get().header( "token", d.token ).text(json.toString()).send( cb );
//		} else
//			res.get().header( "token", d.token ).send( cb );
//	}
	
	/**
	 * POST with JSONValue as body 
	 * @param json set as body, to be read server-side by specifying a parameter with no @ PARAM
	 * @param cb
	 */
	public void post( JSONValue json, JsonCallback cb ) {
		Resource res = new Resource( GWT.getHostPageBaseURL() + "api/" + d.service + ( d.domain != null? "/" + d.domain : "" ) + d.fx );
		for (Argument arg : d.args)
			res = res.addQueryParam(arg.key, arg.value);
//		if (json != null)
//			res.options().json(json);
//		res.post().header( "token", d.token ).send( cb );
//		Log.println("[get] json:"+json);
		if (json == null)
			throw new RuntimeException("json body parameter = null!");
//		if (json != null){
			
		res.post().header( "token", d.token ).json(json).send( cb );
//		} else
//			res.post().header( "token", d.token ).send( cb );
	}
	
	/**
	 * 
	 * @param json set as headerparam
	 * @param cb
	 */
//	public void put(JSONValue json, JsonCallback cb) {
//		Resource res = new Resource(GWT.getHostPageBaseURL() + "api/" + d.service + (d.domain != null ? "/" + d.domain : "") + d.fx);
//		for (Argument arg : d.args)
//			res = res.addQueryParam(arg.key, arg.value);
//		if (json != null)
//			res.options().json(json);
//		res.put().header( "token", d.token ).send( cb );
//	}
	
	/**
	 * 
	 * @param json set as headerparam
	 * @param cb
	 */
//	public void delete(JSONValue json, JsonCallback cb) {
//		Resource res = new Resource(GWT.getHostPageBaseURL() + "api/" + d.service + (d.domain != null ? "/" + d.domain : "") + d.fx);
//		for (Argument arg : d.args)
//			res = res.addQueryParam(arg.key, arg.value);
//		if (json != null)
//			res.options().json(json);
//		res.delete().header( "token", d.token ).send( cb );
//	}
}
