package eu.riscoss.client;

import org.fusesource.restygwt.client.JsonCallback;

public abstract class JsonCallbackWrapper<T> implements JsonCallback {
	private T value;
	public JsonCallbackWrapper( T value ) {
		this.value = value;
	}
	public T getValue() {
		return this.value;
	}
}
