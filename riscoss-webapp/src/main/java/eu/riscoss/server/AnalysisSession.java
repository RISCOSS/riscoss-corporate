//package eu.riscoss.server;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
//import com.google.gson.JsonArray;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonPrimitive;
//
//import eu.riscoss.db.RiskAnalysisSession;
//import eu.riscoss.reasoner.DataType;
//import eu.riscoss.reasoner.Distribution;
//import eu.riscoss.shared.AnalysisOption;
//import eu.riscoss.shared.AnalysisResult;
//
//public class AnalysisSession implements RiskAnalysisSession {
//	
//	static class Entity {
//		String				name;
//		Map<String,Entity>	children = new HashMap<String,Entity>();
//		
//		public Entity( String entityName ) {
//			this.name = entityName;
//		}
//		
//		public Entity( String name, String layer ) {
//			this.name = name;
//		}
//		
//		public Collection<Entity> getChildren() {
//			return children.values();
//		}
//		
//		public String getName() {
//			return name;
//		}
//	}
//	
//	static class Layer {
//		String name;
//		List<Entity> entities = new ArrayList<Entity>();
//		Map<String,Entity> map = new HashMap<String,Entity>();
//		
//		public void addEntity( String entityName ) {
//			Entity entity = new Entity( entityName );
////			entity.layer = name;
//			entities.add( entity );
//			map.put( entityName, entity );
//		}
//
//		public String getEntity( int index ) {
//			return entities.get( index ).name;
//		}
//
//		public Entity getEntity( String entity ) {
//			return map.get( entity );
//		}
//	}
//	
//	static class Configuration {
//		Map<String,ArrayList<String>>	models = new HashMap<String,ArrayList<String>>();
//		Set<Configuration>				childRCs = new HashSet<Configuration>();
//		public String name				= "";
//		
//		public Collection<Configuration> subConfigurations() {
//			return childRCs;
//		}
//		
//		public List<String> getModels( String name ) {
//			List<String> list = models.get( name );
//			if( list == null ) list = new ArrayList<String>();
//			return list;
//		}
//		
//		public void setModels( Map<String, ArrayList<String>> rcModels ) {
//			this.models = rcModels;
//		}
//	}
//	
//	static class UserData {
//		
//		JsonObject data = new JsonObject();
//		
//		public String getRiskData( String id ) {
//			JsonElement e = data.get( id );
//			if( e == null ) return null;
//			if( e.getAsJsonObject() == null ) return null;
//			return e.toString();
//		}
//		
//	}
//	
//	static class Options {
//		
//		private int verbosity;
//		private AnalysisOption opt;
//
//		public int getVerbosity() {
//			return this.verbosity;
//		}
//
//		public void setVerbosity( int verbosity ) {
//			this.verbosity = verbosity;
//		}
//
//		public void setAction( AnalysisOption opt ) {
//			this.opt = opt;
//		}
//		
//		public AnalysisOption getAnalysisOption() {
//			return this.opt;
//		}
//		
//	}
//	
//	static class Inputs {
//		
//		JsonArray jinputs = new JsonArray();
//		
//		Map<String,String> data = new HashMap<String,String>();
//		
//		public void put( JsonObject json ) {
//			jinputs.add( json );
//		}
//
//		public void save( String entity, String value ) {
//			data.put( entity, value );
//		}
//		
//	}
//	
//	static class Results {
//		
//		static class DataMap {
//			Map<String,String> values = new HashMap<String,String>();
//			
//			public void setValue( String attribute, String value ) {
//				values.put( attribute, value );
//			}
//			
//			public String getValue( String attribute, String def ) {
//				String ret = values.get( attribute );
//				if( ret == null ) return def;
//				return ret;
//			}
//		}
//		
//		static class IndicatorMap {
//			Map<String,DataMap> indicators = new HashMap<String,DataMap>();
//			
//			public void setValue( String id, String attribute, String value ) {
//				DataMap map = indicators.get( id );
//				if( map == null ) {
//					map = new DataMap();
//					indicators.put( id, map );
//				}
//				map.setValue( attribute, value );
//			}
//			
//			public String getValue( String id, String attribute, String def ) {
//				DataMap map = indicators.get( id );
//				if( map == null ) return def;
//				return map.getValue( attribute, def );
//			}
//			
//			public Collection<String> indicators() {
//				return indicators.keySet();
//			}
//		}
//		
//		static class EntityMap {
//			Map<String,IndicatorMap> entities = new HashMap<String,IndicatorMap>();
//			
//			public void setValue( Entity target, String id, String attribute, String value ) {
//				setValue( target.name, id, attribute, value );
//			}
//			
//			public void setValue( String entity, String id, String attribute, String value ) {
//				IndicatorMap map = entities.get( entity );
//				if( map == null ) {
//					map = new IndicatorMap();
//					entities.put( entity, map );
//				}
//				map.setValue( id, attribute, value );
//			}
//			
//			public String getValue( Entity target, String id, String attribute, String def ) {
//				return getValue( target.name, id, attribute, def );
//			}
//			
//			public String getValue( String entityName, String id, String attribute, String def ) {
//				IndicatorMap map = entities.get( entityName );
//				if( map == null ) return def;
//				return map.getValue( id, attribute, def );
//			}
//			
//			public Collection<String> entities() {
//				return entities.keySet();
//			}
//			
//			public Collection<String> indicators( String entityName ) {
//				IndicatorMap map = entities.get( entityName );
//				if( map == null ) return new ArrayList<String>();
//				return map.indicators();
//			}
//		}
//		
//		static class LayerMap {
//			Map<String,EntityMap> layers = new HashMap<String,EntityMap>();
//			
//			public void setValue( Layer layer, Entity target, String id, String attribute, String value ) {
//				setValue( layer.name, target.name, id, attribute, value );
//			}
//			
//			public void setValue( String layer, String entity, String id, String attribute, String value ) {
//				EntityMap map = layers.get( layer );
//				if( map == null ) {
//					map = new EntityMap();
//					layers.put( layer, map );
//				}
//				map.setValue( entity, id, attribute, value );
//			}
//			
//			public String getValue( Layer layer, Entity target, String id, String attribute, String def ) {
//				return getValue( layer.name, target.name, id, attribute, def );
//			}
//			
//			public String getValue( String layerName, String entityName, String id, String attribute, String def ) {
//				EntityMap map = layers.get( layerName );
//				if( map == null ) return def;
//				return map.getValue( entityName, id, attribute, def );
//			}
//			
//			public Collection<String> layers() {
//				return layers.keySet();
//			}
//			
//			public Collection<String> entities( String layerName ) {
//				EntityMap map = layers.get( layerName );
//				if( map == null ) return new ArrayList<String>();
//				return map.entities();
//			}
//			
//			public Collection<String> indicators( String layerName, String entityName ) {
//				EntityMap map = layers.get( layerName );
//				if( map == null ) return new ArrayList<String>();
//				return map.indicators( entityName );
//			}
//		}
//		
//		// Layer => Entity => Indicator => Value[]
//		LayerMap data = new LayerMap();
//		
//		public void setValue( Layer layer, Entity target, String id, String attribute, String value ) {
//			data.setValue( layer, target, id, attribute, value );
//		}
//		
//		public void setValue( String layer, String entity, String id, String attribute, String value ) {
//			data.setValue( layer, entity, id, attribute, value );
//		}
//		public String getValue( Layer layer, Entity target, String id, String attribute, String def ) {
//			return data.getValue( layer, target, id, attribute, def );
//		}
//		
//		public String getValue( String layer, String entity, String id, String attribute, String def ) {
//			return data.getValue( layer, entity, id, attribute, def );
//		}
//		
//		public String toJson() {
//			return encodeResults().toString();
//		}
//		
//		JsonArray encodeResults() {
//			
//			JsonArray ret = new JsonArray();
//			
//			for( String layerName : data.layers() ) {
//				
//				for( String entityName : data.entities( layerName ) ) {
//					
//					for( String indicatorId : data.indicators( layerName, entityName ) ) {
//						
//						JsonObject o = new JsonObject();
//						o.addProperty( "id", indicatorId );
//						DataType dt = DataType.valueOf( data.getValue( layerName, entityName, indicatorId, "datatype", DataType.REAL.name() ) );
//						o.addProperty( "datatype", dt.name().toLowerCase() );
//						switch( dt ) {
//						case EVIDENCE: {
//							JsonObject e = new JsonObject();
//							e.addProperty( "e", 
//									Double.parseDouble( data.getValue( layerName, entityName, indicatorId, "e", "0" ) ) );
//							o.add( "e", e );
//							o.addProperty( "p", data.getValue( layerName, entityName, indicatorId, "p", "0" ) );
//							o.addProperty( "m", data.getValue( layerName, entityName, indicatorId, "m", "0" ) );
//							o.addProperty( "description", data.getValue( layerName, entityName, indicatorId, "description", "" ) );
//							o.addProperty( "label", data.getValue( layerName, entityName, indicatorId, "label", indicatorId ) );
//						}
//						break;
//						case DISTRIBUTION: {
//							String value = data.getValue( layerName, entityName, indicatorId, "value", "" );
//							Distribution d = Distribution.unpack( value );
//							JsonArray values = new JsonArray();
//							for( int i = 0; i <  d.getValues().size(); i++ ) {
//								values.add( new JsonPrimitive( "" + d.getValues().get( i ) ) );
//							}
//							o.add( "value", values );
//						}
//						break;
//						case INTEGER:
//							o.addProperty( "value", data.getValue( layerName, entityName, indicatorId, "value", "0" ) );
//							break;
//						case NaN:
//							break;
//						case REAL:
//							o.addProperty( "value", data.getValue( layerName, entityName, indicatorId, "value", "0" ) );
//							break;
//						case STRING:
//							o.addProperty( "value", data.getValue( layerName, entityName, indicatorId, "value", "" ) );
//							break;
//						default:
//							break;
//						}
//						ret.add( o );
//					}
//					
//				}
//			}
//			
//			return ret;
//		}
//		
//	}
//	
//	static class LayerMap {
//		
//		// ordered list
//		List<Layer>			layers = new ArrayList<Layer>();
//		// key-value map
//		Map<String,Layer>	layerMap = new HashMap<String,Layer>();
//		
//		public void clear() {
//			this.layers.clear();
//			this.layerMap.clear();
//		}
//		
//		public List<Layer> list() {
//			return layers;
//		}
//		
//		public void add( Layer layer ) {
//			this.layers.add( layer );
//			this.layerMap.put( layer.name, layer );
//		}
//		
//		public Layer get( String layerName ) {
//			return layerMap.get( layerName );
//		}
//		
//	}
//	
//	Options				options = new Options();
//	
//	UserData			userData = new UserData();
//	
//	Entity				target;
//	LayerMap			layers = new LayerMap();
//	Configuration		rc;
//	
//	Inputs				inputs = new Inputs();
//	
//	Results				results = new Results();
//	
//	final long			timestamp;
//	final String		id;
//	
//	public AnalysisSession() {
//		timestamp = new Date().getTime();
//		id = "" + timestamp + ":" + hashCode();
//	}
//	
//	public Entity getTargetEntity() {
//		return target;
//	}
//	
//	public Configuration getRiskConfiguration() {
//		return rc;
//	}
//	
//	public Results getResults() {
//		return results;
//	}
//	
//	public List<Layer> getLayers() {
//		return layers.list();
//	}
//	
//	public Options getOptions() {
//		return options;
//	}
//	
//	public Inputs getInputs() {
//		return inputs;
//	}
//	
//	public UserData getUserData() {
//		return userData;
//	}
//	
//	public void setLayers( Collection<String> layers ) {
//		this.layers.clear();
//		for( String layerName : layers ) {
//			Layer layer = new Layer();
//			layer.name = layerName;
//			this.layers.add( layer );
//		}
//	}
//	
//	public Layer getLayer( String layerName ) {
//		return layers.get( layerName );
//	}
//	
//	public void setRiskConfiguration( Configuration rc ) {
//		this.rc = rc;
//	}
//	
//	public void setTarget( Entity target ) {
//		this.target = target;
//	}
//	
//	public JsonObject getAnalysisResults() {
//		
//		JsonObject json = new JsonObject();
//		
//		json.add( "results", results.encodeResults() );
//		json.addProperty( "result", getResults().getValue( "", "", "", "analysis-result", AnalysisResult.Failure.name() ) );
//		
//		if( getOptions().getVerbosity() > 0 ) {
//			JsonObject info = new JsonObject();
//			info.addProperty( "entity", target.name );
//			json.add( "info", info );
//			json.add( "inputs", inputs.jinputs );
//		}
//		
//		return json;
//		
//	}
//
//	public long getTimestamp() {
//		return this.timestamp;
//	}
//	
//	public String getId() {
//		return this.id;
//	}
//
//	@Override
//	public void addEntity( String entity, String layer ) {
//		getLayer( layer ).addEntity( entity );
//	}
//
//	@Override
//	public String getTarget() {
//		return this.target.getName();
//	}
//
//	@Override
//	public void setTarget( String target ) {
//		this.target = new Entity( target );
//	}
//
//	@Override
//	public void setRCName( String rc ) {
//		this.rc.name = rc;
//	}
//
//	@Override
//	public void setRCModels( Map<String, ArrayList<String>> rcModels ) {
//		this.rc.models = rcModels;
//	}
//
//	@Override
//	public void setResult( String layer, String entity, String id, String attribute, String value ) {
//		results.setValue(layer, entity, id, attribute, value);
//	}
//
//	@Override
//	public List<String> getModels( String layer ) {
//		return rc.getModels( layer );
//	}
//
//	@Override
//	public String getLayer( int i ) {
//		return getLayers().get( i ).name;
//	}
//
//	@Override
//	public String getRCName() {
//		return rc.name;
//	}
//
//	@Override
//	public int getLayerCount() {
//		return getLayers().size();
//	}
//	
//	@Override
//	public int getEntityCount( String layer ) {
//		try {
//			return getLayer( layer ).entities.size();
//		}
//		catch( Exception ex ) {
//			return 0;
//		}
//	}
//
//	@Override
//	public String getEntity( String layer, int index ) {
//		try {
//			return getLayer( layer ).getEntity( index );
//		}
//		catch( Exception ex ) {
//			return null;
//		}
//	}
//
//	@Override
//	public Collection<String> getIndicators( String layer, String entity ) {
//		return results.data.layers.get( layer ).entities.get( entity ).indicators.keySet();
//	}
//	
//	@Override
//	public String getResult( String layer, String entity, String indicatorId, String attribute, String def ) {
//		return results.getValue( layer, entity, indicatorId, attribute, def );
//	}
//	
//	@Override
//	public void saveInput( String entity, String indicator_id, String origin, String value ) {
//		inputs.save( entity, value );
//	}
//
//	@Override
//	public void setStatus( String entity, String name ) {
//		// TODO
//		throw new RuntimeException( "To be implemented" );
//	}
//
//	@Override
//	public String getInput( String entity, String indicator_id ) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	@Override
//	public String getOption( String key ) {
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
//}
