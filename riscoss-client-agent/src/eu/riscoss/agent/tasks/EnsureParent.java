package eu.riscoss.agent.tasks;

import java.util.List;

import com.google.gson.Gson;

import eu.riscoss.agent.Context;
import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.shared.JEntityData;

public class EnsureParent implements TestTask {
	
	Gson gson = new Gson();
	
	private String child;
	
	private String parent;
	
	public EnsureParent( String child, String parent ) {
		this.child = child;
		this.parent = parent;
	}

	@Override
	public void execute( RiscossRESTClient rest, Context context  ) {
		JEntityData data = null;
		data = gson.fromJson( rest.domain( context.get( "domain", "" ) ).entity( parent ).getData(), JEntityData.class );
		List<String> children = data.children;
		children.add( child );
		String[] array = new String[children.size()];
		children.toArray( array );
		System.out.println( "SET CHILDREN " + parent + " -> " + children );
		if( "c8".equals( child ) )
			if( "c47".equals( parent ) )
				System.out.print("");
		rest.domain( context.get( "domain", "" ) ).entity( parent ).setChildren( array );
	}
	
}
