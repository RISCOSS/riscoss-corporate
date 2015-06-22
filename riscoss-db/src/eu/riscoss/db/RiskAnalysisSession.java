package eu.riscoss.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface RiskAnalysisSession {
	
	void setLayers( Collection<String> layers );
	
	long getTimestamp();
	
	void setTimestamp( long timestamp );
	
	void addEntity( String entity, String layer );
	
	String getTarget();
	
	void setTarget( String entity );
	
	void setRCName( String rc );
	
	void setRCModels( Map<String, ArrayList<String>> rcModels );
	
	String getId();
	
	void setResult( String layer, String entity, String id, String attribute, String value );
	
	String getResult( String layer, String entity, String indicatorId, String attribute, String def );
	
	List<String> getModels( String layer );
	
	String getLayer( int i );
	
	String getRCName();
	
	int getLayerCount();
	
	int getEntityCount( String layer );
	
	String getEntity( String layer, int index );
	
	Collection<String> getResults( String layer, String entity );
	
	void saveInput( String entity, String indicator_id, String origin, String value );
	
	void setStatus( String entity, String name );
	
	String getInput( String entity, String indicator_id );
	
	String getOption( String key, String def );
	
	void setOption( String key, String value );
	
	String getLayer( String target );
	
	Collection<String> getChildren( String entity );
	
	void setParent( String child, String parent );
	
	void saveResults( String json );
	
	String readResults();
	
	List<String> getEntities( String layer );
	
	Map<String, Object> getResult( String layer, String entity, String indicator );
	
	void setName( String name );
	String getName();
	
}
