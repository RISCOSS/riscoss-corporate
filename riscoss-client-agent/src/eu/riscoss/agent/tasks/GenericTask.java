package eu.riscoss.agent.tasks;


public abstract class GenericTask<T> implements TestTask {
	
	T value;
	
	public GenericTask( T value ) {
		this.value = value;
	}
	
	public T getObject() {
		return this.value;
	}
	
}
