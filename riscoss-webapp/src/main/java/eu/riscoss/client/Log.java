package eu.riscoss.client;

public class Log {
	public static void println( String msg ) {
		try {
			consolePrint( msg );
		}
		catch( Exception ex ) {}
	}
	
	private static native void consolePrint( String msg ) /*-{ console.log( msg ); }-*/;
}
