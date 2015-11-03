package eu.riscoss.agent.services;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import eu.riscoss.agent.RiscossRESTClient;

public class EntityService extends RESTService {

	public EntityService(RiscossRESTClient rest, String entity) {
		super(rest);
		this.rest.set( "entity", entity );
	}

	public String getData() {
		return rest.get( "entities/" + getDomain() + "/" + encode( getEntity() ) + "/data_new").send();
	}

	public String getEntity() {
		return rest.get( "entity", "" );
	}
	
	public void setChildren( String ... ids ) {
		JsonObject json = new JsonObject();
		JsonArray array = new JsonArray();
		for( String e : ids ) {
			array.add( new JsonPrimitive( e ) );
		}
		json.add( "list", array );
		rest.post( "entities/" + getDomain() + "/" + encode( getEntity() ) + "/children" ).send( json.toString() );
	}

	public String getChildren() {
		return rest.get( "entities/" + getDomain() + "/" + encode( getEntity() ) + "/children" ).send();
	}
	
}
