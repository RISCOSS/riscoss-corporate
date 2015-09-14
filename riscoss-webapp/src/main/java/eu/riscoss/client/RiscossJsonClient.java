/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/**
 * @author 	Alberto Siena
**/

package eu.riscoss.client;

import java.util.List;

import javax.ws.rs.QueryParam;

import org.fusesource.restygwt.client.JsonCallback;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.Resource;

import com.google.gwt.core.client.GWT;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;

import eu.riscoss.client.ui.WaitWidget;
import eu.riscoss.shared.CookieNames;
import eu.riscoss.shared.EAnalysisOption;

public class RiscossJsonClient {
	
	public static class JsonWaitWrapper implements JsonCallback {
		JsonCallback cb;
		public JsonWaitWrapper( JsonCallback cb ) {
			this.cb = cb;
			WaitWidget.get().show();
		}
		@Override
		public void onFailure( Method method, Throwable exception ) {
			cb.onFailure(method, exception);
		}
		@Override
		public void onSuccess( Method method, JSONValue response ) {
			WaitWidget.get().hide();
			cb.onSuccess(method, response);
		}
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
	
	public static void listLayers( JsonCallback cb ) {
		
		RiscossCall.fromCookies().layers().list().get( cb );
	}
	
	public static void createLayer( String name, String parentName, JsonCallback cb ) {
		
		RiscossCall.fromCookies().layers().create(name).arg( "parent", parentName ).post( cb );
		
	}
	
	public static void renameLayer( String oldLayerName, String newLayerName, JsonCallback cb ) {
		RiscossCall.fromCookies().layers().item( oldLayerName ).rename(newLayerName).post( cb );
//		new Resource( GWT.getHostPageBaseURL() + "api/layers/" + getDomain() + "/edit" )
//			.addQueryParam( "name", oldLayerName )
//			.addQueryParam( "newname", newLayerName )
//			.post().send( cb );
	}
	
	public static void deleteLayer( String layerName, JsonCallback cb ) {
		RiscossCall.fromCookies().layers().item(layerName).delete().delete( cb );
//		new Resource( GWT.getHostPageBaseURL() + "api/layers/" + getDomain() + "/delete" )
//		.addQueryParam( "name", layerName )
//		.delete().send( cb );
	}
	
	public static void getLayerContextualInfo( String layer, JsonCallback cb ) {
		RiscossCall.fromCookies().layers().item( layer ).fx( RiscossCall.CONTEXTUALINFO ).get( cb );
//		new Resource( GWT.getHostPageBaseURL() + "api/layers/" + getDomain() + "/ci" )
//			.addQueryParam( "layer", layer )
//			.get().send( cb );
	}

	public static void setLayerContextualInfo(String layer, JSONValue json, JsonCallback cb) {

		// CodecLayerContextualInfo codec = GWT.create(
		// CodecLayerContextualInfo.class );
		// String json = codec.encode( info ).toString();

		RiscossCall.fromCookies().layers().item(layer).fx(RiscossCall.CONTEXTUALINFO).post(json, cb);
		// new Resource( GWT.getHostPageBaseURL() + "api/layers/" + getDomain()
		// + "/ci" )
		// .addQueryParam( "layer", layer )
		// .put().header( "info", json ).send( cb );
	}

	//TODO:entities
	//("/{domain}/list")
	public static void listEntities( JsonCallback cb ) {
		RiscossCall.fromCookies().entities().list().get( cb );
		//new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/list" ).get().send( cb );
	}
	
