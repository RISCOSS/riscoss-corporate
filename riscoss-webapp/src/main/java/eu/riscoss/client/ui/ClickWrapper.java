package eu.riscoss.client.ui;

import com.google.gwt.event.dom.client.ClickHandler;

public abstract class ClickWrapper<T> implements ClickHandler {
	T value;
	public ClickWrapper( T t ) {
		this.value = t;
	}
	
	
	public T getValue() {
		return this.value;
	}
}
