package eu.riscoss.shared;

public class JDataItem {
	
	String			id;
	EChunkDataType	type;
	EDataOrigin		origin;
	String			value;
	String			label;
	String			description;
	
	public String getId() {
		return id;
	}
	public void setId( String id ) {
		this.id = id;
	}
	public EChunkDataType getType() {
		return type;
	}
	public void setType( EChunkDataType type ) {
		this.type = type;
	}
	public EDataOrigin getOrigin() {
		return origin;
	}
	public void setOrigin( EDataOrigin origin ) {
		this.origin = origin;
	}
	public String getValue() {
		return value;
	}
	public void setValue( String value ) {
		this.value = value;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel( String label ) {
		this.label = label;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription( String description ) {
		this.description = description;
	}
	
}
