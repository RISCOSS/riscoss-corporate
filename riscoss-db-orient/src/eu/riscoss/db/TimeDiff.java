package eu.riscoss.db;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class TimeDiff {
	
	private static TimeDiff instance = new TimeDiff();
	
	public static TimeDiff get() {
		return instance;
	}
	
	Date first = null;
	Date second = null;
	
	long[] diff;
	
	public TimeDiff() {
		this( new Date( System.currentTimeMillis() ) );
	}
	
	public TimeDiff( Date first, Date second ) {
		diff = getTimeDifference( second, first );
	}
	
	public TimeDiff( Date first ) {
		this.first = first;
	}
	
	public String getDiff( Date second ) {
		this.second = second;
		
		diff = getTimeDifference( second, first );
		
		return toString();
	}
	
	/**
	 * Calculate the absolute difference between two Date without
	 * regard for time offsets
	 *
	 * @param d1 Date one
	 * @param d2 Date two
	 * @param field The field we're interested in out of
	 * day, hour, minute, second, millisecond
	 *
	 * @return The value of the required field
	 */
	public static long getTimeDifference(Date d1, Date d2, TimeField field) {
		return TimeDiff.getTimeDifference(d1, d2)[field.ordinal()];
	}
	
	/**
	 * Calculate the absolute difference between two Date without
	 * regard for time offsets
	 *
	 * @param d1 Date one
	 * @param d2 Date two
	 * @return The fields day, hour, minute, second and millisecond
	 */
	public static long[] getTimeDifference(Date d1, Date d2) {
		long[] result = new long[5];
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.setTime(d1);
		
		long t1 = cal.getTimeInMillis();
		cal.setTime(d2);
		
		long diff = Math.abs(cal.getTimeInMillis() - t1);
		final int ONE_DAY = 1000 * 60 * 60 * 24;
		final int ONE_HOUR = ONE_DAY / 24;
		final int ONE_MINUTE = ONE_HOUR / 60;
		final int ONE_SECOND = ONE_MINUTE / 60;
		
		long d = diff / ONE_DAY;
		diff %= ONE_DAY;
		
		long h = diff / ONE_HOUR;
		diff %= ONE_HOUR;
		
		long m = diff / ONE_MINUTE;
		diff %= ONE_MINUTE;
		
		long s = diff / ONE_SECOND;
		long ms = diff % ONE_SECOND;
		result[0] = d;
		result[1] = h;
		result[2] = m;
		result[3] = s;
		result[4] = ms;
		
		return result;
	}
	
	public static void printDiffs(long[] diffs) {
		System.out.printf("Days:         %3d\n", diffs[0]);
		System.out.printf("Hours:        %3d\n", diffs[1]);
		System.out.printf("Minutes:      %3d\n", diffs[2]);
		System.out.printf("Seconds:      %3d\n", diffs[3]);
		System.out.printf("Milliseconds: %3d\n", diffs[4]);
	}
	
	public static enum TimeField {
		DAY,
		HOUR,
		MINUTE,
		SECOND,
		MILLISECOND;
	}
	
	public String toString() {
//		return 
//				diff[0] + " day(s), " + 
//				diff[1] + " hour(s), " + 
//				diff[2] + " minute(s), " +
//				diff[3] + " second(s), " + 
//				diff[4] + " millisecond(s)";
		return 
				diff[2] + ":" +
				diff[3] + "." + 
				diff[4];
	}
	
	public void init() {
		first = new Date( System.currentTimeMillis() );
	}
	
	public long getDays() {
		return diff[0];
	}
	
	public long getHours() {
		return diff[1];
	}
	
	public long getMinutes() {
		return diff[2];
	}
	
	public long getSeconds() {
		return diff[3];
	}
	
	public long getMillis() {
		return diff[4];
	}
	
	public Date getStart() {
		return first;
	}
	
	public Date getEnd() {
		return second;
	}
	
	public void set() {
		second = new Date( System.currentTimeMillis() );
		diff = getTimeDifference( second, first );
	}

	public void log( String string ) {
		set();
		System.out.println( this + " " + string );
	}
}