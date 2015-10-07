package eu.riscoss.agent.tasks;

import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import eu.riscoss.agent.RiscossRESTClient;

public class EnsureDomainExistence implements TestTask {
	
	private String domain;

	public EnsureDomainExistence( String domain ) {
		this.domain = domain;
	}
	
	@Override
	public void execute( RiscossRESTClient rest ) {
		List<String> domains = new Gson().fromJson(rest.domains().list(), new TypeToken<List<String>>() {}.getType() );
		boolean found = false;
		for( String d : domains ) {
			if( domain.equals( d ) )
				found = true;
		}
		if( !found ) {
			rest.domains().create( domain );
		}
	}
	
}
