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
	
	//TODO: gestire Token!
	//TODO: look come gestire DOMAIN!
	public JSONCall(JSONData data) {
		d = data;
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
		Resource res = new Resource( GWT.getHostPageBaseURL() + "api/" + d.service + ( d.domain != null? "/" + d.domain : "" ) + d.fx );
		for (Argument arg : d.args)
			res = res.addQueryParam(arg.key, arg.value);
		if (json != null)
			res.options().json(json);
		res.get().header( "token", d.token ).send( cb );
	}
	
	public void post( JSONValue json, JsonCallback cb ) {
		Resource res = new Resource( GWT.getHostPageBaseURL() + "api/" + d.service + ( d.domain != null? "/" + d.domain : "" ) + d.fx );
		for (Argument arg : d.args)
			res = res.addQueryParam(arg.key, arg.value);
		if (json != null)
			res.options().json(json);
		res.post().header( "token", d.token ).send( cb );
	}
	
	public void put(JSONValue json, JsonCallback cb) {
		Resource res = new Resource(GWT.getHostPageBaseURL() + "api/" + d.service + (d.domain != null ? "/" + d.domain : "") + d.fx);
		for (Argument arg : d.args)
			res = res.addQueryParam(arg.key, arg.value);
		if (json != null)
			res.options().json(json);
		res.put().header( "token", d.token ).send( cb );
	}
	
	public void delete(JSONValue json, JsonCallback cb) {
		Resource res = new Resource(GWT.getHostPageBaseURL() + "api/" + d.service + (d.domain != null ? "/" + d.domain : "") + d.fx);
		for (Argument arg : d.args)
			res = res.addQueryParam(arg.key, arg.value);
		if (json != null)
			res.options().json(json);
		res.delete().header( "token", d.token ).send( cb );
	}
}
