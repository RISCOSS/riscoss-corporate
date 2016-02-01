package eu.riscoss.db;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// TODO: To be renamed in something like "RiscossDomainDB"

/**
 * Riscoss database access, for all accesses to the database content (except users and domain) with a defined domain and user credentials
 * For access to changing domains and users only, "RiscossDatabase" has to be used.
 */
public interface RiscossDB {
	
	/*
	 * Layers
	 */
	public abstract Collection<String>				layerNames();
	public abstract void							addLayer(String name, String after);
	public abstract void							removeLayer(String name);
	public abstract void							renameLayer(String name, String newName);
	public abstract void							editParent(String layer, String newParent);
	public abstract void							setLayerData( String layer, String key, String value );
	public abstract String							getLayerData( String layer, String key );
	public abstract List<String>					getScope( String layer );
	
	/*
	 * Entities
	 */
	public abstract Collection<String>				entities();
	public abstract Collection<String>				entities( String layer );
	public abstract boolean							existsEntity( String entity );
	public abstract void							addEntity(String name, String layer );
	public abstract void							removeEntity(String name);
	public abstract void							assignEntity( String entity, String parent );
	public abstract void							removeEntity( String entity, String parent );
	public abstract List<String>					getParents( String entity );
	public abstract List<String>					getChildren( String entity );
	public abstract String							layerOf(String entity);
	public abstract void							editLayer(String entity, String layer);
	public abstract Collection<String>				listUserData( String entity );
	
	/*
	 * Risk Data Collectors Configuration
	 */
	public abstract boolean							isRDCEnabled( String entity, String rdc );
	public abstract void							setRDCEnabled( String entity, String rdc, boolean enabled );
	public abstract void							setRDCParmeter( String entity, String rdcName, String name, String value );
	public abstract String							getRDCParmeter( String entity, String rdcName, String name, String def );
	
	/*
	 * Models
	 */
	public abstract List<String>					getModelList();
	public abstract void							removeModel(String name);
	public abstract void							storeModel(String modelBlob, String modelName);
	public abstract String							getModelBlob(String modelName);
	public abstract void							removeModelBlob(String modelName);
	public abstract void							updateModel(String modelName, String blobFilename, String modelBlob);
	public abstract String							getModelFilename(String modelName); 
	public abstract void							changeModelName(String modelName, String newName);
	
	/*
	 * Model description
	 */
	public abstract void							storeModelDesc(String modelName, String blobFilename, byte[] modelDescBlob);
	public abstract byte[]							getModelDescBlob(String modelName);
	public abstract void							removeModelDescBlob(String modelName);
	public abstract String							getModelDescFielname(String modelName);
	
	/*
	 * Risk Configurations
	 */
	public abstract Collection<String>				getRiskConfigurations();
	public abstract boolean							existsRAS( String rc );
	public abstract void							createRiskConfiguration(String name);
	public abstract void							removeRiskConfiguration(String name);
	public abstract List<String>					getModelsFromRiskCfg( String rc_name , String entity  );
	public abstract void							setModelsFromRiskCfg( String rcName, List<String> list);
	public abstract void							setRCModels( String entity, Map<String,ArrayList<String>> map );
	public abstract Map<String, ArrayList<String>>	getRCModels( String rc );
	public abstract List<String>					findCandidateRCs( String layer );
	
	/*
	 * Risk Data Repository
	 */
	public abstract void							storeRiskData(String rd) throws Exception;
	public abstract String							readRiskData( String target, String indicator_id);
	public abstract Collection<String>				listRiskData( String e_name );
	
	/*
	 * Risk Analysis Session
	 */
	public abstract void							storeRASResult( String target, String string );
	public abstract String							readRASResult( String entity );
	public abstract RiskAnalysisSession				createRAS();
	public abstract void							saveRAS( RiskAnalysisSession ras );
	public abstract RiskAnalysisSession				openRAS( String sid );
	public abstract List<RecordAbstraction>			listRAS( String entity, String rc );
	public abstract void							destroyRAS( String ras );
	
	public abstract void							close();
	public abstract String							getName();
	
	/*
	 * User management
	 */
	public abstract void							createRole( String name );
	public abstract List<String>					listRoles( String domain );
	public abstract List<String>					listUsers();
	public abstract List<String>					listUsers( String role );
	public abstract void							setUserRole( String user, String role );
	public abstract String							getRole( String username );
	public abstract void							addPermissions( String name, RiscossDBResource res, String perm );
	public abstract Collection<String>				findEntities( String layer, String query, SearchParams params );
	public abstract void							removeUserFromDomain( String name );
	public abstract void 							renameEntity(String entity, String newName);
	
	/*
	 * General purpose methods
	 */
	public abstract String							getProperty( RiscossElements element, String name, String propertyName, String def );
	public abstract void							setProperty( RiscossElements element, String name, String propertyName, String value );
	
}
