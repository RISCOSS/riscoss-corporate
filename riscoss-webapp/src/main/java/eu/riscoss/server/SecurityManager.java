package eu.riscoss.server;

import eu.riscoss.db.RiscossDatabase;

public class SecurityManager {
	
	private static final SecurityManager instance = new SecurityManager();
	
	public static SecurityManager get() {
		return instance;
	}
	
	public String getUser( String token ) throws Exception {
		
		RiscossDatabase database = DBConnector.openDatabase( token );
		
		try {
			return database.getUsername();
		}
		catch( Exception ex ) {
			throw ex;
		}
		finally {
			if( database != null )
				database.close();
		}
		
	}
	
}
