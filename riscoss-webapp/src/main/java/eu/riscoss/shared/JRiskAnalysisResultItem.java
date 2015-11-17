package eu.riscoss.shared;

public class JRiskAnalysisResultItem {
	
	public String id = "";
	public EChunkDataType datatype;
	public String type = "";
	public double rank = 0.0;
	public String description = "";
	public String label = "";
	public double p = 0.0;
	public double m = 0.0;
	public String value = "";
	
	/* JsonArray "results"
	 * 
	 * [
	 *   String id
	 *   datatype
	 *   value
	 * ]
	 * 
	 */
	
}
