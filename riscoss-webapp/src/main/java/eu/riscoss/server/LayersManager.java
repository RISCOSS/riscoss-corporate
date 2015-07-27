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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eu.riscoss.client.Log;
import eu.riscoss.db.RiscossDB;
import eu.riscoss.shared.RiscossUtil;

@Path("layers")
public class LayersManager {
	
	public LayersManager() {
	}
	
	@GET
	@Path("/list")
	public String list() {
		
		JsonArray a = new JsonArray();
		
		RiscossDB db = DBConnector.openDB();
		try {
			for( String layer : db.layerNames() ) {
				JsonObject o = new JsonObject();
				o.addProperty( "name", layer );
				a.add( o );
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
		
		return a.toString();
	}
	
	@POST
	@Path("new")
	public void createNew(
			@QueryParam("name") String name,
			@QueryParam("parent") String parentName
			) {
		//attention:filename sanitation is not directly notified to the user
		name = RiscossUtil.sanitize(name.trim());
		
		parentName = parentName.trim();
		
		RiscossDB db = DBConnector.openDB();
		try
		{
			db.addLayer( name, parentName );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@DELETE
	@Path("delete")
	public void deleteLayer( @QueryParam("name") String name ) {
		RiscossDB db = DBConnector.openDB();
		try {
			db.removeLayer( name );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
	@POST
	@Path("edit")
	public void editLayer( 
			@QueryParam("name") String name, 
			@QueryParam("newname") String newName ) {
		RiscossDB db = DBConnector.openDB();
		try {
			db.renameLayer( name, newName );
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
}