	/**
	 * Lists all entities for layer. Note: if layer = "", returns the list of all entities.
	 * call: ("/{domain}/{layer}/list")
	 * @param layer
	 * @param cb
	 */
	public static void listEntities( String layer, JsonCallback cb ) {
		if (layer==null || layer=="")
			listEntities(cb);
		else
			RiscossCall.fromCookies().entities().item(layer).list().get( cb );
		//new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/list" ).get().send( cb );
	}
		
	
	public static void deleteEntity( String entity, JsonCallback cb ) {
		RiscossCall.fromCookies().entities().item(entity).delete().delete( cb );
//		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/delete" )
//			.addQueryParam( "entity", entity )
//			.delete().send( cb );
	}
	/**
	 * Create an entity with at most a single parent. Add more parents with setParents()
	 * @param name
	 * @param layer
	 * @param parent single parent, empty string if none. 
	 * @param cb
	 */
	public static void createEntity( String name, String layer, String parent, JsonCallback cb ) {
		RiscossCall.fromCookies().entities().create(name).arg("parent", parent).arg("layer", layer).post( cb );
		
//		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/new" )
//			.addQueryParam( "name", name )
//			.addQueryParam( "parent", parent )
//			.addQueryParam( "layer", layer )
//			.post().send( cb );
	}
	
//	public static void createEntity( String name, String layer, List<String> parents, JsonCallback cb ) {
//		JSONObject o = new JSONObject();
//		o.put( "name", new JSONString( name ) );
//		o.put( "layer", new JSONString( layer ) );
//		{
//			JSONArray a = new JSONArray();
//			for( String p : parents ) {
//				a.set( a.size() -1, new JSONString( p ) );
//			}
//			o.put( "parents", a );
//		}
//		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/create" )
//			.post()
//			.header( "info", o.toString() )
//			.send( cb );
//	}

	public static void setParents( String entity, List<String> entities, JsonCallback cb ) {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		for( String e : entities ) {
			array.set( array.size(), new JSONString( e ) );
		}
		json.put( "list", array );

		RiscossCall.fromCookies().entities().item(entity).fx("parents").post(json,cb);

		//			new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/parent" )
		//				.addQueryParam( "entity", entity )
		//				.post().header( "entities", json.toString() ).send( cb );
	}
	
//	public static void getParents(...){
//		... if needed
//	} 
	
//	public static void setChildren(...){
//	... if needed
//} 
	
	public static void listRDCs( String entity, JsonCallback cb ) {
		RiscossCall.fromCookies().entities().item(entity).fx("rdcs").list().get(cb);

//		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/rdcs/list" )
//			.addQueryParam( "entity", entity )
//			.get().send( cb );
	}
	
	public static void saveRDCs( JSONObject rdcMap, String entity, JsonCallback cb ) {
		RiscossCall.fromCookies().entities().item(entity).fx("rdcs").store().post(rdcMap,cb);

//		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/rdcs/save" )
//			.addQueryParam( "entity", entity )
//			.put().header( "rdcmap", rdcMap.toString() ).send( cb );
	}
	
	public static void runRDCs( String entityName, JsonCallback cb ) {
		RiscossCall.fromCookies().entities().item(entityName).fx("rdcs").fx("newrun").get(cb);

//		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/rdcs/newrun" )
//			.addQueryParam( "entity", entityName )
//			.get().send( cb );
	}
	

	//TODO: put from REST Path entities/ to Path rd/ 
	public static void getRiskData( String entity, JsonCallback cb ) {
		RiscossCall.fromCookies().entities().item(entity).fx("rd").get(cb);

//		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/rd/get" )
//			.addQueryParam( "entity", entity )
//			.get().send( cb );
	}
	
	public static void getEntityData( String entity, JsonCallback cb ) {
		RiscossCall.fromCookies().entities().item(entity).fx("data").get(cb);

//		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/data" )
//			.addQueryParam( "entity", entity )
//			.get().send( cb );
	}
	
	public static void getHierarchyInfo( String entity, JsonCallback cb ) {
		RiscossCall.fromCookies().entities().item(entity).fx("hierarchy").get(cb);

//		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/hierarchy" )
//			.addQueryParam( "entity", entity )
//			.get().send( cb );
	}

