package eu.riscoss.client;

public interface Callback<T> {
	
	void onDone( T pack );
	
	void onError( Throwable t );
	
}
