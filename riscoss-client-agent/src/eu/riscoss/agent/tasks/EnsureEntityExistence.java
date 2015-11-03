package eu.riscoss.agent.tasks;

import com.google.gson.Gson;

import eu.riscoss.agent.Context;
import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.shared.JEntityData;

public class EnsureEntityExistence implements TestTask {
	
	private String layer;
	private String entity;
	
	Gson gson = new Gson();
	
	public EnsureEntityExistence( String layer, String entity ) {
		this.layer = layer;
		this.entity = entity;
	}
	
	@Override
	public void execute( RiscossRESTClient rest , Context context  ) {
		JEntityData data = null;
		data = gson.fromJson( rest.domain( context.get( "domain", "" ) ).entity( entity ).getData(), JEntityData.class );
		if( data.layer == null ) {
			rest.domain( context.get( "domain", "" ) ).entities().create( entity, layer );
		}
	}
	
}
