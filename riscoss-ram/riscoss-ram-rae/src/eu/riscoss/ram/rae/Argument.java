package eu.riscoss.ram.rae;

import java.util.ArrayList;
import java.util.List;

public class Argument {
	
	List<Argument> sub_args = new ArrayList<Argument>();
	
	double truthValue = 0;

	String id;

	String summary = "";
	
	public Argument( String id ) {
		this.id = id;
	}

	public void setTruth( double d ) {
		this.truthValue = round( d );
	}
	
	public double getTruth() {
		return this.truthValue;
	}

	private double round( double d ) {
		return ((double)((int)(d * 100000))) / 100000;
	}
	
	public void addArgument( Argument arg ) {
		this.sub_args.add( arg );
	}

	public void setSummary( String string ) {
		this.summary = string;
	}

	public String getSummary() {
		return this.summary;
	}

	public String getId() {
		return this.id;
	}

	public List<Argument> subArguments() {
		return this.sub_args;
	}
	
}