	public static void setChildren( String entity, List<String> entities, JsonCallback cb ) {
		JSONObject json = new JSONObject();
		JSONArray array = new JSONArray();
		for( String e : entities ) {
			array.set( array.size(), new JSONString( e ) );
		}
		json.put( "list", array );
		
		RiscossCall.fromCookies().entities().item(entity).fx("children").post(json,cb);

//		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/children" )
//			.addQueryParam( "entity", entity )
//			.post().header( "entities", json.toString() ).send( cb );
	}

	public static void getRASResults( String entity, JsonCallback cb ) {
		RiscossCall.fromCookies().entities().item(entity).fx("ras").get(cb);

//		new Resource( GWT.getHostPageBaseURL() + "api/entities/" + getDomain() + "/entity/ras" )
//			.addQueryParam( "entity", entity )
//			.get().send( cb );
	}
	

	//TODO:rcds
	public static void listRDCs( JsonCallback cb ) {
		RiscossCall.plainCall().rdcs().list().get(cb);
//		new Resource( GWT.getHostPageBaseURL() + "api/rdcs/list" ).get().send( cb );
	}
	
	//TODO:rcs
	public static void listRCs( String entity, JsonCallback cb ) {
		RiscossCall.fromCookies().rcs().list().arg("entity", entity).get(cb);
//		new Resource( GWT.getHostPageBaseURL() + "api/rcs/" + getDomain() + "/list" ).get().send( cb );
	}
	
	/**
	 * Without defining an entity, all the risk configurations are returned
	 * @param cb
	 */
	public static void listRCs( JsonCallback cb ) {
		RiscossCall.fromCookies().rcs().list().get(cb);
//		new Resource( GWT.getHostPageBaseURL() + "api/rcs/" + getDomain() + "/list" ).get().send( cb );
	}
	
	public static void getRCContent( String rcname, JsonCallback cb ) {
		RiscossCall.fromCookies().rcs().item(rcname).get().get(cb);

//		new Resource( GWT.getHostPageBaseURL() + "api/rcs/" + getDomain() + "/rc/get" )
//			.addQueryParam( "name", rcname )
//			.get().send( cb );
	}

	public static void setRCContent( String rcname, SimpleRiskCconf rc, JsonCallback cb ) {
		try {
			RiscossCall.fromCookies().rcs().item(rcname).store().post(rc.json, cb);

//		JSONObject json = new JSONObject();
//		json.put( "rc", new JSONString( rcName ) );
//		json.put( "models", rc.getModels() );
//		Window.alert( "" + rc );
//		new Resource( GWT.getHostPageBaseURL() + "api/rcs/" + getDomain() + "/rc/put")
//			.addQueryParam( "content", rc.json.toString() )
//			.put().send( cb );
		}
		catch( Exception ex ) {
			Window.alert( ex.getMessage() );
		}
	}
	
	public static void createRC( String name, JsonCallback cb ) {
		RiscossCall.fromCookies().rcs().create(name).post(cb);

//		new Resource( GWT.getHostPageBaseURL() + "api/rcs/" + getDomain() + "/rc/new" )
//			.addQueryParam( "name", name )
//			.post().send( cb );
	}
	
	public static void deleteRC( String name, JsonCallback cb ) {
		RiscossCall.fromCookies().rcs().item(name).delete().delete(cb);
//		new Resource( GWT.getHostPageBaseURL() + "api/rcs/rc/delete" )
//			.addQueryParam( "name", name )
//			.delete().send( cb );
	}
	
	//TODO:models
	public static void listModels( JsonCallback cb ) {
		RiscossCall.fromCookies().models().list().get(cb);
//		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/list").get().send( cb );
	}
	
	//unused, created by uploading!
//	public static void createModelEntry( String name, JsonCallback cb ) {
//		RiscossCall.fromCookies().models().create(name).post(cb);
//
////		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/model/new" )
////			.addQueryParam( "name", name )
////			.post().send( cb );
//	}
	
	public static void getModelinfo( String name, JsonCallback cb ) {
		RiscossCall.fromCookies().models().item(name).get().get(cb);

//		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/model/get" )
//			.addQueryParam( "name", name )
//			.get().send( cb );
	}

