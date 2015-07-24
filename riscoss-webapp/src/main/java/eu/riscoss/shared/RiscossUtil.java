/**
 * 
 */
package eu.riscoss.shared;

/**
 * Various utility functions that need to be used to be called in several packages.
 * @author morandini
 *
 */
public class RiscossUtil {

	//private static final String[] PROHIBITED_STRINGS = {"/", "##", "@", "\""};
	
	private static final String PROHIBITED_STRINGS_REGEXP = "/|##|@|\"";
	private static final String REPLACEMENT_STRING = "_";

	
	/**
	 * Sanitisation has to be used for every filename and other strings to be able to be stored correctly in the database
	 * @param input
	 * @return
	 */
	public static String sanitize(String input){
		return sanitize(input, PROHIBITED_STRINGS_REGEXP);
	}
	
	public static String sanitize(String input, String prohibitedStringsRegexp){
		return sanitize(input, prohibitedStringsRegexp, REPLACEMENT_STRING);
	}
	
	public static String sanitize(String input, String prohibitedStringsRegexp, String replacement){
		String s = input.replaceAll(prohibitedStringsRegexp, REPLACEMENT_STRING);
		return s;
	}
	
}
