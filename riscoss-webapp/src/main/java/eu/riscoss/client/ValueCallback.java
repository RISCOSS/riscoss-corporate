package eu.riscoss.client;

public abstract class ValueCallback<T> {
	
	private T value;

	public ValueCallback( T value ) {
		this.value = value;
	}
	
	public T getValue() {
		return this.value;
	}
	
	public abstract void onCallback();
	
}
