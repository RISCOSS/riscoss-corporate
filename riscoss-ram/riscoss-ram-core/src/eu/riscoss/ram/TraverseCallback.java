package eu.riscoss.ram;

public abstract class TraverseCallback<T> {
	
	private T value;

	public TraverseCallback( T storedValue ) {
		this.value = storedValue;
	}
	
	public T getValue() {
		return this.value;
	}
	
	public abstract void onLayerFound( String layer );
	public abstract void onEntityFound( String entity );
}