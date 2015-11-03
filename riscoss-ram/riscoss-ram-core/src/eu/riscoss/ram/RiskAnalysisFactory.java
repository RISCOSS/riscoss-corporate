package eu.riscoss.ram;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class RiskAnalysisFactory {
	
	private static RiskAnalysisFactory instance = new RiskAnalysisFactory();
	
	public static RiskAnalysisFactory get() {
		return instance;
	}
	
	private RiskAnalysisFactory() {
		activities.put( EMitigationActivity.ACCEPT, new HashMap<String,MitigationActivity>() );
		activities.put( EMitigationActivity.AVOID, new HashMap<String,MitigationActivity>() );
		activities.put( EMitigationActivity.CHANGE, new HashMap<String,MitigationActivity>() );
		activities.put( EMitigationActivity.REMOVE, new HashMap<String,MitigationActivity>() );
		activities.put( EMitigationActivity.RETAIN, new HashMap<String,MitigationActivity>() );
		activities.put( EMitigationActivity.SHARE, new HashMap<String,MitigationActivity>() );
	}
	
	
	Map<EMitigationActivity,Map<String,MitigationActivity>> activities = new HashMap<EMitigationActivity,Map<String,MitigationActivity>>();
	
	public Collection<MitigationActivity> listMitigationActivities( EMitigationActivity type ) {
		Map<String,MitigationActivity> map = activities.get( type );
		if( map != null ) return map.values();
		return new ArrayList<MitigationActivity>();
	}
	
	public MitigationActivity createMitigationActivityInstance( EMitigationActivity type ) {
		return null;
	}
	
}