	public static void getModelBlob( String name, JsonCallback cb ) {
		RiscossCall.fromCookies().models().item(name).blob().get(cb);
//		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/model/blob" )
//		.addQueryParam( "name", name )
//		.get().send( cb );
	}
	
	
	public static void deleteModel( String name, JsonCallback cb ) {
		RiscossCall.fromCookies().models().item(name).delete().delete(cb);
//		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/model/delete" )
//			.addQueryParam( "name", name )
//			.delete().send( cb );
	}
	
	public static void changeModelName( String name, String newName, JsonCallback cb){
		RiscossCall.fromCookies().models().item(name).rename(newName).post(cb);

//		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/model/changename" )
//		.addQueryParam( "name", name )
//		.addQueryParam( "newname", newName )
//		.post().send( cb );
	}
	
	public static void listChunks( List<String> list, JsonCallback cb ) {
		RiscossCall.fromCookies().models().fx("chunks").post(mkJsonArray(list), cb);
//		new Resource( GWT.getHostPageBaseURL() + "api/models/" + getDomain() + "/model/chunks" )
//			.get().header( "models", mkJsonArray( list ).toString() ).send( cb );
	}
	
	public static void listChunkslist( List<String> list, JsonCallback cb ) {
		RiscossCall.fromCookies().models().fx("chunklist").post(mkJsonArray(list), cb);

	}
	
	//TODO:analysis
	
	/**
	 * 
	 * @param entity can be empty
	 * @param rc can be empty
	 * @param cb
	 */
	public static void listRiskAnalysisSessions(String entity, String rc, JsonCallback cb){
		RiscossCall.fromCookies().analysis().fx("session").list().arg("entity", entity).arg("rc", rc).get(cb);;
	}
	
	public static void creteRiskAnalysisSession(String name, String riskConf, String target, JsonCallback cb){
		RiscossCall.fromCookies().analysis().fx("session").create(name).arg("rc",riskConf).arg("target", target).post(cb);
	}
	
	public static void updateSessionData(String riskAnalysisSession, JsonCallback cb){
		//new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + RiscossJsonClient.getDomain() +
		//"/session/" + selectedRAS + "/update-data" ).get().send( new JsonCallback() {
		RiscossCall.fromCookies().analysis().fx("session").fx(riskAnalysisSession).fx("update-data").get(cb);
	}
	
	public static void getAnalysisMissingData(String riskAnalysisSession, JsonCallback cb){
//		new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + getDomain() + "/session/" + riskAnalysisSession + "/missing-data" )
//		.put().header( "values",  ).send(riskdata, cb ); 
		RiscossCall.fromCookies().analysis().fx("session").fx(riskAnalysisSession).fx("missing-data").get( cb );
	}
	
	public static void setAnalysisMissingData(String riskAnalysisSession, JSONValue riskdata, JsonCallback cb){
//		new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + getDomain() + "/session/" + riskAnalysisSession + "/missing-data" )
//		.put().header( "values",  ).send(riskdata, cb ); 
		RiscossCall.fromCookies().analysis().fx("session").fx(riskAnalysisSession).fx("missing-data").post(riskdata, cb);
	}

	public static void getSessionSummary(String riskAnalysisSession, JsonCallback cb){
		RiscossCall.fromCookies().analysis().fx("session").fx(riskAnalysisSession).fx("summary").get(cb);
	}
	
	public static void getSessionResults(String riskAnalysisSession, JsonCallback cb){
		RiscossCall.fromCookies().analysis().fx("session").fx(riskAnalysisSession).fx("results").get(cb);
	}
	
	/**
	 * analysis().fx("session").fx(riskAnalysisSession).delete()
	 * @param riskAnalysisSession
	 * @param cb
	 */
	public static void deleteRiskAnalysisSession(String riskAnalysisSession, JsonCallback cb){
		RiscossCall.fromCookies().analysis().fx("session").fx(riskAnalysisSession).delete().delete(cb);
	}
	
