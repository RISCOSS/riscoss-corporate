package eu.riscoss.server;

import java.util.Stack;

import eu.riscoss.db.RiscossDB;

public class EntityTreeBrowser {
	
	public static abstract class Callback<T> {
		
		private T value;

		public Callback( T storedValue ) {
			this.value = storedValue;
		}
		
		public T getValue() {
			return this.value;
		}
		
		public abstract void onLayerFound( String layer );
		public abstract void onEntityFound( String entity );
	}
	
	RiscossDB db;
	Stack<String> layerStack = new Stack<String>();
	
	public EntityTreeBrowser( RiscossDB db ) {
		this.db = db;
	}
	
	public void analyseEntity( String entity, Callback<?> cb ) {
		
		String layer = db.layerOf( entity );
		boolean removeLast = false;
		
		if( layerStack.size() < 1 ) {
			layerStack.push( entity );
			removeLast = true;
		}
		else {
			if( !layerStack.equals( layer ) ) {
				layerStack.push( layer );
				removeLast = true;
				cb.onLayerFound( layer );
			}
		}
		
		for( String child : db.getChildren( entity ) ) {
			analyseEntity( child, cb );
		}
		
		cb.onEntityFound( entity );
		
		if( removeLast == true ) {
			layerStack.pop();
		}
		
	}
	
}
