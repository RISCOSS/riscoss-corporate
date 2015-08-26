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

package eu.riscoss.server;

import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eu.riscoss.db.RiscossDB;
import eu.riscoss.shared.JLayerContextualInfo;
import eu.riscoss.shared.RiscossUtil;

@Path("layers")
public class LayersManager {
	
	Gson gson = new Gson();
	
	public LayersManager() {
	}
	
	@GET @Path("/{domain}/list")
	public String list( @DefaultValue("Playground") @PathParam("domain") String domain ) {
		
		JsonArray a = new JsonArray();
		
		RiscossDB db = DBConnector.openDB( domain );
		try {
			for( String layer : db.layerNames() ) {
				JsonObject o = new JsonObject();
				o.addProperty( "name", layer );
				a.add( o );
			}
		}
		finally {
			if( db != null )
				DBConnector.closeDB( db );
		}
		
		return a.toString();
	}
	
	@POST @Path("{domain}/new")
	public void createNew(
			@DefaultValue("Playground") @PathParam("domain") String domain,
			@QueryParam("name") String name,
			@QueryParam("parent") String parentName
			) {
		//attention:filename sanitation is not directly notified to the user
		name = RiscossUtil.sanitize(name.trim());
		
		parentName = parentName.trim();
		
		RiscossDB db = DBConnector.openDB( domain );
		try
		{
			db.addLayer( name, parentName );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@DELETE @Path("{domain}/delete")
	public void deleteLayer( @DefaultValue("Playground") @PathParam("domain") String domain, @QueryParam("name") String name ) {
		
		RiscossDB db = DBConnector.openDB( domain );
		try {
			db.removeLayer( name );
		}
		finally {
			DBConnector.closeDB( db );
		}
		
	}
	
	@GET @Path( "{domain}/ci" )
	public String getContextualInfo( @DefaultValue("Playground") @PathParam("domain") String domain, @QueryParam("layer") String layer ) {
		
		RiscossDB db = DBConnector.openDB( domain );
		try {
			String json = db.getLayerData( layer, "ci" );
			if( json == null ) {
				JLayerContextualInfo info = new JLayerContextualInfo();
				json = gson.toJson( info );
			}
			return json;
		}
		finally {
			DBConnector.closeDB( db );
		}
		
	}
	
	@PUT @Path( "{domain}/ci" )
	public void setContextualInfo( 
			@DefaultValue("Playground") @PathParam("domain") String domain,
			@QueryParam("layer") String layer, 
			@HeaderParam("info") String json ) {
		
		RiscossDB db = DBConnector.openDB( domain );
		try {
			db.setLayerData( layer, "ci", json );
		}
		finally {
			DBConnector.closeDB( db );
		}
		
	}
	
	@POST
	@Path("{domain}/edit")
	public void editLayer( 
			@DefaultValue("Playground") @PathParam("domain") String domain,
			@QueryParam("name") String name, 
			@QueryParam("newname") String newName ) {
		
		System.out.println("Name change request: "+name+" to "+newName+".");
		
		RiscossDB db = DBConnector.openDB( domain );
		try {
			db.renameLayer( name, newName );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
}