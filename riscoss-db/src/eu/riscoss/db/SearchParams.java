package eu.riscoss.db;

public class SearchParams {
	
	public enum SORT {
		NONE, AZ
	}
	
	public int from = 0;
	public int max = 0;
	public boolean loadHierarchy = false;
	public SORT sort = SORT.NONE;
	
	public void setFrom( String strFrom ) {
		try {
			this.from = Integer.parseInt( strFrom );
		}
		catch( Exception ex ) {}
	}

	public void setMax( String strMax ) {
		try {
			this.max = Integer.parseInt( strMax );
		}
		catch( Exception ex ) {}
	}

	public void setOptLoadHierarchy( String strHierarchy ) {
		try {
			this.loadHierarchy = Boolean.parseBoolean( strHierarchy );
		}
		catch( Exception ex ) {}
	}
	
}
