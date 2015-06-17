package eu.riscoss.client;


public class Range {
	private double min = 0;
	private double max = 1;
	private double intervals = 100;
//	private double step = 0.01;
	
	public Range() {}
	
	public Range( double min, double max, int intervals ) {
		this.min = min;
		this.max = max;
		this.intervals = intervals;
//		this.step = (max - min) / (double)intervals;
	}
	
	public String toString() {
		return min + ";" + max + ";" + intervals;
	}
//	public static Range mk( String str ) {
//		String parts[] = str.split( "[;]" );
//		Range r = new Range();
//		try {
//			r.min = Double.parseDouble( parts[0] );
//			r.max = Double.parseDouble( parts[1] );
//			r.step = Double.parseDouble( parts[2] );
//		}
//		catch( Exception ex ) {
//			ex.printStackTrace();
//		}
//		return r;
//	}
//	public double getDouble( int src ) {
//		return (src * step);
//	}
//	public double getDouble( double d ) {
//		return getDouble( (int)d );
//	}
//	public int getInt( int src ) {
//		return (int)(src * step);
//	}
//	public int getInt( double d ) {
//		return getInt( (int)d );
//	}
	public double getSliderMin() {
		return 0;
//		return (int)(min / step);
	}
	public double getSliderMax() {
		return intervals;
//		return (int)(max / step);
	}
//	public int getSliderStep() {
//		return (int)step;
//	}
//	public void setMin( String val ) {
//		try {
//			min = Double.parseDouble( val );
//		}
//		catch( Exception ex ) {}
//	}
//	public void setMax( String val ) {
//		try {
//			max = Double.parseDouble( val );
//		}
//		catch( Exception ex ) {}
//	}
//	public void setStep( String val ) {
//		try {
//			step = Double.parseDouble( val );
//		}
//		catch( Exception ex ) {}
//	}

	public double getValue( int value ) {
//		Window.alert( min + " + (((" + max + " - " + min + ") / " + intervals + ") * " + value + ") = " + (min + (((max - min) / intervals) * value)) );
		return min + (((max - min) / intervals) * value);
	}
}