	public static void rerunRiskAnalysisSession(String riskAnalysisSession, String strOpt, JsonCallback cb){
		RiscossCall.fromCookies().analysis().fx("session").fx(riskAnalysisSession).fx("newrun").arg("opt",strOpt).post(cb);
	}
	
	/////Analysis////////
	public static void runAnalysis( String target, String rc, String verbosity, EAnalysisOption opt, JSONObject values, JsonCallback cb ) {
		//TODO:convert to new format!!
		new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + getDomain() + "/new" )
			.addQueryParam( "target", target )
			.addQueryParam( "rc", rc )
			.addQueryParam( "verbosity", verbosity )
			.addQueryParam( "opt", opt.name() )
			.post()
			.header( "customData", values.toString() )
			.header( "token", getToken() )
			.send( new JsonWaitWrapper( cb ) );
	}
	
	public static void runWhatIfAnalysis( List<String> models, JSONObject values, JsonCallback cb ) {	
		new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + getDomain() + "/whatif" )
			.addQueryParam( "models", mkJsonArray( models ).toString() )
			.post().header( "values", values.toString() )
			.header( "token", getToken() )
			.send( cb );
	}
	
	//json put in body to respect the current API!
	public static void runAHP( String values, JsonCallback cb ) {	
		new Resource( GWT.getHostPageBaseURL() + "api/analysis/" + getDomain() + "/ahp" )
			.post().header( "token", RiscossCall.getToken() )
			.text( values ).send( cb ); 
	}
	
	public static JSONArray mkJsonArray( List<String> list ) {
		JSONArray array = new JSONArray();
		for( String e : list ) {
			array.set( array.size(), new JSONString( e ) );
		}
		return array;
	}

	//TODO: rdr
	public static void postRiskData( JSONArray json, JsonCallback cb ) {
		RiscossCall.fromCookies().rdr().fx("store").post(json, cb);

//		new Resource( GWT.getHostPageBaseURL() + "api/rdr/" + getDomain() + "/store" )
//			.post().header( "json", o.toString() )
//			.send( cb );
	}
	

//	public static void getModelDescBlob( String name, JsonCallback cb ) {
//		new Resource( GWT.getHostPageBaseURL() + "models/descBlob" )
//		.addQueryParam( "name", name )
//		.get().send( cb );
//	}
	
	//TODO: admin
	public static void createDomain( String name, JsonCallback cb ) {
		RiscossCall.fromToken(getToken()).admin().fx("domains").create(name).post( cb );
		//new Resource( GWT.getHostPageBaseURL() + "api/admin/domains/create" )
	}	
	
	public static void listDomainsForUser( String username, JsonCallback cb ) {
		RiscossCall.fromToken(getToken()).admin().fx("domains").list().arg("username", username).get( cb );
	}
	
	public static void selectDomain(String domain, JsonCallback cb ) {
		RiscossCall.fromToken(getToken()).admin().item(domain).fx("domains").fx( "selected" ).post( cb );
		//RiscossCall.fromCookies().withDomain(null).admin().fx( "domains/selected" ).arg( "domain", Cookies.getCookie( CookieNames.DOMAIN_KEY ) ).post(
	}
	
	public static void getDomainInfo( String domainname, JsonCallback cb ) {
		RiscossCall.fromCookies().withDomain(domainname).admin().fx("info").get( cb );
	}
	
	public static void getDomainUsers( String domainname, JsonCallback cb ) {
		RiscossCall.fromCookies().withDomain(domainname).admin().fx("users").list().get( cb );
	}
	
	public static void setDomainUserRole( String domainname, String user, String role, JsonCallback cb ) {
		RiscossCall.fromCookies().withDomain(domainname).admin().fx("users").fx(user).fx("set").arg("role", role).post( cb );
	}
	
	public static void listUsers( JsonCallback cb ) {
		RiscossCall.fromToken(getToken()).admin().fx("users").list().get( cb );
	}
	
	
}
