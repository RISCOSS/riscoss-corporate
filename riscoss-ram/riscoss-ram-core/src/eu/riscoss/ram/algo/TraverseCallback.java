package eu.riscoss.ram.algo;

public abstract class TraverseCallback<T> {
	
	private T value;

	public TraverseCallback( T storedValue ) {
		this.value = storedValue;
	}
	
	public T getValue() {
		return this.value;
	}
	
	public boolean onLayerFound( String layer ) {
		return true;
	}
	
	public boolean onEntityFound( String entity ) {
		return false;
	}
	
	public void	beforeEntityAnalyzed( String entity ) {}
	public void	afterEntityAnalyzed( String entity ) {}
}