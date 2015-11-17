package eu.riscoss.agent.tasks;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

import eu.riscoss.agent.Context;
import eu.riscoss.agent.RiscossRESTClient;
import eu.riscoss.dataproviders.RiskData;

public class PostRiskData implements TestTask {
	
	List<RiskData> rd = new ArrayList<RiskData>();
	
	String domain = null;
	
	public PostRiskData( String domain, RiskData riskData ) {
		this.rd.add( riskData );
		this.domain = domain;
		if( this.domain == null )
			this.domain = "";
	}

	public PostRiskData( RiskData riskData) {
		this( "", riskData );
	}

	@Override
	public void execute( RiscossRESTClient rest, Context context ) {
		
		String d = this.domain;
		if( d == null )
			d = context.get( "domain", "" );
		
		rest.domain( d ).rdr().store( new Gson().toJson( rd ) );
		
	}
	
}
