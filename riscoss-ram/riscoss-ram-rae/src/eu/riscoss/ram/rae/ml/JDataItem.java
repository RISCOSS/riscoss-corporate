package eu.riscoss.ram.rae.ml;

import eu.riscoss.ram.RiskDataValue;

public class JDataItem {
	
	public String id = "";
	public String description = "";
	public String question = "";
	public String label = "";
	public String type = "";
	public String value = "";
	
	public void setDescription( String description ) {
		this.description = description;
	}
	
	public void setLabel( String label ) {
		this.label = label;
	}
	
	public void setId( String id ) {
		this.id = id;
	}
	
	public void setValue( String value ) {
		this.value = value;
	}
	
	public void setOrigin( EDataOrigin rdr ) {
		// TODO Auto-generated method stub
		
	}
	
	public void setType( EChunkDataType type ) {
		// TODO Auto-generated method stub
		
	}
	
	public void setValue( RiskDataValue dt ) {
		
	}
	
}
