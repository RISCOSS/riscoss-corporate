//package eu.riscoss.ram.app;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.Map;
//
//import eu.riscoss.db.RiskScenario;
//
//public class InMemoryScenario implements RiskScenario {
//	
//	List<String> layers = new ArrayList<String>();
//	long timestamp;
//	
//	String target = "";
//	private String rcName;
//	
//	@Override
//	public void setLayers( Collection<String> layers ) {
//		this.layers = new ArrayList<String>();
//		this.layers.addAll( layers );
//	}
//
//	@Override
//	public long getTimestamp() {
//		return this.timestamp;
//	}
//
//	@Override
//	public void setTimestamp( long timestamp ) {
//		this.timestamp = timestamp;
//	}
//
//	@Override
//	public void addEntity( String entity, String layer ) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public String getTarget() {
//		return this.target;
//	}
//
//	@Override
//	public void setTarget( String entity ) {
//		this.target = entity;
//	}
//
//	@Override
//	public void setRCName( String rc ) {
//		this.rcName = rc;
//	}
//
//	@Override
//	public void setRCModels( Map<String, ArrayList<String>> rcModels ) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public String getId() {
//		return "" + this.hashCode();
//	}
//
//	@Override
//	public void setResult( String layer, String entity, String id, String attribute, String value ) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public String getResult( String layer, String entity, String indicatorId,
//			String attribute, String def ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<String> getModels( String layer ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getLayer( int index ) {
//		try {
//			return layers.get( index );
//		}
//		catch( Exception ex ) {
//			return null;
//		}
//	}
//
//	@Override
//	public String getRCName() {
//		return this.rcName;
//	}
//
//	@Override
//	public int getLayerCount() {
//		return this.layers.size();
//	}
//
//	@Override
//	public int getEntityCount( String layer ) {
//		// TODO Auto-generated method stub
//		return 0;
//	}
//
//	@Override
//	public String getEntity( String layer, int index ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Collection<String> getResults( String layer, String entity ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void saveInput( String entity, String indicator_id, String origin,
//			String value ) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void setStatus( String entity, String name ) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public String getInput( String entity, String indicator_id ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getOption( String key, String def ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setOption( String key, String value ) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public String getLayer( String target ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Collection<String> getChildren( String entity ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setParent( String child, String parent ) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void saveResults( String json ) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public String readResults() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<String> getEntities( String layer ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public Map<String, Object> getResult( String layer, String entity,
//			String indicator ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setName( String name ) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public String getName() {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void storeModelBlob( String name, String layer, String blob ) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public String getStoredModelBlob( String model ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public void setEntityAttribute( String target, String key, String value ) {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public String getEntityAttribute( String target, String key, String def ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public List<String> listInputs( String entity ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//	
//}
