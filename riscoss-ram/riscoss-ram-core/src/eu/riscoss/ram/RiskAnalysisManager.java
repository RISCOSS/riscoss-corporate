package eu.riscoss.ram;

import java.util.HashMap;
import java.util.Map;


public class RiskAnalysisManager {
	
	private static RiskAnalysisManager instance = new RiskAnalysisManager();
	
	public static RiskAnalysisManager get() {
		return instance;
	}
	
	private RiskAnalysisManager() {}
	
	Map<String,Class<?>> maClasses = new HashMap<String, Class<?>>();
	
	public void register( String maName, Class<? extends MitigationActivity> maClass ) {
		maClasses.put( maName, maClass );
	}

	public MitigationActivity getMitigationTechniqueInstance( String name ) {
		
		Class<?> cls = maClasses.get( name );
		
		if( cls == null ) return null;
		
		MitigationActivity ma = null;
		
		try {
			ma = (MitigationActivity) cls.newInstance();
		} catch(InstantiationException e) {
			e.printStackTrace();
			return null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
		
		return ma;
	}
	
}
