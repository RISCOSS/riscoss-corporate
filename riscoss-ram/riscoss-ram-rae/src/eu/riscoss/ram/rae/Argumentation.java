package eu.riscoss.ram.rae;

import java.io.PrintStream;

public class Argumentation {
	
	Argument arg = new Argument( "" );
	
	public Argumentation() {
		arg.setSummary( "There is some evience of risk" );
	}
	
	public Argument getArgument() {
		return this.arg;
	}

	public void print( PrintStream out ) {
		
		print( arg, out, "" );
		
	}

	private void print( Argument a, PrintStream out, String prefix ) {
		
		out.println( prefix + " " + a.getSummary() + ": " + a.getTruth() );
		
		for( Argument sub_a : a.subArguments() ) {
			print( sub_a, out, prefix + "  " );
		}
		
	}

	public void add( Argumentation a ) {
		for( Argument arg : a.getArgument().subArguments() ) {
			getArgument().addArgument( arg );
		}
	}
	
}
