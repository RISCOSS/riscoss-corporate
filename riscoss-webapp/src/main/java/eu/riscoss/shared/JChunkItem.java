package eu.riscoss.shared;


public class JChunkItem {
	
	String			id = "";
	EDataOrigin		origin = EDataOrigin.RDR;
	JChunkValue		value = new JChunkValue.JNaN();
	String			label = "";
	String			description = "";
	EChunkType		type;
	
	public String getId() {
		return id;
	}
	public void setId( String id ) {
		this.id = id;
	}
	public EDataOrigin getOrigin() {
		return origin;
	}
	public void setOrigin( EDataOrigin origin ) {
		this.origin = origin;
	}
	public JChunkValue getValue() {
		return value;
	}
	public void setValue( JChunkValue value ) {
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
	public void setType( EChunkType type ) {
		this.type = type;
	}
	public EChunkType getType() {
		return this.type;
	}
	
}
