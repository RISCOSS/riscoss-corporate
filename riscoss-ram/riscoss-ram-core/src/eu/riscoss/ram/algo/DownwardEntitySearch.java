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

package eu.riscoss.ram.algo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import eu.riscoss.db.RiscossDB;

public class DownwardEntitySearch {
	
	public enum DIRECTION {
		DOWN, UP
	}
	
	RiscossDB db;
	Stack<String> layerStack = new Stack<String>();
	
	Set<String> set = new HashSet<String>();
	
	private DIRECTION direction = DIRECTION.DOWN;
	
	public DownwardEntitySearch( RiscossDB db ) {
		this.db = db;
	}
	
	public DownwardEntitySearch( RiscossDB db, DIRECTION d ) {
		this.db = db;
		this.direction = d;
	}
	
	public void analyseEntity( String entity, TraverseCallback<?> cb ) {
		
		String layer = db.layerOf( entity );
		
		String top = "";
		if( layerStack.size() > 0 )
			top = layerStack.peek();
		if( top == null ) top = "";
		
		if( !top.equals( layer ) ) {
			
			if( !cb.onLayerFound( layer ) ) {
				return;
			}
			
		}
		
		layerStack.push( layer );
		
		if( set.contains( entity ) ) {
			System.err.println( "CYCLE DETECTED!" );
			for( String s : set ) System.out.print( s );
			System.out.println();
			return;
		}
		
		if( cb.onEntityFound( entity ) ) return;
		
		cb.beforeEntityAnalyzed( entity );
		
		set.add( entity );
		
		for( String child : next( entity ) ) {
			analyseEntity( child, cb );
		}
		
		cb.afterEntityAnalyzed( entity );
		
		set.remove( entity );
		
		layerStack.pop();
		
	}
	
	private List<String> next( String entity ) {
		if( direction == DIRECTION.UP ) {
			return db.getParents( entity );
		}
		else {
			return db.getChildren( entity );
		}
	}
	
}
