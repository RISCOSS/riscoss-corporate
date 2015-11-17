package eu.riscoss.agent.usecases;

import com.google.gson.Gson;

import eu.riscoss.agent.RiscossRESTClient;

public class UCThreads {

	Gson gson = new Gson();
	
	public static void main( String[] args ) {
		
		Thread t1 = new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					new UCCycles( "Domain3" ).run( new RiscossRESTClient( "http://127.0.0.1:8888" ) );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}} );
		Thread t2 = new Thread( new Runnable() {
			@Override
			public void run() {
				try {
					new UCCycles( "Domain3" ).run( new RiscossRESTClient( "http://127.0.0.1:8888" ) );
				} catch (Exception e) {
					e.printStackTrace();
				}
			}} );
		
		t1.start();
		try { Thread.sleep( 1000 ); } catch (InterruptedException e) {}
		t2.start();
		
	}
	
}
