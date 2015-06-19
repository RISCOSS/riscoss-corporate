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

package eu.riscoss.client;


public class Range {
	private double min = 0;
	private double max = 1;
	private double intervals = 100;
	
	public Range() {}
	
	public Range( double min, double max, int intervals ) {
		this.min = min;
		this.max = max;
		this.intervals = intervals;
	}
	
	public String toString() {
		return min + ";" + max + ";" + intervals;
	}
	public double getSliderMin() {
		return 0;
	}
	public double getSliderMax() {
		return intervals;
	}
	public double getValue( int value ) {
		return min + (((max - min) / intervals) * value);
	}
}