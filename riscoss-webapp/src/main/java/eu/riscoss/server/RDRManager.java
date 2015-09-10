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

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.riscoss.db.RiscossDB;

@Path("rdr")
public class RDRManager {
	
	@POST @Path("/{domain}/store")
	@Consumes({"application/json"})
	public void store( @DefaultValue("Playground") @PathParam("domain") String domain,
			@DefaultValue("") @HeaderParam("token") String token, 
			String riskData ) { //@HeaderParam("json") 
		System.out.println("RiskData: "+ riskData );
		JsonArray json = (JsonArray)new JsonParser().parse( riskData );
		RiscossDB db = DBConnector.openDB( domain, token );
		try {
			for( int i = 0; i < json.size(); i++ ) {
				JsonObject o = json.get( i ).getAsJsonObject();
				try {
					db.storeRiskData( o.toString() );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		finally {
			DBConnector.closeDB( db );
		}
	}
	
}
