package eu.riscoss.db;

public class ORiskScenario extends OrientRAS implements RiskScenario {
	
	String scenario = "";
	
	public ORiskScenario( OrientRAS ras, String name ) {
		super( ras.dom, ras.id );
		this.scenario = name;
	}
	
	protected String path() {
		String scenarioPath = super.path() + "/scenarios/" + scenario;
		if( dom.get( scenarioPath ) != null )
			return scenarioPath;
		else
			return super.path();
	}
	
	@Override
	public void set( String key, String value ) {
		setAttribute( path(), key, value);
	}

	@Override
	public String get( String key, String def ) {
		return getAttribute( path(), key, def );
	}
	
}
