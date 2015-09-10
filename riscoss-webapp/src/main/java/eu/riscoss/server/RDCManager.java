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

import javax.ws.rs.GET;
import javax.ws.rs.Path;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import eu.riscoss.rdc.RDC;
import eu.riscoss.rdc.RDCFactory;
import eu.riscoss.rdc.RDCParameter;

/**
 * Risk Data Coellectors
 * @author morandini
 *
 */
@Path("rdcs")
public class RDCManager {
	
	@GET @Path("/list")
	public String listRDCs() {
		
		JsonObject o = new JsonObject();
		
		for( RDC rdc : RDCFactory.get().listRDCs() ) {
			JsonObject jrdc = new JsonObject();
			JsonArray params = new JsonArray();
			for( RDCParameter par : rdc.getParameterList() ) {
				JsonObject jpar = new JsonObject();
				jpar.addProperty( "name", par.getName() );
				jpar.addProperty( "desc", par.getDescription() );
				jpar.addProperty( "def", par.getDefaultValue() );
				jpar.addProperty( "ex", par.getExample() );
				params.add( jpar );
			}
			jrdc.add( "params", params );
			o.add( rdc.getName(), jrdc );
		}
		
		System.out.println( "Returning RDC list: " + o.toString() );
		
		return o.toString();
	}
	
}