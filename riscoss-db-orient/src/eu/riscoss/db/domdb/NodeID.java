package eu.riscoss.db.domdb;

public class NodeID implements Comparable<NodeID> {
	
	private String value;

	public NodeID( String val ) {
		this.value = val;
	}
	
	public String toString() {
		return this.value;
	}
	
	@Override
	public int compareTo( NodeID o ) {
		return value.compareTo( o.value );
	}
}
