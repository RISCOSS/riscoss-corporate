package eu.riscoss.client;

public abstract class CallbackWrapper<T,V> implements Callback<T> {
	
	V value;
	
	public CallbackWrapper( V value ) {
		this.value = value;
	}
	
	public V getValue() {
		return this.value;
	}
	
}
