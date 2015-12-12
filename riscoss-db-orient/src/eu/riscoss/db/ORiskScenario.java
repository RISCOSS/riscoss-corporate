package eu.riscoss.db;

public class ORiskScenario extends OrientRAS implements RiskScenario {
	
	String scenario = "";
	private OrientRAS ras;
	
	public ORiskScenario( OrientRAS ras, String name ) {
		super( ras.dom, ras.id );
		this.ras = ras;
		this.scenario = name;
	}
	
	public RiskAnalysisSession getSession() {
		return this.ras;
	}
	
	protected String scenarioPath() {
		return super.path() + "/scenarios/" + scenario;
	}
	
	protected String path() {
		String scenarioPath = scenarioPath();
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
