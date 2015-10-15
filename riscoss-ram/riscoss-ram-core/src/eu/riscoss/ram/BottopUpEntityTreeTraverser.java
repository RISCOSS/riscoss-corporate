/*
   (C) Copyright 2013-2016 The RISCOSS Project Consortium
   
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

*/

/**
 * @author 	Alberto Siena
**/

package eu.riscoss.ram;

import java.util.Stack;

import eu.riscoss.db.RiscossDB;

public class BottopUpEntityTreeTraverser {
	
	RiscossDB db;
	Stack<String> layerStack = new Stack<String>();
	
	public BottopUpEntityTreeTraverser( RiscossDB db ) {
		this.db = db;
	}
	
	public void analyseEntity( String entity, TraverseCallback<?> cb ) {
		
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
